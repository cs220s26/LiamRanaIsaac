#!/bin/bash

# Update system
sudo yum update -y

# Install dependencies
sudo yum install -y git redis6 maven-amazon-corretto21

# Clone repository
git clone https://github.com/cs220s26/LiamRanaIsaac.git /LiamRanaIsaac
cd /LiamRanaIsaac

# Start Redis
sudo systemctl start redis6
sudo systemctl enable redis6

# Build project
mvn clean package

# Copy and start the systemd service
sudo cp watchlistbot.service /etc/systemd/system
sudo systemctl enable watchlistbot
sudo systemctl start watchlistbot
