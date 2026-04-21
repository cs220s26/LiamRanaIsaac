#!/bin/bash

# Set Java 26
export JAVA_HOME=/usr/lib/jvm/java-26-amazon-corretto.x86_64
export PATH=$JAVA_HOME/bin:$PATH

# Navigate to project root
cd /home/ec2-user/LiamRanaIsaac

# Kill old process (force kill to make sure it dies)
pkill -9 -f "WatchlistBot-1.0.0-jar-with-dependencies.jar"

# Wait a moment for process to fully terminate
sleep 2

# Pull latest changes
git pull origin main

# Rebuild
mvn clean package

# Restart bot
nohup java -jar target/WatchlistBot-1.0.0-jar-with-dependencies.jar > bot.log 2>&1 &

echo "Bot redeployed successfully!"
