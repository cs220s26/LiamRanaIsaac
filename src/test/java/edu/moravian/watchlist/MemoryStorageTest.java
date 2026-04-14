package edu.moravian.watchlist;

import edu.moravian.media.Media;
import edu.moravian.media.Movie;
import edu.moravian.media.Show;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class MemoryStorageTest {

    private MemoryStorage storage;

    // Test Data Objects
    private Movie actionMovie;
    private Movie scifiMovie;
    private Show comedyShow;
    private Show dramaShow;

    @BeforeEach
    void setUp() {
        storage = new MemoryStorage();

        // Initialize scenarios using the specific constructors from your files
        // Movie: title, rating, genre, platform, release, runtime, director
        actionMovie = new Movie("Mad Max", "8.5", "Action", "Netflix", "2015", "120", "George Miller");
        scifiMovie = new Movie("Inception", "9.0", "Sci-Fi", "Hulu", "2010", "148", "Christopher Nolan");

        // Show: title, rating, genre, platform, start, end, seasons
        comedyShow = new Show("The Office", "8.9", "Comedy", "Peacock", "2005", "2013", "9");
        dramaShow = new Show("Breaking Bad", "9.5", "Drama", "Netflix", "2008", "2013", "5");
    }

    @Test
    void testAddAndRetrieveWatchlist() {
        storage.addMedia(actionMovie);
        storage.addMedia(comedyShow);

        List<Media> result = storage.getWatchlist();

        assertEquals(2, result.size(), "Watchlist should contain 2 items");
        assertTrue(result.contains(actionMovie));
        assertTrue(result.contains(comedyShow));
    }

    @Test
    void testTypeLists() {
        storage.addMedia(actionMovie);
        storage.addMedia(comedyShow);
        storage.addMedia(scifiMovie);

        List<Media> movies = storage.getMovieList();
        List<Media> shows = storage.getShowlist();

        assertEquals(2, movies.size(), "Should return exactly 2 movies");
        assertEquals(1, shows.size(), "Should return exactly 1 show");
        assertTrue(movies.contains(actionMovie));
        assertFalse(movies.contains(comedyShow));
    }

    @Test
    void testSuggestionByGenre() {
        storage.addMedia(actionMovie); // Genre: Action
        storage.addMedia(scifiMovie);  // Genre: Sci-Fi
        storage.addMedia(comedyShow);  // Genre: Comedy

        Map<String, String> criteria = new HashMap<>();
        criteria.put("genre", "action"); // Lowercase search test

        List<Media> results = storage.getSuggestedMedia("user1", criteria);

        assertEquals(1, results.size());
        assertEquals("Action", results.get(0).getGenre());
    }

    @Test
    void testSuggestionByMultipleGenres() {
        storage.addMedia(actionMovie); // Action
        storage.addMedia(scifiMovie);  // Sci-Fi
        storage.addMedia(comedyShow);  // Comedy

        // Search for Action OR Comedy
        Map<String, String> criteria = new HashMap<>();
        criteria.put("genre", "Action, Comedy");

        List<Media> results = storage.getSuggestedMedia("user1", criteria);

        assertEquals(2, results.size(), "Should find both Action and Comedy media");
        assertTrue(results.contains(actionMovie));
        assertTrue(results.contains(comedyShow));
        assertFalse(results.contains(scifiMovie));
    }

    @Test
    void testSuggestionByPlatform() {
        storage.addMedia(actionMovie); // Netflix
        storage.addMedia(scifiMovie);  // Hulu
        storage.addMedia(dramaShow);   // Netflix

        Map<String, String> criteria = new HashMap<>();
        criteria.put("platform", "Netflix");

        List<Media> results = storage.getSuggestedMedia("user1", criteria);

        assertEquals(2, results.size());
        assertTrue(results.stream().allMatch(m -> m.getStreamingService().equals("Netflix")));
    }

    @Test
    void testSuggestionByRating() {
        // actionMovie: 8.5
        // scifiMovie: 9.0
        // comedyShow: 8.9
        storage.addMedia(actionMovie);
        storage.addMedia(scifiMovie);
        storage.addMedia(comedyShow);

        Map<String, String> criteria = new HashMap<>();
        criteria.put("rating", "8.9"); // Should include 8.9 and higher

        List<Media> results = storage.getSuggestedMedia("user1", criteria);

        assertEquals(2, results.size());
        assertTrue(results.contains(scifiMovie)); // 9.0
        assertTrue(results.contains(comedyShow)); // 8.9
        assertFalse(results.contains(actionMovie)); // 8.5 (too low)
    }

    @Test
    void testSuggestionByRuntime() {
        // actionMovie: 120 mins
        // scifiMovie: 148 mins
        storage.addMedia(actionMovie);
        storage.addMedia(scifiMovie);

        Map<String, String> criteria = new HashMap<>();
        criteria.put("runtime", "130"); // Max runtime 130

        List<Media> results = storage.getSuggestedMedia("user1", criteria);

        assertEquals(1, results.size());
        assertTrue(results.contains(actionMovie));
        assertFalse(results.contains(scifiMovie)); // 148 is > 130
    }

    @Test
    void testSuggestionBySeasons() {
        // comedyShow: 9 seasons
        // dramaShow: 5 seasons
        storage.addMedia(comedyShow);
        storage.addMedia(dramaShow);

        Map<String, String> criteria = new HashMap<>();
        criteria.put("seasons", "8"); // Minimum 8 seasons

        List<Media> results = storage.getSuggestedMedia("user1", criteria);

        assertEquals(1, results.size());
        assertTrue(results.contains(comedyShow)); // 9 >= 8
        assertFalse(results.contains(dramaShow)); // 5 < 8
    }

    @Test
    void testSuggestionComplex() {
        // Nolan movie (9.0) vs Miller movie (8.5)
        storage.addMedia(actionMovie);
        storage.addMedia(scifiMovie);

        Map<String, String> criteria = new HashMap<>();
        criteria.put("director", "Christopher Nolan");
        criteria.put("rating", "8.0");

        List<Media> results = storage.getSuggestedMedia("user1", criteria);

        assertEquals(1, results.size());
        assertEquals("Christopher Nolan", ((Movie)results.get(0)).getDirector());
    }

    @Test
    void testInvalidNumericInput() {
        storage.addMedia(actionMovie);

        Map<String, String> criteria = new HashMap<>();
        criteria.put("rating", "invalid-number");

        // MemoryStorage catches NumberFormatException and returns false for that filter
        List<Media> results = storage.getSuggestedMedia("user1", criteria);

        assertTrue(results.isEmpty(), "Should return empty list if filter parsing fails");
    }
}