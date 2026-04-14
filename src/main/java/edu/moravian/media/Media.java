package edu.moravian.media;

public abstract class Media {
    private String title;
    private String rating;
    private String genre;
    private String streamingService;

    public abstract String getType();

    public Media(){
        this.title = "";
        this.rating = "";
        this.genre = "";
        this.streamingService = "";
    }

    public Media(String title, String rating, String genre, String streamingPlatform){
        this.title = title;
        this.rating = rating;
        this.genre = genre;
        this.streamingService = streamingPlatform;
    }

    @Override
    public String toString(){
        return "Title: " + title + "\n" + "Rating: " + rating + "\n" + "Genre: " + genre + "\n" + "Note: " + streamingService;
    }

    public String getTitle() {
        return title;
    }

    public String getRating() {
        return rating;
    }

    public String getGenre() {
        return genre;
    }

    public String getStreamingService() {
        return streamingService;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setRating(String rating) {
        this.rating = rating;
    }

    public void setGenre(String genre) {
        this.genre = genre;
    }

    public void setStreamingService(String streamingPlatform) {
        this.streamingService = streamingPlatform;
    }
}
