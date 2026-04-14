package edu.moravian.process;

import edu.moravian.media.Movie;
import edu.moravian.media.Show;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class MemoryProcessStorageTest {

    private MemoryProcessStorage storage;
    private final String TEST_USER = "testUser123";

    @BeforeEach
    void setUp() {
        storage = new MemoryProcessStorage();
    }

    @Test
    void testStateManagement() {
        storage.setCurrentProcessType(TEST_USER, "onboarding");
        assertEquals("onboarding", storage.getCurrentProcessType(TEST_USER));

        storage.setState(TEST_USER, "ASK_NAME");
        assertEquals("ASK_NAME", storage.getState(TEST_USER));
    }

    @Test
    void testMediaSessionMovie() {
        Movie movie = new Movie("Inception", "PG-13", "Sci-Fi", "Netflix", "2010", "148m", "Nolan");

        storage.setMediaInProgress(TEST_USER, movie);

        var result = storage.getMediaInProgress(TEST_USER);
        assertNotNull(result);
        assertInstanceOf(Movie.class, result);
        assertEquals("Inception", result.getTitle());
    }

    @Test
    void testMediaSessionShow() {
        Show show = new Show("Breaking Bad", "TV-MA", "Crime", "Netflix", "2008", "2013", "5");

        storage.setMediaInProgress(TEST_USER, show);

        var result = storage.getMediaInProgress(TEST_USER);

        assertNotNull(result);
        assertInstanceOf(Show.class, result, "The retrieved object should be an instance of Show");

        Show retrievedShow = (Show) result;
        assertEquals("Breaking Bad", retrievedShow.getTitle());
        assertEquals("5", retrievedShow.getSeasons());
    }

    @Test
    void testFilters() {
        storage.setPendingFilters(TEST_USER, "genre,director");
        assertEquals("genre,director", storage.getPendingFilters(TEST_USER));

        storage.addFilter(TEST_USER, "genre", "Action");
        storage.addFilter(TEST_USER, "director", "Nolan");

        Map<String, String> filters = storage.getFilter(TEST_USER);
        assertEquals(2, filters.size());
        assertEquals("Action", filters.get("genre"));
    }

    @Test
    void testClearProcess() {
        storage.setCurrentProcessType(TEST_USER, "search");
        storage.addFilter(TEST_USER, "year", "2023");

        storage.clearProcess(TEST_USER);

        assertNull(storage.getCurrentProcessType(TEST_USER));
        assertTrue(storage.getFilter(TEST_USER).isEmpty());
    }
}