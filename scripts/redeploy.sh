#!/bin/bash

# Pull latest changes
sudo git pull origin main
# Rebuild
sudo mvn clean package
# Restart bot
sudo systemctl restart watchlistbot.service
