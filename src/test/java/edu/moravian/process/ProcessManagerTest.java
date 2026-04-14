package edu.moravian.process;

import edu.moravian.exceptions.StorageException;
import edu.moravian.process.processes.BotProcess;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ProcessManagerTest {

    private MemoryProcessStorage storage;
    private ProcessManager manager;
    private MockBotProcess mockSearchProcess;
    private final String TEST_USER = "UserB";

    /**
     * Internal Mock class to isolate ProcessManager testing.
     * We pass null to the super constructor because ProcessManager
     * doesn't use WatchlistApp; it only delegates to the BotProcess.
     */
    static class MockBotProcess extends BotProcess {

        public MockBotProcess() {
            super(null); // Satisfies the BotProcess(WatchlistApp) constructor
        }

        @Override
        public String start(String username) {
            return "Welcome to Search! Please enter query.";
        }

        @Override
        public String handleInput(String username, String msg) {
            return "Results for: " + msg;
        }
    }

    @BeforeEach
    void setUp() {
        // Use Memory storage for fast, isolated testing
        storage = new MemoryProcessStorage();
        manager = new ProcessManager(storage);

        // Register our mock process so the manager can find it
        mockSearchProcess = new MockBotProcess();
        manager.registerProcess("SEARCH_PROCESS", mockSearchProcess);
    }

    @Test
    void testStartProcess() throws StorageException {
        // 1. Start the process via the Manager
        String response = manager.startProcess(TEST_USER, "SEARCH_PROCESS");

        // 2. Verify the Manager received the start message from our Mock
        assertEquals("Welcome to Search! Please enter query.", response);

        // 3. Verify Manager correctly updated the Storage state
        assertEquals("SEARCH_PROCESS", storage.getCurrentProcessType(TEST_USER));
        assertEquals("START", storage.getState(TEST_USER));
    }

    @Test
    void testHandleInput() throws StorageException {
        // 1. Setup: User must be in a process to handle input
        manager.startProcess(TEST_USER, "SEARCH_PROCESS");

        // 2. Handle Input
        String response = manager.handleInput(TEST_USER, "The Matrix");

        // 3. Verify the Manager routed the input to our Mock correctly
        assertEquals("Results for: The Matrix", response);
    }

    @Test
    void testHandleInputNoActiveProcess() throws StorageException {
        // 1. Ensure storage is empty (user is not in a flow)
        storage.clearProcess(TEST_USER);

        // 2. Attempt to handle input
        String response = manager.handleInput(TEST_USER, "Hello?");

        // 3. Manager should return empty string if no process is found
        assertEquals("", response);
    }

    @Test
    void testHandleInputMissingDefinition() throws StorageException {
        // 1. Corrupt storage: User is assigned a process key that the Manager doesn't know about
        storage.setCurrentProcessType(TEST_USER, "UNKNOWN_PROCESS");

        // 2. Attempt to handle input
        String response = manager.handleInput(TEST_USER, "Help");

        // 3. Manager should return a specific error message
        assertTrue(response.startsWith("Error: System could not find the process definition"));
    }

    @Test
    void testGetUserStatus() throws StorageException {
        // Case A: No state
        assertEquals("No state to report.", manager.getUserStatus(TEST_USER));

        // Case B: Active state
        manager.startProcess(TEST_USER, "SEARCH_PROCESS");

        // Manually move step forward to verify Manager reads from storage
        storage.setState(TEST_USER, "WAITING_FOR_SELECTION");

        String status = manager.getUserStatus(TEST_USER);

        // Expected format: "username Process: key | Step: state"
        assertEquals(TEST_USER + " Process: SEARCH_PROCESS | Step: WAITING_FOR_SELECTION", status);
    }
}