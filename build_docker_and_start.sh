#!/bin/bash

# Check for changes in the code
if git diff --quiet HEAD; then
    echo "No changes detected. Skipping Docker image build, save, and transfer."
    exit 0
fi

# Load environment variables from .env file
set -o allexport
source .env
set +o allexport

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
sshpass -p "$SSH_PASSWORD" scp -P "$SSH_PORT" "$DOCKER_IMAGE_TAR" docker-compose.yml "$SSH_USER@$SSH_HOST:$REMOTE_DIR"

# Execute the deploy script on the remote server
sshpass -p "$SSH_PASSWORD" ssh -p "$SSH_PORT" "$SSH_USER@$SSH_HOST" "/etc/local.d/start_t7_offline_bot.start"

echo "Deployment completed. Check the remote server for the running container."
#!/bin/bash

# Load environment variables from .env file
set -o allexport
source .env
set +o allexport

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
sshpass -p "$SSH_PASSWORD" scp -P "$SSH_PORT" "$DOCKER_IMAGE_TAR" docker-compose.yml "$SSH_USER@$SSH_HOST:$REMOTE_DIR"

# Execute the deploy script on the remote server
sshpass -p "$SSH_PASSWORD" ssh -p "$SSH_PORT" "$SSH_USER@$SSH_HOST" "/etc/local.d/start_t7_offline_bot.start"

echo "Deployment completed. Check the remote server for the running container."