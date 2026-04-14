package edu.moravian.media.mapper;

import edu.moravian.media.Media;

import java.util.HashMap;
import java.util.Map;

public abstract class MediaMapper<T extends Media> {

    public  HashMap<String,String> toHash(Media media){
        HashMap<String, String> hash = new HashMap<>();
        if (media.getTitle() != null) hash.put("title", media.getTitle());
        if (media.getRating() != null) hash.put("rating", media.getRating());
        if (media.getGenre() != null) hash.put("genre", media.getGenre());
        if (media.getStreamingService() != null) hash.put("platform", media.getStreamingService());
        addSubclassFieldsToHash((T) media,hash);
        return hash;
    }

    public Media fromHash(Media media, Map<String,String> hash){
        media.setTitle(hash.get("title"));
        media.setRating(hash.get("rating"));
        media.setGenre(hash.get("genre"));
        media.setStreamingService(hash.get("platform"));
        addSubclassHashesToFields((T) media,hash);
        return media;
    }

    public abstract T createEmptyMedia();

    abstract void addSubclassFieldsToHash(T media, Map<String,String> hash);

    abstract void addSubclassHashesToFields(T media, Map<String,String> hash);





}
