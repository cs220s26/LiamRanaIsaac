package edu.moravian;

import edu.moravian.exceptions.StorageException;
import edu.moravian.process.ProcessManager;

public class BotCommands {
    private final ProcessManager processManager;

    public BotCommands(ProcessManager processManager) {
        this.processManager = processManager;
    }

    public String respond(String username, String msg) {
        try {

            switch (msg) {
                case "!watchlist":
                    return getWatchlist(username);
                case "!add":
                    return startAddMediaDialogue(username);
                case "!suggest":
                    return startSuggestMediaDialogue(username);
                case "!status":
                    return retrieveStatus(username);
                case "!helk":
                    return listCommands();
                default:
                    return handleDialogue(username,msg);
            }
        } catch(StorageException e){
            return "Sorry, Im experiencing an **Internal Server Error**";
        }
    }

    private String listCommands() {
        return "**📺 Watchlist Bot Commands**\n\n" +
                "`!watchlist`   - View your watchlist (Option to filter by Movie/Show).\n" +
                "`!add`    - Add a new Movie or TV Show to the Watchlist.\n" +
                "`!suggest` - Filter the watchlist to find your next watch.\n" +
                "`!status` - Check current status (Process | State).\n" +
                "`!help` - This menu you are viewing right now!";
    }

    private String retrieveStatus(String username) throws StorageException {
        return processManager.getUserStatus(username);
    }

    private String getWatchlist(String username) throws StorageException{
        return processManager.startProcess(username, "view");
    }

    private String startAddMediaDialogue(String username) throws StorageException{
        return processManager.startProcess(username, "add");
    }

    private String startSuggestMediaDialogue(String username) throws StorageException{
        return processManager.startProcess(username, "suggest");
    }

    private String handleDialogue(String username, String msg) throws StorageException{
        return processManager.handleInput(username,msg);
    }
}
