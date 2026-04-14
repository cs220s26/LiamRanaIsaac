package edu.moravian.media;

public class Show extends Media{
    private String start;
    private String end;
    private String seasons;

    public Show(){
        super();
        this.start = "";
        this.end = "";
        this.seasons = "";
    }

    public Show (String title, String rating, String genre, String streamingPlatform,
                 String start, String end, String seasons){
        super(title, rating, genre, streamingPlatform);
        this.start = start;
        this.end = end;
        this.seasons = seasons;
    }

    @Override
    public String getType(){
        return "show";
    }

    public String getStart() {
        return start;
    }

    public String getEnd() {
        return end;
    }

    public String getSeasons() {
        return seasons;
    }

    public void setStart(String start) {
        this.start = start;
    }

    public void setEnd(String end) {
        this.end = end;
    }

    public void setSeasons(String seasons) {
        this.seasons = seasons;
    }
}
