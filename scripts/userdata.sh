#!/bin/bash

# Update system
sudo yum update -y

# Install dependencies
sudo yum install -y git redis6 maven java-26-amazon-corretto-devel

# Configure Java/Maven to use Java 26
sudo alternatives --set java /usr/lib/jvm/java-26-amazon-corretto.x86_64/bin/java
sudo alternatives --set javac /usr/lib/jvm/java-26-amazon-corretto.x86_64/bin/javac

# Set JAVA_HOME permanently for ec2-user
echo 'export JAVA_HOME=/usr/lib/jvm/java-26-amazon-corretto.x86_64' >> /home/ec2-user/.bashrc
echo 'export PATH=$JAVA_HOME/bin:$PATH' >> /home/ec2-user/.bashrc

# Set JAVA_HOME for this script session
export JAVA_HOME=/usr/lib/jvm/java-26-amazon-corretto.x86_64
export PATH=$JAVA_HOME/bin:$PATH

# Clone repository
git clone https://github.com/cs220s26/LiamRanaIsaac.git /LiamRanaIsaac
cd /LiamRanaIsaac

# Start Redis
sudo systemctl start redis6
sudo systemctl enable redis6

# Build project
mvn clean package

# Copy and start the systemd service
cp watchlistbot.service /etc/systemd/system
sudo systemctl enable watchlistbot
sudo systemctl start watchlistbot
