package edu.moravian.watchlist;

import edu.moravian.media.Media;
import edu.moravian.media.Movie;
import edu.moravian.media.Show;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class MemoryStorage implements WatchlistAppStorage {
    private final List<Media> watchlist;

    public MemoryStorage() {
        this.watchlist = new ArrayList<>();
    }

    @Override
    public void addMedia(Media media) {
        watchlist.add(media);
    }

    @Override
    public List<Media> getWatchlist() {
        return new ArrayList<>(watchlist);
    }

    @Override
    public List<Media> getMovieList() {
        return watchlist.stream()
                .filter(media -> "movie".equals(media.getType()))
                .collect(Collectors.toList());
    }

    @Override
    public List<Media> getShowlist() {
        return watchlist.stream()
                .filter(media -> "show".equals(media.getType()))
                .collect(Collectors.toList());
    }

    @Override
    public List<Media> getSuggestedMedia(String username, Map<String, String> rawCriteria) {
        Map<String, String> criteria = new HashMap<>();
        for (Map.Entry<String, String> entry : rawCriteria.entrySet()) {
            criteria.put(entry.getKey(), entry.getValue().trim().toLowerCase());
        }

        return watchlist.stream()
                .filter(media -> matchesCriteria(media, criteria))
                .collect(Collectors.toList());
    }

    /**
     * Replicates the intersection/union logic of RedisStorage.
     * Logic: (Genre A OR Genre B) AND (Type) AND (Platform) AND (Director) AND (NumericFilters)
     */
    private boolean matchesCriteria(Media media, Map<String, String> criteria) {
        if (criteria.containsKey("type")) {
            if (!media.getType().equals(criteria.get("type"))) {
                return false;
            }
        }

        if (criteria.containsKey("platform")) {
            String platform = media.getStreamingService();
            if (platform == null || !platform.trim().toLowerCase().equals(criteria.get("platform"))) {
                return false;
            }
        }

        if (criteria.containsKey("director")) {
            if (media instanceof Movie) {
                String director = ((Movie) media).getDirector();
                if (director == null || !director.trim().toLowerCase().equals(criteria.get("director"))) {
                    return false;
                }
            } else {
                return false;
            }
        }

        if (criteria.containsKey("genre")) {
            String[] targetGenres = criteria.get("genre").split(",");
            String mediaGenre = media.getGenre();

            if (mediaGenre == null) {
                return false;
            }

            String normalizedMediaGenre = mediaGenre.trim().toLowerCase();
            boolean matchesAnyGenre = Arrays.stream(targetGenres)
                    .map(String::trim)
                    .anyMatch(normalizedMediaGenre::equals);

            if (!matchesAnyGenre) {
                return false;
            }
        }

        return passesNumericFilters(media, criteria);
    }

    /**
     * Exact copy of the logic from RedisStorage to ensure consistency in testing.
     */
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
