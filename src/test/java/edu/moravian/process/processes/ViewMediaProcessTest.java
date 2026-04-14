package edu.moravian.process.processes;

import edu.moravian.exceptions.StorageException;
import edu.moravian.media.Media;
import edu.moravian.media.Movie;
import edu.moravian.media.Show;
import edu.moravian.process.MemoryProcessStorage;
import edu.moravian.watchlist.WatchlistApp;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class ViewMediaProcessTest {

    private MemoryProcessStorage storage;
    private ViewMediaProcess process;
    private MockWatchlistApp mockApp;
    private final String TEST_USER = "ViewerVicky";

    // --- Mock App ---
    // Extends WatchlistApp to intercept calls without needing a real Database/Storage
    static class MockWatchlistApp extends WatchlistApp {
        private final List<Media> allMedia = new ArrayList<>();

        public MockWatchlistApp() {
            // Pass null because we are overriding all methods that would use the storage
            super(null);

            // Pre-populate with test data
            allMedia.add(new Movie("Iron Man", "8", "Action", "Disney+", "2008", "126", "Favreau"));
            allMedia.add(new Show("Friends", "7", "Comedy", "Max", "1994", "2004", "10"));
        }

        // We override the methods used by ViewMediaProcess to return our test data
        @Override
        public List<Media> getWatchlist() {
            return new ArrayList<>(allMedia);
        }

        @Override
        public List<Media> getMovieList() {
            List<Media> movies = new ArrayList<>();
            for(Media m : allMedia) {
                if(m instanceof Movie) movies.add(m);
            }
            return movies;
        }

        @Override
        public List<Media> getShowList() {
            List<Media> shows = new ArrayList<>();
            for(Media m : allMedia) {
                if(m instanceof Show) shows.add(m);
            }
            return shows;
        }

        // Stubs for methods not used in this test
        @Override public void addMedia(Media media) {}
        @Override public List<Media> getSuggestedMedia(String u, Map<String, String> c) { return new ArrayList<>(); }
    }

    @BeforeEach
    void setUp() {
        storage = new MemoryProcessStorage();
        mockApp = new MockWatchlistApp();
        process = new ViewMediaProcess(mockApp, storage);
    }

    @Test
    @DisplayName("Scenario: Start View Process")
    void testStart() throws StorageException {
        // 1. Start the process
        String response = process.start(TEST_USER);

        // 2. Verify prompt and state update
        assertTrue(response.contains("Movies, Shows, or All"), "Should ask user what they want to view");
        assertEquals(ViewMediaState.ASK_FILTER.name(), storage.getState(TEST_USER));
    }

    @Test
    @DisplayName("Scenario: View Only Movies")
    void testViewMovies() throws StorageException {
        process.start(TEST_USER);

        // Handle input "movies"
        String result = process.handleInput(TEST_USER, "movies");

        // Verify Iron Man (Movie) is present, but Friends (Show) is not
        assertTrue(result.contains("Iron Man"));
        assertFalse(result.contains("Friends"));

        // Process should clear after showing results
        assertNull(storage.getCurrentProcessType(TEST_USER));
    }

    @Test
    @DisplayName("Scenario: View Only Shows")
    void testViewShows() throws StorageException {
        process.start(TEST_USER);

        // Handle input "shows"
        String result = process.handleInput(TEST_USER, "shows");

        // Verify Friends (Show) is present, but Iron Man (Movie) is not
        assertFalse(result.contains("Iron Man"));
        assertTrue(result.contains("Friends"));
    }

    @Test
    @DisplayName("Scenario: View All Media")
    void testViewAll() throws StorageException {
        process.start(TEST_USER);

        // Handle input "all"
        String result = process.handleInput(TEST_USER, "all");

        // Verify both are present
        assertTrue(result.contains("Iron Man"));
        assertTrue(result.contains("Friends"));
    }
}