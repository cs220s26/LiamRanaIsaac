package edu.moravian.media;

public class Movie extends Media{
    private String runtime;
    private String director;
    private String release;

    public Movie(){
        super();
        runtime = "";
        director = "";
        release = "";
    }

    public Movie(String title, String rating, String genre, String streamingPlatform,
                 String release, String runtime, String director){
        super(title, rating, genre, streamingPlatform);
        this.runtime = runtime;
        this.release = release;
        this.director = director;
    }

    @Override
    public String getType(){
        return "movie";
    }

    public String getRuntime() {
        return runtime;
    }

    public String getDirector() {
        return director;
    }

    public String getRelease() {
        return release;
    }

    public void setRuntime(String runtime) {
        this.runtime = runtime;
    }

    public void setDirector(String director) {
        this.director = director;
    }

    public void setRelease(String release) {
        this.release = release;
    }
}
