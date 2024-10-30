@echo off

REM Check for changes in the code using git
REM git diff --quiet HEAD
REM if %errorlevel% equ 0 (
REM     echo No changes detected. Skipping Docker image build, save, and transfer.
REM     exit /b 0
REM )

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

REM Save Docker image to tar file
call docker save tekken7-offline-tg-bot:latest > %DOCKER_IMAGE_TAR%

REM Transfer tar file and docker-compose.yml to remote server
REM call sshpass -p %SSH_PASSWORD% scp -P %SSH_PORT% %DOCKER_IMAGE_TAR% docker-compose.yml %SSH_USER%@%SSH_HOST%:%REMOTE_DIR%
REM call sshpass -p %SSH_PASSWORD% ssh -p %SSH_PORT% %DOCKER_IMAGE_TAR% "/etc/local.d/start_t7_offline_bot.start"