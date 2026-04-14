package edu.moravian.watchlist;

import edu.moravian.exceptions.StorageException;
import edu.moravian.media.Media;
import edu.moravian.media.Movie;
import edu.moravian.media.Show;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class WatchlistAppTest {

    private WatchlistApp app;
    private WatchlistAppStorage storage;

    @BeforeEach
    void setUp() {
        // Use MemoryStorage as the concrete implementation to test the App wrapper
        storage = new MemoryStorage();
        app = new WatchlistApp(storage);
    }

    @Test
    void testAppAddAndRetrieve() throws StorageException {
        // Movie: title, rating, genre, platform, release, runtime, director
        Movie m = new Movie("Test Movie", "5.0", "Action", "Netflix", "2023", "100", "Director X");

        app.addMedia(m);

        List<Media> result = app.getWatchlist();

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("Test Movie", result.get(0).getTitle());
    }

    @Test
    void testAppGetSpecificLists() throws StorageException {
        // Using correct constructors
        Movie m = new Movie("M1", "1.0", "G", "P", "2020", "100", "Dir");
        Show s = new Show("S1", "1.0", "G", "P", "2020", "2021", "1");

        app.addMedia(m);
        app.addMedia(s);

        assertEquals(1, app.getMovieList().size());
        assertEquals(1, app.getShowList().size());
    }

    @Test
    void testAppSuggestedMedia() throws StorageException {
        // Movie: title, rating, genre, platform, release, runtime, director
        Movie m = new Movie("Scary Movie", "5.0", "Horror", "Hulu", "2000", "90", "Wayans");

        app.addMedia(m);

        Map<String, String> criteria = new HashMap<>();
        criteria.put("genre", "Horror");

        List<Media> suggestions = app.getSuggestedMedia("user123", criteria);

        assertFalse(suggestions.isEmpty(), "App should pass criteria correctly to storage");
        assertEquals("Horror", suggestions.get(0).getGenre());
    }
}