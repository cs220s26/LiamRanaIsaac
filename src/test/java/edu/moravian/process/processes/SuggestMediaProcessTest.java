package edu.moravian.process.processes;

import edu.moravian.exceptions.StorageException;
import edu.moravian.media.Media;
import edu.moravian.media.Movie;
import edu.moravian.process.MemoryProcessStorage;
import edu.moravian.watchlist.WatchlistApp;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class SuggestMediaProcessTest {

    private MemoryProcessStorage storage;
    private SuggestMediaProcess process;
    private MockWatchlistApp mockApp;
    private final String TEST_USER = "SearcherSam";

    // --- Mock App ---
    static class MockWatchlistApp extends WatchlistApp {
        public Map<String, String> lastCriteria;

        public MockWatchlistApp() {
            // Pass null to satisfy super(WatchlistAppStorage)
            super(null);
        }

        @Override
        public List<Media> getSuggestedMedia(String username, Map<String, String> criteria) {
            // Capture the criteria passed by the process so we can verify it in the test
            this.lastCriteria = criteria;

            // Return dummy result so the process completes successfully
            List<Media> dummy = new ArrayList<>();
            dummy.add(new Movie("Found Movie", "10", "Test", "Test", "2023", "100", "Test"));
            return dummy;
        }

        // Stubs for unused methods
        @Override public void addMedia(Media media) {}
        @Override public List<Media> getWatchlist() { return new ArrayList<>(); }
        @Override public List<Media> getMovieList() { return new ArrayList<>(); }
        @Override public List<Media> getShowList() { return new ArrayList<>(); }
    }

    @BeforeEach
    void setUp() {
        storage = new MemoryProcessStorage();
        mockApp = new MockWatchlistApp();
        process = new SuggestMediaProcess(mockApp, storage);
    }

    @Test
    void testSearchByGenre() throws StorageException {
        // 1. Start the process
        process.start(TEST_USER);
        assertEquals(SuggestMediaState.SUGGEST_BASE.name(), storage.getState(TEST_USER));

        // 2. Select Base Type (Movie)
        process.handleInput(TEST_USER, "Movie");
        assertEquals(SuggestMediaState.SUGGEST_FILTERS.name(), storage.getState(TEST_USER));

        // 3. Select Filter Type (Genre)
        // The process detects "Genre" is a valid filter key and asks for its value
        process.handleInput(TEST_USER, "Genre");
        assertEquals(SuggestMediaState.SUGGEST_GENRE.name(), storage.getState(TEST_USER));

        // 4. Provide Filter Value (Horror)
        String result = process.handleInput(TEST_USER, "Horror");

        // 5. Verify Results
        assertTrue(result.contains("Found Movie")); // Ensures getSuggestedMedia was called

        // 6. Verify correct criteria passed to App
        assertNotNull(mockApp.lastCriteria);
        assertEquals("Horror", mockApp.lastCriteria.get("genre"));
    }

    @Test
    void testInvalidFilterInput() throws StorageException {
        // 1. Start Process
        process.start(TEST_USER);

        // 2. Select 'Both' to get to the filter selection step
        process.handleInput(TEST_USER, "Both");
        assertEquals(SuggestMediaState.SUGGEST_FILTERS.name(), storage.getState(TEST_USER));

        // 3. Enter 'none' (which is NOT a valid filter key in your code)
        String result = process.handleInput(TEST_USER, "none");

        // 4. Verify Behavior:
        // Expecting the error message you described: "there has to be valid filters..."
        assertTrue(result.toLowerCase().contains("valid filter"),
                "Response should warn the user that they must enter a valid filter");

        // 5. Verify State:
        // The process should NOT have searched. It should still be waiting for filters.
        assertNull(mockApp.lastCriteria, "Search should NOT be triggered by invalid input");
        assertEquals(SuggestMediaState.SUGGEST_FILTERS.name(), storage.getState(TEST_USER),
                "State should remain in SUGGEST_FILTERS until valid input is given");
    }
}