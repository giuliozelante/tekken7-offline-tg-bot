@echo off

REM Check for changes in the code using git
git diff --quiet HEAD
if %errorlevel% equ 0 (
    echo No changes detected. Skipping Docker image build, save, and transfer.
    exit /b 0
)

REM Load environment variables from .env file
for /f "tokens=*" %%a in (.env) do (
    set %%a
)

REM Build and clean Docker image
call gradlew clean optimizedDockerBuild

REM Check if the build was successful
if %errorlevel% neq 0 (
    echo Docker build failed. Please check your Dockerfile and try again.
    exit /b 1
)

REM Save Docker image to tar file
docker save tekken7-offline-tg-bot:latest > %DOCKER_IMAGE_TAR%

REM Transfer tar file and docker-compose.yml to remote server
call sshpass -p %SSH_PASSWORD% scp -P %SSH_PORT% %DOCKER_IMAGE_TAR% docker-compose.yml %SSH_USER%@%SSH_HOST%:%REMOTE_DIR%

call shpass -p %SSH_PASSWORD% ssh -p %SSH_PORT% %DOCKER_IMAGE_TAR% "/etc/local.d/start_t7_offline_bot.start"