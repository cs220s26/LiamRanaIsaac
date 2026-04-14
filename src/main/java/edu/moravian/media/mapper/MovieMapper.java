package edu.moravian.media.mapper;

import edu.moravian.media.Media;
import edu.moravian.media.Movie;

import java.util.HashMap;
import java.util.Map;

public class MovieMapper extends MediaMapper<Movie> {

    public HashMap<String,String> movieToHash(Movie media) {
        return super.toHash(media);
    }

    public Movie hashToMovie(Movie media, HashMap<String,String> hash) {
        return (Movie) super.fromHash(media,hash);
    }

    @Override
    public Movie createEmptyMedia() {
        return new Movie();
    }

    @Override
    void addSubclassFieldsToHash(Movie movie, Map<String, String> hash) {
        if (movie.getType() != null) hash.put("type", movie.getType());
        if (movie.getRelease() != null) hash.put("release", movie.getRelease());
        if (movie.getRuntime() != null) hash.put("runtime", movie.getRuntime());
        if (movie.getDirector() != null) hash.put("director", movie.getDirector());
    }



    @Override
    void addSubclassHashesToFields(Movie movie, Map<String, String> hash) {
        movie.setRelease(hash.get("release"));
        movie.setRuntime(hash.get("runtime"));
        movie.setDirector(hash.get("director"));
    }

}
