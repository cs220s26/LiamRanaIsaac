package edu.moravian.process;

import edu.moravian.exceptions.StorageException;
import edu.moravian.process.processes.BotProcess;

import java.util.HashMap;

public class ProcessManager {
    private final ProcessStorage storage;
    private final HashMap<String, BotProcess> processes;

    public ProcessManager(ProcessStorage storage){
        this.storage = storage;
        this.processes = new HashMap<>();
    }

    public void registerProcess(String processKey, BotProcess process){
        processes.put(processKey,process);
    }

    public String startProcess(String username, String processKey) throws StorageException {
        BotProcess process = processes.get(processKey);
        storage.clearProcess(username);
        storage.setCurrentProcessType(username,processKey);
        storage.setState(username, "START");
        return process.start(username);
    }

    public String handleInput(String username, String msg) throws StorageException {
        String currentProcess = storage.getCurrentProcessType(username);
        if (currentProcess == null) {
            return "";
        }
        BotProcess process = processes.get(currentProcess);
        if (process == null) {
            return "Error: System could not find the process definition for '" + currentProcess + "'.";
        }
        return process.handleInput(username,msg);
    }

    public String getUserStatus(String username) throws StorageException{
        String currentKey = storage.getCurrentProcessType(username);
        String currentStep = storage.getState(username);
        if(currentKey == null){
            return "No state to report.";
        }
        return username + " Process: " + currentKey + " | Step: " + currentStep;
    }
}
