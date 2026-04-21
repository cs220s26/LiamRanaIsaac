## DevOps Final Project

# Overview

The Watchlist Bot is a Java-based Discord application designed to help users curate and discover movies and TV shows. Unlike standard command-line bots that require complex, single-line arguments, this bot implements a conversational interface. It utilizes a finite state machine to guide users through multi-step processes, such as adding a movie with specific metadata (Director, Runtime, Genre) or filtering their current library to find something to watch.

Technically, the project demonstrates Clean Architecture. It strictly separates the delivery mechanism (Discord JDA) from the business logic (WatchlistApp) and the persistence layer (RedisStorage). This design allows the application to switch seamlessly between an in-memory database (for unit testing) and a Redis database (for production) without changing the core application logic.

# Dev Setup/Execution

# Prod Setup/Execution

# CI/CD Setup

# CI/CD Execution

# Technologies Used

* Java Discord API (JDA)
* Redis

# Background


# Contributors:

* Liam Kerr
* Rana Yum
* Isaac Nunez
