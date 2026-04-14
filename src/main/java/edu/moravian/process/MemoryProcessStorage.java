package edu.moravian.process;

import edu.moravian.media.Media;

import java.util.HashMap;
import java.util.Map;

public class MemoryProcessStorage implements ProcessStorage {

    private final Map<String, String> processTypes;
    private final Map<String, String> states;
    private final Map<String, Media> mediaSessions;
    private final Map<String, String> pendingFilters;
    private final Map<String, Map<String, String>> activeFilters;

    public MemoryProcessStorage() {
        this.processTypes = new HashMap<>();
        this.states = new HashMap<>();
        this.mediaSessions = new HashMap<>();
        this.pendingFilters = new HashMap<>();
        this.activeFilters = new HashMap<>();
    }

    @Override
    public void setCurrentProcessType(String username, String processType) {
        processTypes.put(username, processType);
    }

    @Override
    public String getCurrentProcessType(String username) {
        return processTypes.get(username);
    }

    @Override
    public void setState(String username, String state) {
        states.put(username, state);
    }

    @Override
    public String getState(String username) {
        return states.get(username);
    }

    @Override
    public void setMediaInProgress(String username, Media media) {
        mediaSessions.put(username, media);
    }

    @Override
    public Media getMediaInProgress(String username) {
        return mediaSessions.get(username);
    }

    @Override
    public void clearProcess(String username) {
        processTypes.remove(username);
        states.remove(username);
        mediaSessions.remove(username);
        pendingFilters.remove(username);
        activeFilters.remove(username);
    }

    @Override
    public void setPendingFilters(String username, String filters) {
        pendingFilters.put(username, filters);
    }

    @Override
    public String getPendingFilters(String username) {
        return pendingFilters.get(username);
    }

    @Override
    public void addFilter(String username, String filter, String value) {
        activeFilters.computeIfAbsent(username, k -> new HashMap<>()).put(filter, value);
    }

    @Override
    public Map<String, String> getFilter(String username) {
        return activeFilters.getOrDefault(username, new HashMap<>());
    }
}
