@echo off

REM Load environment variables from .env file
for /f "tokens=*" %%a in (.env) do (
    set %%a
)

REM Build and clean Docker image
call gradlew bot:clean bot:optimizedDockerBuild

REM Check if the build was successful
if %errorlevel% neq 0 (
    echo Docker build failed. Please check your Dockerfile and try again.
    exit /b 1
)

REM Check if the build was successful
if %errorlevel% neq 0 (
    echo Docker build failed. Please check your Dockerfile and try again.
    exit /b 1
)

REM Save Docker image to tar file
docker save tekken7-offline-tg-bot:latest > tekken7-offline-tg-bot.tar

REM Transfer tar file and deploy script to remote server
scp -P %SSH_PORT% %DOCKER_IMAGE_TAR% %SSH_USER%@%SSH_HOST%:%REMOTE_DIR%

REM Execute the deploy script on the remote server
ssh -p %SSH_PORT% %SSH_USER%@%SSH_HOST% "/etc/local.d/start_t7_offline_bot.start"

echo Deployment completed. Check the remote server for the running container.