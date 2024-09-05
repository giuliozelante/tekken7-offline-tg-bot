package it.giuliozelante.tekken7.offline.tg.bot.batch;

import java.io.IOException;
import java.util.List;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class Batch {
    public static void main(String[] args) {
        Batch batch = new Batch();
        List<String> commands = List.of(
            "./gradlew clean optimizedDockerBuild",
            "docker save tekken7-offline-tg-bot:latest > tekken7-offline-tg-bot.tar",
            "scp tekken7-offline-tg-bot.tar root@192.168.1.105:/home/gzelante",
            "ssh root@192.168.1.105"
        );
        try {
            batch.runCommandsInSeries(commands);
        } catch (InterruptedException | IOException e) {
            log.error("Error executing commands", e);
            if (e instanceof InterruptedException) {
                Thread.currentThread().interrupt();
            }
        }
    }

    public void runCommandsInSeries(List<String> commands) throws IOException, InterruptedException {
        for (String command : commands) {
            List<String> commandParts = parseCommand(command);
            ProcessBuilder pb = new ProcessBuilder(commandParts);
            pb.inheritIO();
            Process process = pb.start();
            int exitCode = process.waitFor();
            if (exitCode != 0) {
                log.warn("Command '{}' exited with code {}", command, exitCode);
            }
        }
    }

    private List<String> parseCommand(String command) {
        return List.of(command.split("\\s+"));
    }
}
