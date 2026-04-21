#!/bin/bash

# Set Java 21
export JAVA_HOME=/usr/lib/jvm/java-21-amazon-corretto.x86_64
export PATH=$JAVA_HOME/bin:$PATH

# Pull latest changes
sudo git pull origin main

# Rebuild
sudo mvn clean package

# Restart bot
sudo systemctl restart watchlistbot.service

echo "Bot redeployed successfully!"
