#!/bin/bash

cd /home/ec2-user/LiamRanaIsaac

# Pull latest changes
git pull origin main

# Rebuild
mvn clean package

# Kill old process
pkill -f "WatchlistBot-1.0.0-jar-with-dependencies.jar"

# Restart bot
nohup java -jar target/WatchlistBot-1.0.0-jar-with-dependencies.jar > bot.log 2>&1 &

echo "Bot redeployed successfully!"
