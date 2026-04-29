package edu.moravian.process.processes;

import edu.moravian.exceptions.StorageException;
import edu.moravian.media.Media;
import edu.moravian.media.Movie;
import edu.moravian.media.Show;
import edu.moravian.process.ProcessStorage;
import edu.moravian.watchlist.WatchlistApp;

import java.util.List;

public class ViewMediaProcess extends BotProcess {

    private final ProcessStorage storage;

    public ViewMediaProcess(WatchlistApp app, ProcessStorage storage) {
        super(app);
        this.storage = storage;
    }

    @Override
    public String start(String username) throws StorageException{
        storage.setState(username, ViewMediaState.ASK_FILTER.name());
        return "What would you like to view?(Movies, Shows, or All)";
    }

    @Override
    public String handleInput(String username, String msg) throws StorageException {
        String type = msg.trim().toLowerCase();

        List<Media> results = assignList(type);
        String watchlist = formatList(results);

        storage.clearProcess(username);
        return watchlist;
    }

    private List<Media> assignList(String type) throws StorageException {
        WatchlistApp app = getApp();
        if(type.contains("movie")) {
            return app.getMovieList();
        } else if(type.contains("show")) {
            return app.getShowList();
        } else {
            return app.getWatchlist();
        }
    }

    private String formatList(List<Media> watchlist) {
        StringBuilder sb = new StringBuilder();
        sb.append("**Watchlist:**\n\n");

        if (watchlist.isEmpty()) {
            return "Your watchlist is currently empty.";
        }

        for (Media media : watchlist) {
            // --- Header: Title and Rating ---
            sb.append("**").append(media.getTitle()).append("**");
            sb.append(" (").append(media.getRating()).append("/10)\n");

            // --- Common Attributes ---
            sb.append("* Genre: ").append(media.getGenre()).append("\n");
            sb.append("* Platform: ").append(media.getStreamingService()).append("\n");

            // --- Dynamic Attributes based on Type ---
            if (media.getType().equals("movie")) {
                Movie movie = (Movie) media;
                sb.append("* Director: ").append(movie.getDirector()).append("\n");
                sb.append("* Runtime: ").append(movie.getRuntime()).append(" mins\n");
                sb.append("* Release Year: ").append(movie.getRelease()).append("\n");
            } else if (media.getType().equals("show")) {
                Show show = (Show) media;
                sb.append("* Seasons: ").append(show.getSeasons()).append("\n");
                sb.append("* Run: ").append(show.getStart()).append(" - ").append(show.getEnd()).append("\n");
            }
            sb.append("\n\n");
        }

        return sb.toString();
    }
}
