package edu.moravian.watchlist;
import edu.moravian.exceptions.StorageException;
import edu.moravian.media.Media;

import java.util.List;
import java.util.Map;

public class WatchlistApp {
    private final WatchlistAppStorage storage;

    public WatchlistApp(WatchlistAppStorage storage){
        this.storage = storage;
    }

    public void addMedia(Media media) throws StorageException {
        storage.addMedia(media);
    }

    public List<Media> getWatchlist() throws StorageException{
        return storage.getWatchlist();
    }

    public List<Media> getMovieList() throws StorageException{
        return storage.getMovieList();
    }

    public List<Media> getShowList() throws StorageException{
        return storage.getShowlist();
    }

    public List<Media> getSuggestedMedia(String username, Map<String,String> filters) throws StorageException{
        return storage.getSuggestedMedia(username,filters);
    }
}
