package edu.moravian;

import edu.moravian.exceptions.StorageException;
import edu.moravian.media.Movie;
import edu.moravian.media.Show;
import edu.moravian.watchlist.RedisStorage;
import edu.moravian.watchlist.WatchlistAppStorage;

public class DatabaseSeeder {

    public static void main(String[] args) {
        try{
            WatchlistAppStorage storage = new RedisStorage("localhost", 6379);

            System.out.println("Manually adding to DB");

            // 2. Add Movies
            // Constructor: Title, Rating, Genre, Platform, Release, Runtime, Director
            storage.addMedia(new Movie("Inception", "9.0", "Sci-Fi", "Netflix", "2010", "148", "Christopher Nolan"));
            storage.addMedia(new Movie("The Dark Knight", "9.5", "Action", "Max", "2008", "152", "Christopher Nolan"));
            storage.addMedia(new Movie("Interstellar", "8.8", "Sci-Fi", "Paramount+", "2014", "169", "Christopher Nolan"));
            storage.addMedia(new Movie("Toy Story", "8.3", "Animation", "Disney+", "1995", "81", "John Lasseter"));
            storage.addMedia(new Movie("Finding Nemo", "8.5", "Animation", "Disney+", "2003", "100", "Andrew Stanton"));
            storage.addMedia(new Movie("The Matrix", "8.7", "Action", "Max", "1999", "136", "Lana Wachowski"));
            storage.addMedia(new Movie("Mean Girls", "7.1", "Comedy", "Paramount+", "2004", "97", "Mark Waters"));
            storage.addMedia(new Movie("Get Out", "7.8", "Horror", "Peacock", "2017", "104", "Jordan Peele"));
            storage.addMedia(new Movie("Dune", "8.0", "Sci-Fi", "Max", "2021", "155", "Denis Villeneuve"));
            storage.addMedia(new Movie("Barbie", "7.0", "Comedy", "Max", "2023", "114", "Greta Gerwig"));

            // 3. Add Shows
            // Constructor: Title, Rating, Genre, Platform, Start, End, Seasons
            storage.addMedia(new Show("Breaking Bad", "9.5", "Crime", "Netflix", "2008", "2013", "5"));
            storage.addMedia(new Show("Stranger Things", "8.7", "Sci-Fi", "Netflix", "2016", "Present", "4"));
            storage.addMedia(new Show("The Office", "8.9", "Comedy", "Peacock", "2005", "2013", "9"));
            storage.addMedia(new Show("Game of Thrones", "9.2", "Fantasy", "Max", "2011", "2019", "8"));
            storage.addMedia(new Show("The Mandalorian", "8.7", "Sci-Fi", "Disney+", "2019", "Present", "3"));
            storage.addMedia(new Show("Severance", "8.7", "Thriller", "AppleTV", "2022", "Present", "1"));
            storage.addMedia(new Show("Succession", "8.8", "Drama", "Max", "2018", "2023", "4"));
            storage.addMedia(new Show("The Boys", "8.7", "Action", "Prime", "2019", "Present", "4"));
            storage.addMedia(new Show("Friends", "8.9", "Comedy", "Max", "1994", "2004", "10"));
            storage.addMedia(new Show("Black Mirror", "8.8", "Sci-Fi", "Netflix", "2011", "Present", "6"));

            System.out.println("Movies and Shows have been added with correct indices.");
        } catch(StorageException e){
            System.exit(1);
        }
    }
}
