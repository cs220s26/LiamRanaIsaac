#!/bin/bash

# Navigate to the project directory
cd /LiamRanaIsaac
# Pull latest changes
sudo git pull origin main
# Rebuild
sudo mvn clean package
# Restart bot
sudo systemctl restart watchlistbot.service
