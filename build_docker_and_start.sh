#!/bin/bash

# Load environment variables from .env file
set -a
source .env
set +a

# Build and clean Docker image
./gradlew clean optimizedDockerBuild

# Check if the build was successful
if [ $? -ne 0 ]; then
    echo "Docker build failed. Please check your Dockerfile and try again."
    exit 1
fi

# Save Docker image to tar file
docker save bot:latest > "$DOCKER_IMAGE_TAR"

# Transfer tar file and deploy script to remote server
scp -P "$SSH_PORT" "$DOCKER_IMAGE_TAR" "$SSH_USER@$SSH_HOST:$REMOTE_DIR"

# Execute the deploy script on the remote server
ssh -p "$SSH_PORT" "$SSH_USER@$SSH_HOST" "/etc/local.d/start_t7_offline_bot.start"

echo "Deployment completed. Check the remote server for the running container."