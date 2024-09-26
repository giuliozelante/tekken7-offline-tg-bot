@echo off

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
docker save tekken7-offline-tg-bot:latest > tekken7-offline-tg-bot.tar

REM Transfer tar file and docker-compose.yml to remote server
call scp tekken7-offline-tg-bot.tar docker-compose.yml root@192.168.1.105:/home/gzelante