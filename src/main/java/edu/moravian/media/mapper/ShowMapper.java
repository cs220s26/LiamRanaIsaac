package edu.moravian.media.mapper;

import edu.moravian.media.Media;
import edu.moravian.media.Show;

import java.util.HashMap;
import java.util.Map;

public class ShowMapper extends MediaMapper<Show> {

    public  HashMap<String,String> showToHash(Show media) {
        return super.toHash(media);
    }

    public Show hashToShow(Show media, Map<String,String> hash) {
        return (Show) super.fromHash(media,hash);
    }

    @Override
    public Show createEmptyMedia() {
        return new Show();
    }

    @Override
    void addSubclassFieldsToHash(Show show, Map<String, String> hash) {
        if (show.getType() != null)hash.put("type", show.getType());
        if (show.getStart() != null)hash.put("start", show.getStart());
        if (show.getEnd() != null)hash.put("end", show.getEnd());
        if (show.getSeasons() != null)hash.put("seasons", show.getSeasons());
    }

    @Override
    void addSubclassHashesToFields(Show show, Map<String, String> hash) {
        show.setStart(hash.get("start"));
        show.setEnd(hash.get("end"));
        show.setSeasons(hash.get("seasons"));
    }



}
