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
cd /home/ec2-user
git clone https://github.com/cs220s26/LiamRanaIsaac.git
cd LiamRanaIsaac

# Create .aws directory structure (credentials added manually after)
mkdir -p /home/ec2-user/.aws
chown ec2-user:ec2-user /home/ec2-user/.aws

# Start Redis
sudo systemctl start redis6
sudo systemctl enable redis6

# Build project
mvn clean package

echo "Setup complete! SSH in and add AWS credentials to ~/.aws/credentials, then run the bot."
