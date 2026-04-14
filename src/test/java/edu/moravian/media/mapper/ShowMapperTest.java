package edu.moravian.media.mapper;

import edu.moravian.media.Show;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.HashMap;

import static org.junit.jupiter.api.Assertions.*;

class ShowMapperTest {

    private ShowMapper mapper;

    @BeforeEach
    void setUp() {
        mapper = new ShowMapper();
    }

    @Test
    void testToHash() {
        Show originalShow = new Show("Breaking Bad", "TV-MA", "Crime", "Netflix",
                "2008", "2013", "5 Seasons");

        HashMap<String, String> resultMap = mapper.showToHash(originalShow);

        assertEquals("Breaking Bad", resultMap.get("title"));
        assertEquals("Netflix", resultMap.get("platform")); // Superclass field mapping
        assertEquals("5 Seasons", resultMap.get("seasons")); // Subclass field mapping
        assertEquals("show", resultMap.get("type")); // Verify type injection
        assertEquals("2008", resultMap.get("start"));
    }

    @Test
    void testRoundTrip() {
        Show originalShow = new Show("The Office", "TV-14", "Comedy", "Peacock",
                "2005", "2013", "9 Seasons");

        HashMap<String, String> savedHash = mapper.showToHash(originalShow);

        Show loadedShow = new Show();
        mapper.hashToShow(loadedShow, savedHash);

        assertEquals(originalShow.getTitle(), loadedShow.getTitle());
        assertEquals(originalShow.getSeasons(), loadedShow.getSeasons());
        assertEquals(originalShow.getStreamingService(), loadedShow.getStreamingService());
        assertEquals(originalShow.getStart(), loadedShow.getStart());
    }

    @Test
    void testNulls() {
        Show incompleteShow = new Show();
        incompleteShow.setTitle("Mystery Show");
        incompleteShow.setSeasons(null);
        incompleteShow.setEnd(null);

        HashMap<String, String> resultMap = mapper.showToHash(incompleteShow);

        assertTrue(resultMap.containsKey("title"));
        assertFalse(resultMap.containsKey("seasons"), "Map should not contain key for null seasons");
        assertFalse(resultMap.containsKey("end"), "Map should not contain key for null end date");

        assertEquals("show", resultMap.get("type"));
    }
}