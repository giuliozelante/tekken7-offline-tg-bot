package it.giuliozelante.tekken7.offline.tg.bot.batch;

import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Slf4j
public class Batch {
    private static class BatchImpl implements Runnable {

        public void run() {
            List<String> commands = List.of(
                    "gradlew clean optimizedDockerBuild",
                    "docker save tekken7-offline-tg-bot:latest > tekken7-offline-tg-bot.tar",
                    "scp tekken7-offline-tg-bot.tar root@192.168.1.105:/home/gzelante",
                    "ssh root@192.168.1.105"
            );

            for (String command : commands) {
                try {
                    executeCommand(command);
                } catch (IOException | InterruptedException e) {
                    log.error(e.getMessage(), e);
                }
            }
        }

        private void executeCommand(String command) throws IOException, InterruptedException {
            String firstCommand;
            Path filePath = Paths.get(command.split(" ")[0]);
            if (filePath.toAbsolutePath().toFile().exists())
                firstCommand = filePath.toAbsolutePath().toString();
            else
                firstCommand = filePath.toString();
            ProcessBuilder processBuilder = new ProcessBuilder(Stream
                    .concat(Stream.of(firstCommand),
                            Arrays.stream(Arrays.copyOfRange(command.split(" "), 1, command.split(" ").length)))
                    .collect(Collectors.toList()));
            processBuilder.redirectErrorStream(true);
            Process process = processBuilder.start();

            // Connect the process streams to the current console
            InputStream processInputStream = process.getInputStream();
            OutputStream consoleOutputStream = System.out;
            Thread inputThread = new Thread(() -> {
                try {
                    int c;
                    while ((c = processInputStream.read()) != -1) {
                        consoleOutputStream.write(c);
                    }
                } catch (IOException e) {
                    log.error("Error reading process output", e);
                }
            });
            inputThread.start();

            // Wait for the process to complete
            int exitCode = process.waitFor();
            if (exitCode != 0) {
                log.error("Command failed with exit code: " + exitCode);
            }

            // Wait for the input thread to finish
            inputThread.join();
        }
    }

    public static void main(String[] args) {
        Thread t1 = new Thread(new BatchImpl());
        t1.start();
    }
}
