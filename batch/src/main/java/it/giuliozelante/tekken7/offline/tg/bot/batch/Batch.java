package it.giuliozelante.tekken7.offline.tg.bot.batch;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class Batch {
    public static void main(String args[]) {
        Batch batch = new Batch();
        String[] commands = List.of("gradlew clean optimizedDockerBuild",
                "docker save tekken7-offline-tg-bot:latest > tekken7-offline-tg-bot.tar",
                "scp tekken7-offline-tg-bot.tar root@192.168.1.105:/home/gzelante",
                "ssh root@192.168.1.105").toArray(String[]::new);
        try {
            batch.runCommandsInSeries(commands);
        } catch (InterruptedException | IOException e) {
            log.error(e.getMessage(), e);
        }

    }

    public void runCommandsInSeries(String[] commands) throws IOException, InterruptedException {
        for (String command : commands) {
            String firstCommand;
            Path filePath = Paths.get(command.split(" ")[0]);
            if(filePath.toAbsolutePath().toFile().exists())
                firstCommand = filePath.toAbsolutePath().toString();
            else
                firstCommand = filePath.toString();
            ProcessBuilder pb = new ProcessBuilder(Stream.concat(Stream.of(firstCommand), Arrays.stream(Arrays.copyOfRange(command.split(" "),1, command.split( " ").length))).collect(Collectors.toList()));
            pb.redirectErrorStream(true);
            pb.redirectOutput(ProcessBuilder.Redirect.INHERIT);
            pb.redirectInput(ProcessBuilder.Redirect.INHERIT);
            Process process = pb.start();
            process.waitFor();
        }
    }
}
