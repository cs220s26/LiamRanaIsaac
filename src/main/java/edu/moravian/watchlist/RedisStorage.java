package edu.moravian.watchlist;

import edu.moravian.exceptions.StorageException;
import edu.moravian.media.Media;
import edu.moravian.media.Movie;
import edu.moravian.media.Show;
import edu.moravian.media.mapper.MediaMapper;
import edu.moravian.media.mapper.MovieMapper;
import edu.moravian.media.mapper.ShowMapper;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.exceptions.JedisException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class RedisStorage implements WatchlistAppStorage {
    //Adding to Watchlists
    private static final String MEDIA_ID_COUNTER = "media:counter";
    private static final String WATCHLIST_KEY = "watchlist";

    //Filters for media
    private static final String GENRE_PREFIX = "has:genre:";
    private static final String RATING_KEY = "has:rating";
    private static final String STREAMING_SERVICE_PREFIX = "has:platform:";

    //Filters specific for movies
    private static final String RUNTIME_KEY = "has:runtime";
    private static final String DIRECTOR_PREFIX = "has:director:";
    private static final String RELEASE_YEAR_KEY = "has:release";

    //Filters specific for shows
    private static final String START_YEAR_KEY = "has:start";
    private static final String END_YEAR_KEY = "has:end";
    private static final String SEASONS_KEY = "has:seasons";

    private final Jedis jedis;
    private final Map<String, MediaMapper<? extends Media>> mapperTypes;

    public RedisStorage(String hostname, int port) {
        this.jedis = new Jedis(hostname, port);
        this.mapperTypes = new HashMap<>();
        this.mapperTypes.put("movie", new MovieMapper());
        this.mapperTypes.put("show", new ShowMapper());
    }

    @Override
    public List<Media> getWatchlist() throws StorageException {
        try {
            List<String> mediaIds = jedis.lrange(WATCHLIST_KEY, 0, -1);
            List<Media> watchlist = new ArrayList<>();

            for (String id : mediaIds) {
                String dataKey = "media:" + id;
                Map<String,String> hash = jedis.hgetAll(dataKey);
                MediaMapper<?> mapper = mapperTypes.get(hash.get("type"));
                Media media = mapper.createEmptyMedia();
                mapper.fromHash(media,hash);
                watchlist.add(media);
            }
            return watchlist;
        } catch(JedisException e){
            throw new StorageException("Internal Server Error");
        }
    }

    @Override
    public  List<Media> getMovieList() throws StorageException {
        try {
            MediaMapper<?> movieMapper = mapperTypes.get("movie");
            Set<String> mediaIds = jedis.smembers(WATCHLIST_KEY + ":movie");
            List<Media> watchlist = new ArrayList<>();

            for (String id : mediaIds) {
                String dataKey = "media:" + id;
                Map<String, String> hash = jedis.hgetAll(dataKey);
                Media media = movieMapper.createEmptyMedia();
                movieMapper.fromHash(media,hash);
                watchlist.add(media);
            }
            return watchlist;
        } catch(JedisException e){
            throw new StorageException("Internal Server Error");
        }

    }

    @Override
    public List<Media> getShowlist() throws StorageException {
        try{
            MediaMapper<?> showMapper = mapperTypes.get("show");
            Set<String> mediaIds = jedis.smembers(WATCHLIST_KEY + ":show");
            List<Media> watchlist = new ArrayList<>();

            for (String id : mediaIds) {
                String dataKey = "media:" + id;
                Map<String, String> hash = jedis.hgetAll(dataKey);
                Media media = showMapper.createEmptyMedia();
                showMapper.fromHash(media,hash);
                watchlist.add(media);
            }
            return watchlist;
        } catch(JedisException e){
            throw new StorageException("Internal Server Error");
        }
    }

    @Override
    public void addMedia(Media media) throws StorageException {
        try{
            long id = jedis.incr(MEDIA_ID_COUNTER);
            String mediaId = "" + id;

            MediaMapper<?> mapper = mapperTypes.get(media.getType());
            Map<String,String> hash = mapper.toHash(media);

            saveMediaData(mediaId, hash);
            addToWatchlists(mediaId, media);
            updateIndexes(mediaId, hash);
        } catch(JedisException e){
            throw new StorageException("Internal Server Error");
        }
    }

    private void saveMediaData(String mediaId, Map<String,String> hash){
        String mediaKey = "media:" + mediaId;
        jedis.hmset(mediaKey, hash);
    }

    private void addToWatchlists(String mediaId, Media media){
        jedis.lpush(WATCHLIST_KEY, mediaId);
        String typeKey = WATCHLIST_KEY + ":" + media.getType();
        jedis.sadd(typeKey, mediaId);
    }

    private void updateIndexes(String mediaId, Map<String,String> hash) {
        //all media
        addStringIndex(GENRE_PREFIX, hash.get("genre"), mediaId);
        addStringIndex(STREAMING_SERVICE_PREFIX, hash.get("platform"), mediaId);
        addNumericIndex(RATING_KEY,hash.get("rating"), mediaId);

        String type = hash.get("type");
        if ("movie".equals(type)) {
            // Only index Movie fields if it IS a movie
            addStringIndex(DIRECTOR_PREFIX, hash.get("director"), mediaId);
            addNumericIndex(RUNTIME_KEY, hash.get("runtime"), mediaId);
            addNumericIndex(RELEASE_YEAR_KEY, hash.get("release"), mediaId);
        } else if ("show".equals(type)) {
            // Only index Show fields if it IS a show
            addNumericIndex(START_YEAR_KEY, hash.get("start"), mediaId);
            addNumericIndex(END_YEAR_KEY, hash.get("end"), mediaId);
            addNumericIndex(SEASONS_KEY, hash.get("seasons"), mediaId);
        }
    }

    private void addStringIndex(String prefix, String value, String mediaId) {
        if(value != null && !value.isEmpty()) {
            String normalizedInput = value.toLowerCase().trim();
            jedis.sadd(prefix + normalizedInput, mediaId);
        }
    }

    private void addNumericIndex(String indexKey, Object value, String mediaId){
        Double numericValue = parseNumericValue(value);

        if (numericValue != null) {
            jedis.zadd(indexKey, numericValue, mediaId);
        }
    }

    private Double parseNumericValue(Object value) {
        if (value == null) {
            return null;
        }

        if (value instanceof Number) {
            return ((Number) value).doubleValue();
        }

        String strVal = value.toString().trim();
        if (strVal.isEmpty() || strVal.equalsIgnoreCase("Present")) {
            return null;
        }

        try {
            return Double.parseDouble(strVal);
        } catch (NumberFormatException e) {
            System.out.println("Could not index numeric value: " + strVal);
            return null;
        }
    }

    /**
     * Retrieves a list of media based on dynamic filter criteria.
     *
     * --- Filtering Strategy ---
     * 1. Identify the Initial Candidate Set:
     * - If a 'genre' is provided, we start with the Redis Set specific to that genre (e.g., "has:genre:Sci-Fi").
     * This is much faster than starting with the entire library.
     * - If no genre is provided, we start with the full "watchlist" Set containing all media IDs.
     *
     * 2. Apply Categorical Filters (Set Intersection):
     * - We iterate through "exact match" filters (Platform, Director).
     * - For each filter, we fetch its corresponding Set from Redis and intersect it
     * with our current candidates (using retainAll). This rapidly reduces the ID list.
     *
     * 3. Apply Numeric/Range Filters (Application Logic):
     * - Range queries (e.g., Rating > 8.0, Year > 2010) are complex to perform via Set intersection.
     * - Therefore, we fetch the full Media objects for the remaining IDs and filter them
     * manually in Java using the 'passesNumericFilters' helper method.
     *
     * @param username The user requesting the suggestion (can be used for future personalization).
     * @param rawCriteria A map of filter keys (e.g., "genre", "rating") and their target values.
     * @return A list of Media objects that satisfy ALL criteria.
     */
    @Override
    public List<Media> getSuggestedMedia(String username, Map<String, String> rawCriteria) throws StorageException {
        Map<String, String> criteria = new HashMap<>();
        for (Map.Entry<String, String> entry : rawCriteria.entrySet()) {
            criteria.put(entry.getKey(), entry.getValue().trim().toLowerCase());
        }

        String tempKeyPrefix = "temp:search:" + username + ":";
        List<String> keysToIntersect = new ArrayList<>();

        if (criteria.containsKey("type")) {
            String type = criteria.get("type");
            keysToIntersect.add(WATCHLIST_KEY + ":" + type);
        } else {
            String allKey = tempKeyPrefix + "all";
            jedis.sunionstore(allKey, WATCHLIST_KEY + ":movie", WATCHLIST_KEY + ":show");
            keysToIntersect.add(allKey);
        }

        if (criteria.containsKey("genre")) {
            String[] genres = criteria.get("genre").split(",");
            String[] redisKeys = new String[genres.length];

            for (int i = 0; i < genres.length; i++) {
                redisKeys[i] = GENRE_PREFIX + genres[i].trim();
            }

            String genreUnionKey = tempKeyPrefix + "genre";
            jedis.sunionstore(genreUnionKey, redisKeys);
            keysToIntersect.add(genreUnionKey);
        }

        if (criteria.containsKey("platform")) {
            String platform = criteria.get("platform");
            keysToIntersect.add(STREAMING_SERVICE_PREFIX + platform.trim());
        }

        if (criteria.containsKey("director")) {
            keysToIntersect.add(DIRECTOR_PREFIX + criteria.get("director").trim());
        }


        Set<String> finalIds;
        if (keysToIntersect.isEmpty()) {
            finalIds = new HashSet<>();
        } else {
            finalIds = jedis.sinter(keysToIntersect.toArray(new String[0]));
        }


        keysToIntersect.stream()
                .filter(k -> k.startsWith("temp:"))
                .forEach(jedis::del);

        List<Media> results = new ArrayList<>();

        for (String id : finalIds) {

            Map<String, String> hash = jedis.hgetAll("media:" + id);
            if (hash == null || hash.isEmpty()) {
                continue;
            }

            String type = hash.get("type");
            MediaMapper<?> mapper = mapperTypes.get(type);
            Media media = mapper.fromHash(mapper.createEmptyMedia(), hash);


            if (!passesNumericFilters(media, criteria)) {
                continue;
            }

            results.add(media);
        }

        return results;
    }


    private boolean passesNumericFilters(Media media, Map<String, String> criteria) {
        try {

            if (criteria.containsKey("rating")) {
                double min = Double.parseDouble(criteria.get("rating"));
                double actual = Double.parseDouble(media.getRating());
                if (actual < min) {
                    return false;
                }
            }


            if (criteria.containsKey("runtime") && media instanceof Movie) {
                int max = Integer.parseInt(criteria.get("runtime"));
                int actual = Integer.parseInt(((Movie) media).getRuntime());
                if (actual > max) {
                    return false;
                }
            }


            if (criteria.containsKey("seasons") && media instanceof Show) {
                int min = Integer.parseInt(criteria.get("seasons"));
                int actual = Integer.parseInt(((Show) media).getSeasons());
                if (actual < min) {
                    return false;
                }
            }


            if (criteria.containsKey("release") && media instanceof Movie) {
                int target = Integer.parseInt(criteria.get("release"));
                int actual = Integer.parseInt(((Movie) media).getRelease());
                return actual >= target;
            }

            return true;
        } catch (NumberFormatException | NullPointerException e) {
            return false;
        }
    }

}