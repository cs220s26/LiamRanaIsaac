package edu.moravian.process.processes;

import edu.moravian.exceptions.StorageException;
import edu.moravian.media.Media;
import edu.moravian.media.Movie;
import edu.moravian.media.Show;
import edu.moravian.process.ProcessStorage;
import edu.moravian.watchlist.WatchlistApp;

public class AddMediaProcess extends BotProcess {
    private final ProcessStorage storage;

    public AddMediaProcess(WatchlistApp app, ProcessStorage storage){
        super(app);
        this.storage = storage;
    }

    private AddMediaState getState(String username) throws StorageException{
        String state = storage.getState(username);
        if(state == null){
            return AddMediaState.NOT_ACTIVE;
        }
        return AddMediaState.valueOf(state);
    }

    @Override
    public String start(String username) throws StorageException{
        startMediaCreation(username);
        return "Hello " + username + "would you like to add a Movie or Show?";
    }

    @Override
    public String handleInput(String username, String msg) throws StorageException {
        switch (getState(username)) {
            case AddMediaState.NOT_ACTIVE:
                return "";
            case AddMediaState.ASK_TYPE:
                processType(username,msg.toLowerCase());
                return " What is the title " + username + "?";
            case AddMediaState.ASK_TITLE:
                processTitle(username,msg);
                return "Got it" + username + ". Now, what is the genre? (e.g., Sci-Fi, Comedy)";
            case AddMediaState.ASK_GENRE:
                processGenre(username,msg);
                return "Okay " + username + ", What rating was it given? \n Enter an integer 1 - 10:";
            case AddMediaState.ASK_RATING:
                processRating(username,msg);
                return username + " Which streaming service is this available on (e.g., Netflix, Hulu)?";
            case AddMediaState.ASK_STREAMING_SERVICE:
                processStreamingService(username,msg);
                Media media = storage.getMediaInProgress(username);
                if(media.getType().equals("movie")){
                    return username + " What year was the movie Released?";
                } else {
                    return username + " How many seasons are there?";
                }

            // MOVIE STATE
            case AddMediaState.ASK_RELEASE:
                processRelease(username, msg);
                return username + " What is the runtime (in minutes)?";

            case AddMediaState.ASK_RUNTIME:
                processRuntime(username, msg);
                return username + " Who directed the movie?";

            case AddMediaState.ASK_DIRECTOR:
                processDirector(username, msg);
                finalizeMediaCreation(username);
                return username + " Movie added successfully! You can view it with `!watchlist`.";

            // SHOW STATE
            case AddMediaState.ASK_SEASONS:
                processSeasons(username, msg);
                return username + " What year did the show start?";

            case AddMediaState.ASK_START:
                processStart(username, msg);
                return username + " What year did the show end? (Type 'Present' if ongoing)";

            case AddMediaState.ASK_END:
                processEnd(username, msg);
                finalizeMediaCreation(username);
                return username + " Show added successfully! You can view it with `!watchlist`.";
            default:
                return "Weird... Could not find state";
        }
    }

    private void startMediaCreation(String username) throws StorageException{
        storage.setState(username, AddMediaState.ASK_TYPE.name());
    }

    private void processType(String username, String type) throws StorageException{
        Media media;

        if(type.equals("movie")){
            media = new Movie();
        } else if(type.equals("show")){
            media = new Show();
        } else{
            throw new IllegalArgumentException("Unknown type specified: " + type);
        }

        storage.setMediaInProgress(username,media);
        storage.setState(username, AddMediaState.ASK_TITLE.name());
    }

    private void processTitle(String username, String title) throws StorageException{
        Media media = storage.getMediaInProgress(username);
        media.setTitle(title);
        storage.setMediaInProgress(username,media);
        storage.setState(username, AddMediaState.ASK_GENRE.name());
    }

    private void processGenre(String username, String genre) throws StorageException{
        Media media = storage.getMediaInProgress(username);
        media.setGenre(genre);
        storage.setMediaInProgress(username,media);
        storage.setState(username, AddMediaState.ASK_RATING.name());
    }

    private void processRating(String username, String rating) throws StorageException{
        Media media = storage.getMediaInProgress(username);
        media.setRating(rating);
        storage.setMediaInProgress(username,media);
        storage.setState(username, AddMediaState.ASK_STREAMING_SERVICE.name());
    }

    private void processStreamingService(String username, String streamingService) throws StorageException{
        Media media = storage.getMediaInProgress(username);
        media.setStreamingService(streamingService);
        storage.setMediaInProgress(username,media);

        if(media.getType().equals("movie")){
            storage.setState(username, AddMediaState.ASK_RELEASE.name());
        } else if(media.getType().equals("show")){
            storage.setState(username, AddMediaState.ASK_SEASONS.name());
        }

    }

    // MOVIE SPECIFIC FILTERS

    private void processRelease(String username, String release) throws StorageException{
        Movie movie = (Movie) storage.getMediaInProgress(username);
        movie.setRelease(release);
        storage.setMediaInProgress(username, movie);
        storage.setState(username, AddMediaState.ASK_RUNTIME.name());
    }

    private void processRuntime(String username, String runtime) throws StorageException{
        Movie movie = (Movie) storage.getMediaInProgress(username);
        movie.setRuntime(runtime);
        storage.setMediaInProgress(username, movie);
        storage.setState(username, AddMediaState.ASK_DIRECTOR.name());
    }

    private void processDirector(String username, String director) throws StorageException{
        Movie movie = (Movie) storage.getMediaInProgress(username);
        movie.setDirector(director);
        storage.setMediaInProgress(username, movie);
        storage.setState(username, AddMediaState.FINALIZE.name());
    }

    // SHOW SPECIFIC FILTERS

    private void processSeasons(String username, String seasons) throws StorageException{
        Show show = (Show) storage.getMediaInProgress(username);
        show.setSeasons(seasons);
        storage.setMediaInProgress(username, show);
        storage.setState(username, AddMediaState.ASK_START.name());
    }

    private void processStart(String username, String start) throws StorageException{
        Show show = (Show) storage.getMediaInProgress(username);
        show.setStart(start);
        storage.setMediaInProgress(username, show);
        storage.setState(username, AddMediaState.ASK_END.name());
    }

    private void processEnd(String username, String end) throws StorageException{
        Show show = (Show) storage.getMediaInProgress(username);
        show.setEnd(end);
        storage.setMediaInProgress(username, show);
        storage.setState(username, AddMediaState.FINALIZE.name());
    }

    private void finalizeMediaCreation(String username) throws StorageException{
        Media media = storage.getMediaInProgress(username);
        getApp().addMedia(media);
        storage.clearProcess(username);
    }
}
