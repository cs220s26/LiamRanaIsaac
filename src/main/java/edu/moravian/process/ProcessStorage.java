package edu.moravian.process;

import edu.moravian.exceptions.StorageException;
import edu.moravian.media.Media;

import java.util.Map;

public interface ProcessStorage {

    void setCurrentProcessType(String username, String processType) throws StorageException;

    String getCurrentProcessType(String username) throws StorageException;

    void setState(String username, String state) throws StorageException;

    String getState(String username) throws StorageException;

    void setMediaInProgress(String username, Media media) throws StorageException;

    Media getMediaInProgress(String username) throws StorageException;

    void clearProcess(String username) throws StorageException;

    public void setPendingFilters(String username, String filters) throws StorageException;

    public String getPendingFilters(String username) throws StorageException;

    public void addFilter(String username, String filter, String value) throws StorageException;

    public Map<String,String> getFilter(String username) throws StorageException;
}
