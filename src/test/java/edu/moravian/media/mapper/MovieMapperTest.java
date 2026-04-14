package edu.moravian.media.mapper;

import edu.moravian.media.Movie;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.HashMap;

import static org.junit.jupiter.api.Assertions.*;

class MovieMapperTest {

    private MovieMapper mapper;

    @BeforeEach
    void setUp() {
        mapper = new MovieMapper();
    }

    @Test
    void testFullData() {
        Movie originalMovie = new Movie("Inception", "PG-13", "Sci-Fi", "Netflix",
                "2010", "148 min", "Christopher Nolan");

        HashMap<String, String> resultMap = mapper.movieToHash(originalMovie);

        assertEquals("Inception", resultMap.get("title"));
        assertEquals("Netflix", resultMap.get("platform")); // Remember: key is "platform", not streamingService
        assertEquals("Christopher Nolan", resultMap.get("director"));
        assertEquals("movie", resultMap.get("type")); // Verify the hardcoded type is injected
        assertEquals("2010", resultMap.get("release"));
    }

    @Test
    void testRoundTrip() {
        Movie originalMovie = new Movie("The Matrix", "R", "Action", "HBO Max",
                "1999", "136 min", "Wachowskis");

        HashMap<String, String> savedHash = mapper.movieToHash(originalMovie);

        Movie loadedMovie = new Movie();
        mapper.hashToMovie(loadedMovie, savedHash);

        assertEquals(originalMovie.getTitle(), loadedMovie.getTitle());
        assertEquals(originalMovie.getDirector(), loadedMovie.getDirector());
        assertEquals(originalMovie.getStreamingService(), loadedMovie.getStreamingService());
        assertEquals(originalMovie.getRuntime(), loadedMovie.getRuntime());
    }

    @Test
    void testNulls() {
        Movie incompleteMovie = new Movie();
        incompleteMovie.setTitle("Unknown Movie");
        incompleteMovie.setDirector(null);

        HashMap<String, String> resultMap = mapper.movieToHash(incompleteMovie);

        assertTrue(resultMap.containsKey("title"));
        assertFalse(resultMap.containsKey("director"), "Map should not contain key for null director");
    }
}