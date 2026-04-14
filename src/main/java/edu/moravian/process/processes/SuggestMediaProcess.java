package edu.moravian.process.processes;

import edu.moravian.exceptions.StorageException;
import edu.moravian.media.Media;
import edu.moravian.process.ProcessStorage;
import edu.moravian.watchlist.WatchlistApp;

import java.util.*;

public class SuggestMediaProcess extends BotProcess{
    private final ProcessStorage storage;

    public SuggestMediaProcess(WatchlistApp app, ProcessStorage storage){
        super(app);
        this.storage = storage;
    }

    private SuggestMediaState getState(String username) throws StorageException{
        String state = storage.getState(username);
        if(state == null){
            return SuggestMediaState.NOT_ACTIVE;
        }
        return SuggestMediaState.valueOf(state);
    }

    @Override
    public String start(String username) throws StorageException {
        storage.setState(username, SuggestMediaState.SUGGEST_BASE.name());
        storage.setPendingFilters(username, "");
        return "Are you looking to watch a movie, show or both?";
    }

    @Override
    public String handleInput(String username, String msg) throws StorageException{
        switch(getState(username)) {
            case SUGGEST_BASE:
                return processBase(username, msg);
            case SUGGEST_FILTERS:
                return processFilterSelection(username, msg);

            // Value Processors
            case SUGGEST_GENRE:
                return processValue(username, "genre", msg);
            case SUGGEST_RATING:
                return processValue(username, "rating", msg);
            case SUGGEST_PLATFORM:
                return processValue(username, "platform", msg);
            case SUGGEST_DIRECTOR:
                return processValue(username, "director", msg);
            case SUGGEST_RUNTIME:
                return processValue(username, "runtime", msg);
            case SUGGEST_RELEASE:
                return processValue(username, "release", msg);
            case SUGGEST_SEASONS:
                return processValue(username, "seasons", msg);
            case SUGGEST_START:
                return processValue(username, "start", msg);
            case SUGGEST_END:
                return processValue(username, "end", msg);

            default:
                return "Unknown state.";
        }
    }

    private String processBase(String username, String msg) throws StorageException{
        String choice = msg.trim().toLowerCase();

        if(choice.contains("movie")) {
            storage.addFilter(username, "type", "movie");
            storage.setState(username, SuggestMediaState.SUGGEST_FILTERS.name());
            return "Searching **Movies**. What do you want to filter by?\n" +
                    "(Options: Genre, Rating, Platform, Director, Runtime, Release)";
        }
        else if (choice.contains("show")) {
            storage.addFilter(username, "type", "show");
            storage.setState(username, SuggestMediaState.SUGGEST_FILTERS.name());
            return "Searching **TV Shows**. What do you want to filter by?\n" +
                    "(Options: Genre, Rating, Platform, Seasons, Start Year, End Year)";
        }
        else {
            // "Both" or invalid input treated as global search
            // We don't set a "type" filter here.
            storage.setState(username, SuggestMediaState.SUGGEST_FILTERS.name());
            return "Searching **All Media**. What do you want to filter by?\n" +
                    "(Options: Genre, Rating, Platform)";
        }
    }

    private String processFilterSelection(String username, String msg) throws StorageException{
        String[] inputs = msg.split(",");
        Queue<String> queue = new LinkedList<>();

        Map<String, String> currentFilters = storage.getFilter(username);
        String type = currentFilters.getOrDefault("type", "all");

        for (String s : inputs) {
            String raw = s.trim().toUpperCase();
            String filter = normalizeKey(raw);

            // Validation: Skip filters that don't match the selected type
            if (filter.equals("DIRECTOR") && type.equals("show")) continue;
            if (filter.equals("RUNTIME") && type.equals("show")) continue;
            if (filter.equals("RELEASE") && type.equals("show")) continue;

            if (filter.equals("SEASONS") && type.equals("movie")) continue;
            if (filter.equals("START") && type.equals("movie")) continue;
            if (filter.equals("END") && type.equals("movie")) continue;

            // Map string input to Enum name checking
            if (isValidFilter(filter)) {
                queue.add(filter);
            }
        }

        if (queue.isEmpty()) {
            return "Please enter valid filters for your selection (e.g., Genre, Rating).";
        }

        // Save the queue to Redis
        storage.setPendingFilters(username, String.join(",", queue));

        // Start the first question
        return nextStep(username);
    }

    private String nextStep(String username) throws StorageException{
        String pending = storage.getPendingFilters(username);

        // If no more filters, run the search
        if (pending == null || pending.isEmpty()) {
            return performSearch(username);
        }

        // Pop the first item
        String[] parts = pending.split(",");
        String currentFilter = parts[0]; // The one we do now

        // Save the rest back
        List<String> remaining = new LinkedList<>(Arrays.asList(parts));
        remaining.removeFirst();
        storage.setPendingFilters(username, String.join(",", remaining));

        // Transition to the specific question
        switch (currentFilter) {
            case "GENRE":
                storage.setState(username, SuggestMediaState.SUGGEST_GENRE.name());
                return "Which **Genre(s)**? (comma separated)";
            case "RATING":
                storage.setState(username, SuggestMediaState.SUGGEST_RATING.name());
                return "Minimum **Rating**? (1-10)";
            case "PLATFORM":
                storage.setState(username, SuggestMediaState.SUGGEST_PLATFORM.name());
                return "Which **Streaming Service**?";
            case "DIRECTOR":
                storage.setState(username, SuggestMediaState.SUGGEST_DIRECTOR.name());
                return "Which **Director**?";
            case "RUNTIME":
                storage.setState(username, SuggestMediaState.SUGGEST_RUNTIME.name());
                return "Max **Runtime** (minutes)?";
            case "RELEASE":
                storage.setState(username, SuggestMediaState.SUGGEST_RELEASE.name());
                return "Release Year?";
            case "SEASONS":
                storage.setState(username, SuggestMediaState.SUGGEST_SEASONS.name());
                return "Minimum number of **Seasons**?";
            case "START":
                storage.setState(username, SuggestMediaState.SUGGEST_START.name());
                return "Start Year?";
            case "END":
                storage.setState(username, SuggestMediaState.SUGGEST_END.name());
                return "End Year?";
            default:
                return nextStep(username); // Skip if somehow invalid
        }
    }

    private String processValue(String username, String key, String value) throws StorageException{
        storage.addFilter(username, key, value);
        return nextStep(username);
    }

    private String performSearch(String username) throws StorageException{
        Map<String, String> criteria = storage.getFilter(username);

        // Ensure you updated WatchlistApp/Storage to accept 'username' as discussed
        List<Media> results = getApp().getSuggestedMedia(username, criteria);

        storage.clearProcess(username);

        if (results.isEmpty()) {
            return "No media found matching your criteria.";
        }

        return formatResults(results);
    }

    private boolean isValidFilter(String s) {
        try {
            SuggestMediaState.valueOf("SUGGEST_" + s);
            return true;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    private String formatResults(List<Media> list) {
        StringBuilder sb = new StringBuilder();
        sb.append("**Search Results").append(":**\n\n");

        for (Media m : list) {
            sb.append("**").append(m.getTitle()).append("**");
            sb.append(" (").append(m.getRating()).append("/10) - ");
            sb.append(m.getStreamingService()).append("\n");
        }

        return sb.toString();
    }

    private String normalizeKey(String raw) {
        // Handle Plurals
        if (raw.equals("GENRES")) return "GENRE";
        if (raw.equals("RATINGS")) return "RATING";
        if (raw.equals("DIRECTORS")) return "DIRECTOR";
        if (raw.equals("STREAMING_SERVICE") || raw.equals("STREAMING SERVICE") || raw.equals("PLATFORMS")) {
            return "PLATFORM";
        }

        return raw;
    }

}
