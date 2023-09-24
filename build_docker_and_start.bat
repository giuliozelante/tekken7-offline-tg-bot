@echo off

REM Build and clean Docker image
call gradlew clean optimizedDockerBuild

REM Save Docker image to tar file
call docker save tekken7-offline-tg-bot:latest > tekken7-offline-tg-bot.tar

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