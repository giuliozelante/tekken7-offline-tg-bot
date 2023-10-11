package it.giuliozelante.tekken7.offline.tg.bot.batch;

import java.io.IOException;
import java.nio.file.Paths;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class Batch {
    public static void main(String args[]) {
        try {
            new ProcessBuilder(Paths.get("gradlew").toString(), "clean", "optimizedDockerBuild").start();
            new ProcessBuilder(Paths.get("docker").toString(), "tekken7-offline-tg-bot:latest", ">",
                    "tekken7-offline-tg-bot.tar").start();
            new ProcessBuilder(Paths.get("scp").toString(), "tekken7-offline-tg-bot.tar",
                    "root@192.168.1.105:/home/gzelante").start();
            new ProcessBuilder(Paths.get("ssh").toString(), "root@192.168.1.105").start();

        } catch (IOException e) {
            log.error(e.getMessage(), e);
        }

    }
}
