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

REM Check if the build was successful
if %errorlevel% neq 0 (
    echo Docker build failed. Please check your Dockerfile and try again.
    exit /b 1
)

REM Save Docker image to tar file
call docker save tekken7-offline-tg-bot:latest > %DOCKER_IMAGE_TAR%

REM Transfer tar file to remote server
call scp tekken7-offline-tg-bot.tar root@192.168.1.105:/home/gzelante

REM SSH into remote server
call ssh root@192.168.1.105 "<< EOF

#REM Kill existing Docker container
docker kill t7_offline_tg_bot

#REM Load Docker image from tar file
docker load -i /home/gzelante/tekken7-offline-tg-bot.tar

#REM Run Docker container with environment variables
docker run -d -e T7_OFFLINE_BOT_DB_PASSWORD \
                -e T7_OFFLINE_BOT_DB_USERNAME \
                -e T7_OFFLINE_BOT_TOKEN \
                -e T7_OFFLINE_BOT_USERNAME \
                -e VIRUS_TOTAL_API_KEY \
                --rm --name t7_offline_tg_bot -it tekken7-offline-tg-bot:latest \
                -v /var/logs/tekken-offline-tg-bot:/var/logs/tekken-offline-tg-bot
EOF"