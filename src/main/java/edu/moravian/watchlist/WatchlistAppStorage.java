package edu.moravian.watchlist;

import edu.moravian.exceptions.StorageException;
import edu.moravian.media.Media;

import java.util.List;
import java.util.Map;

public interface WatchlistAppStorage {

    List<Media> getWatchlist() throws StorageException;

    List<Media> getMovieList() throws StorageException;

    List<Media> getShowlist() throws StorageException;

    void addMedia(Media media) throws StorageException;

    List<Media> getSuggestedMedia(String username, Map<String, String> filters) throws StorageException;
}
