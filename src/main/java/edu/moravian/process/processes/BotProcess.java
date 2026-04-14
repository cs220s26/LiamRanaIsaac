package edu.moravian.process.processes;
import edu.moravian.exceptions.InternalServerException;
import edu.moravian.exceptions.StorageException;
import edu.moravian.watchlist.WatchlistApp;

public abstract class BotProcess {
    private WatchlistApp app;

    public BotProcess(WatchlistApp app){
        this.app = app;
    }

    WatchlistApp getApp(){
        return app;
    }

    public abstract String start(String username) throws StorageException;
    public abstract String handleInput(String username, String msg) throws StorageException;
}
