package edu.moravian.process.processes;

import edu.moravian.exceptions.StorageException;
import edu.moravian.media.Media;
import edu.moravian.media.Movie;
import edu.moravian.media.Show;
import edu.moravian.process.MemoryProcessStorage;
import edu.moravian.watchlist.MemoryStorage;
import edu.moravian.watchlist.WatchlistApp;
import edu.moravian.watchlist.WatchlistAppStorage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class AddMediaProcessTest {

    private MemoryProcessStorage storage;
    private AddMediaProcess process;
    private MockWatchlistApp mockApp;
    private MemoryStorage appStorage;
    private final String TEST_USER = "BuilderBob";

    // --- Mock App to capture the final result ---
    static class MockWatchlistApp extends WatchlistApp {
        public Media lastAddedMedia;

        public MockWatchlistApp(WatchlistAppStorage storage) {
            super(storage);
        }

        @Override
        public void addMedia(Media media) {
            this.lastAddedMedia = media;
        }

        // Stubs for abstract methods not used in this specific test
        public List<Media> getWatchlist() { return new ArrayList<>(); }
        public List<Media> getMovieList() { return new ArrayList<>(); }
        public List<Media> getShowList() { return new ArrayList<>(); }
        public List<Media> getSuggestedMedia(String u, Map<String, String> c) { return new ArrayList<>(); }
    }

    @BeforeEach
    void setUp() {
        storage = new MemoryProcessStorage();
        appStorage = new MemoryStorage();
        mockApp = new MockWatchlistApp(appStorage);
        process = new AddMediaProcess(mockApp, storage);
    }

    @Test
    void testAddMovieScenario() throws StorageException {
        // 1. Start
        String response = process.start(TEST_USER);
        assertNotNull(response);
        assertEquals(AddMediaState.ASK_TYPE.name(), storage.getState(TEST_USER));

        // 2. Select Type
        response = process.handleInput(TEST_USER, "Movie");
        assertEquals(AddMediaState.ASK_TITLE.name(), storage.getState(TEST_USER));

        // 3. Title
        process.handleInput(TEST_USER, "Inception");
        assertEquals(AddMediaState.ASK_GENRE.name(), storage.getState(TEST_USER));

        // 4. Genre
        process.handleInput(TEST_USER, "Sci-Fi");
        assertEquals(AddMediaState.ASK_RATING.name(), storage.getState(TEST_USER));

        // 5. Rating
        process.handleInput(TEST_USER, "10");
        assertEquals(AddMediaState.ASK_STREAMING_SERVICE.name(), storage.getState(TEST_USER));

        // 6. Streaming Service
        process.handleInput(TEST_USER, "HBO");

        // --- CORRECTION BASED ON ERROR LOG ---
        // The app asks for Release Year next, not Director.
        assertEquals(AddMediaState.ASK_RELEASE.name(), storage.getState(TEST_USER));

        // 7. Release Year
        process.handleInput(TEST_USER, "2010");
        // Assuming Runtime comes next (since Director is usually last based on your snippets)
        assertEquals(AddMediaState.ASK_RUNTIME.name(), storage.getState(TEST_USER));

        // 8. Runtime
        process.handleInput(TEST_USER, "148 min");
        // Now it should ask for Director (which is the final step)
        assertEquals(AddMediaState.ASK_DIRECTOR.name(), storage.getState(TEST_USER));

        // 9. Director (Final Step)
        process.handleInput(TEST_USER, "Nolan");

        // 10. Verify Persistence
        // The process should verify the object was added to the App and state cleared
        assertNull(storage.getState(TEST_USER), "Process should be cleared after completion");
        assertNotNull(mockApp.lastAddedMedia, "Media should have been added to the App");

        Movie result = (Movie) mockApp.lastAddedMedia;
        assertEquals("Inception", result.getTitle());
        assertEquals("Nolan", result.getDirector());
        assertEquals("2010", result.getRelease());
    }

    @Test
    void testAddShowScenario() throws StorageException {
        // 1. Start
        process.start(TEST_USER);

        // 2. Select Type: Show
        process.handleInput(TEST_USER, "Show");
        assertInstanceOf(Show.class, storage.getMediaInProgress(TEST_USER));

        // 3. Fill common fields
        process.handleInput(TEST_USER, "The Office"); // Title
        process.handleInput(TEST_USER, "Comedy");     // Genre
        process.handleInput(TEST_USER, "9");          // Rating
        process.handleInput(TEST_USER, "Peacock");    // Platform

        // 4. Verify Branching: Should ask for Seasons now, NOT Director
        assertEquals(AddMediaState.ASK_SEASONS.name(), storage.getState(TEST_USER));

        // 5. Seasons
        process.handleInput(TEST_USER, "9");
        assertEquals(AddMediaState.ASK_START.name(), storage.getState(TEST_USER));

        // 6. Start Year
        process.handleInput(TEST_USER, "2005");
        assertEquals(AddMediaState.ASK_END.name(), storage.getState(TEST_USER));

        // 7. End Year (Final Step)
        process.handleInput(TEST_USER, "2013");

        // 8. Verify
        assertNull(storage.getState(TEST_USER));
        assertNotNull(mockApp.lastAddedMedia);
        assertInstanceOf(Show.class, mockApp.lastAddedMedia);

        Show result = (Show) mockApp.lastAddedMedia;
        assertEquals("The Office", result.getTitle());
        assertEquals("2005", result.getStart());
    }
}