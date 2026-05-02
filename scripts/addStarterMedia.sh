#!/bin/bash

echo -e "Starting bulk import of media items... \n"

# --- MOVIES ---
./addMediaDB.sh movie "Inception" "9.0" "Sci-Fi" "Netflix" "2010" "Christopher Nolan" "148"
./addMediaDB.sh movie "The Dark Knight" "9.5" "Action" "Max" "2008" "Christopher Nolan" "152"
./addMediaDB.sh movie "Interstellar" "8" "Sci-Fi" "Paramount" "2014" "Christopher Nolan" "169"
./addMediaDB.sh movie "Toy Story" "8.3" "Animation" "Disney+" "1995" "John Lasseter" "81"
./addMediaDB.sh movie "Finding Nemo" "8.5" "Animation" "Disney+" "2003" "Andrew Stanton" "100"
./addMediaDB.sh movie "The Matrix" "8.7" "Action" "Max" "1999" "Lana Wachowski" "136"
./addMediaDB.sh movie "Mean Girls" "7.1" "Comedy" "Paramount+" "2004" "Mark Waters" "97"
./addMediaDB.sh movie "Get Out" "7.8" "Horror" "Peacock" "2017" "Jordan Peele" "104"
./addMediaDB.sh movie "Dune" "8.0" "Sci-Fi" "Max" "2021" "Denis Villeneuve" "155"
./addMediaDB.sh movie "Barbie" "7.0" "Comedy" "Max" "2023" "Greta Gerwig" "114"

# --- SHOWS ---
./addMediaDB.sh show "Breaking Bad" "9.5" "Crime" "Netflix" "2008" "2013" "5"
./addMediaDB.sh show "Stranger Things" "8.7" "Sci-Fi" "Netflix" "2016" "Present" "4"
./addMediaDB.sh show "The Office" "8.9" "Comedy" "Peacock" "2005" "2013" "9"
./addMediaDB.sh show "Game of Thrones" "9.2" "Fantasy" "Max" "2011" "2019" "8"
./addMediaDB.sh show "The Mandalorian" "8.7" "Sci-Fi" "Disney+" "2019" "Present" "3"
./addMediaDB.sh show "Severance" "8.7" "Thriller" "AppleTV" "2022" "Present" "1"
./addMediaDB.sh show "Succession" "8.8" "Drama" "Max" "2018" "2023" "4"
./addMediaDB.sh show "The Boys" "8.7" "Action" "Prime" "2019" "Present" "4"
./addMediaDB.sh show "Friends" "8.9" "Comedy" "Max" "1994" "2004" "10"
./addMediaDB.sh show "Black Mirror" "8.8" "Sci-Fi" "Netflix" "2011" "Present" "6"

echo -e "\nAll items have been processed."