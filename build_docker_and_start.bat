@echo off

REM Build and clean Docker image
call gradlew clean optimizedDockerBuild

REM Save Docker image to tar file
call docker save tekken7-offline-tg-bot:latest > tekken7-offline-tg-bot.tar

REM Transfer tar file to remote server
call scp tekken7-offline-tg-bot.tar root@192.168.1.105:/home/gzelante

REM Transfer docker-compose.yml to remote server
call scp docker-compose.yml root@192.168.1.105:/home/gzelante