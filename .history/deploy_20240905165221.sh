#!/bin/bash

# Check if H2 database is already running
if ! pgrep -f "h2-2.2.220.jar" > /dev/null; then
    # Start H2 database in the background
    nohup java -jar h2/bin/h2-2.2.220.jar -tcp -tcpAllowOthers &
    echo "H2 database started."
else
    echo "H2 database is already running."
fi

# Check if Docker image exists (optional)
if ! docker image inspect tekken7-offline-tg-bot:latest &> /dev/null; then
  echo "Error: Docker image 'tekken7-offline-tg-bot:latest' not found."
  exit 1
fi

# Stop any existing container (optional)
docker stop t7_offline_tg_bot &> /dev/null

# Load the Docker image
docker load -i /home/gzelante/tekken7-offline-tg-bot.tar

# Start the Docker container
docker run -d \
  --rm \
  -e T7_OFFLINE_BOT_DB_PASSWORD \
  -e T7_OFFLINE_BOT_DB_USERNAME \
  -e T7_OFFLINE_BOT_TOKEN \
  -e T7_OFFLINE_BOT_USERNAME \
  -e VIRUS_TOTAL_API_KEY \
  --name t7_offline_tg_bot \
  -v /var/logs/tekken-offline-tg-bot:/var/logs/tekken-offline-tg-bot \
  tekken7-offline-tg-bot:latest

echo "Docker container started."