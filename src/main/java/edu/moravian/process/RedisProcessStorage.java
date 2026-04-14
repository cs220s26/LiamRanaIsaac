package edu.moravian.process;

import edu.moravian.exceptions.StorageException;
import edu.moravian.media.Media;
import edu.moravian.media.mapper.MediaMapper;
import edu.moravian.media.mapper.MovieMapper;
import edu.moravian.media.mapper.ShowMapper;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.exceptions.JedisException;

import java.util.HashMap;
import java.util.Map;

public class RedisProcessStorage implements ProcessStorage{

    private static final String PROCESS_TYPE_PREFIX = "process:type:";
    private static final String STATE_PREFIX = "process:state:";
    private static final String SESSION_PREFIX = "process:session:";
    private static final String PENDING_FILTERS_PREFIX = "process:pfilters";
    private static final String FILTERS_PREFIX = "process:filters";

    private final Jedis jedis;
    private final Map<String, MediaMapper<? extends Media>> mapperTypes;

    public RedisProcessStorage(String hostname, int port) {
        this.jedis = new Jedis(hostname, port);
        this.mapperTypes = new HashMap<>();
        this.mapperTypes.put("movie", new MovieMapper());
        this.mapperTypes.put("show", new ShowMapper());
    }

    @Override
    public void setCurrentProcessType(String username, String processType) throws StorageException {
        try{
            jedis.set(PROCESS_TYPE_PREFIX + username, processType);
        }
        catch(JedisException e){
            throw new StorageException("Internal Server Error");
        }
    }

    @Override
    public String getCurrentProcessType(String username) throws StorageException{
        try{
            return jedis.get(PROCESS_TYPE_PREFIX + username);
        }
        catch(JedisException e){
            throw new StorageException("Internal Server Error");
        }
    }

    @Override
    public void setState(String username, String state) throws StorageException{
        try{
            jedis.set(STATE_PREFIX + username, state);
        }
        catch(JedisException e){
            throw new StorageException("Internal Server Error");
        }

    }

    @Override
    public String getState(String username) throws StorageException{
        try{
            return jedis.get(STATE_PREFIX + username);
        }
        catch(JedisException e){
            throw new StorageException("Internal Server Error");
        }


    }

    @Override
    public void setMediaInProgress(String username, Media media) throws StorageException{
        try{
            String key = SESSION_PREFIX + username;
            String type = media.getType();
            MediaMapper<?> mapper = mapperTypes.get(type);
            Map<String,String> hash = mapper.toHash(media);
            jedis.hmset(key,hash);
        }
        catch(JedisException e){
            throw new StorageException("Internal Server Error");
        }

    }

    @Override
    public Media getMediaInProgress(String username) throws StorageException{
        try{
            String key = SESSION_PREFIX + username;
            Map<String,String> hash = jedis.hgetAll(key);
            String type = hash.get("type");
            MediaMapper<?> mapper = mapperTypes.get(type);
            Media emptyMedia = mapper.createEmptyMedia();
            return mapper.fromHash(emptyMedia,hash);
        }
        catch(JedisException e){
            throw new StorageException("Internal Server Error");
        }

    }

    @Override
    public void clearProcess(String username) throws StorageException{
        try{
            jedis.del(PROCESS_TYPE_PREFIX + username);
            jedis.del(STATE_PREFIX + username);
            jedis.del(SESSION_PREFIX + username);
            jedis.del(PENDING_FILTERS_PREFIX + username);
            jedis.del(FILTERS_PREFIX + username);
        }
        catch(JedisException e) {
            throw new StorageException("Internal Server Error");
        }
    }

    @Override
    public void setPendingFilters(String username, String filters) throws StorageException{
        try{
            jedis.set(PENDING_FILTERS_PREFIX + username, filters);
        }
        catch(JedisException e) {
            throw new StorageException("Internal Server Error");
        }
    }

    @Override
    public String getPendingFilters(String username) throws StorageException{
        try{
            return jedis.get(PENDING_FILTERS_PREFIX + username);
        }
        catch(JedisException e) {
            throw new StorageException("Internal Server Error");
        }
    }

    @Override
    public void addFilter(String username, String filter, String value) throws StorageException{
        try{
            jedis.hset(FILTERS_PREFIX + username, filter, value);
        }
        catch(JedisException e) {
            throw new StorageException("Internal Server Error");
        }
    }

    @Override
    public Map<String,String> getFilter(String username) throws StorageException{
        try{
            return jedis.hgetAll(FILTERS_PREFIX + username);
        }
        catch(JedisException e) {
            throw new StorageException("Internal Server Error");
        }
    }
}
