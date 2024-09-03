package it.giuliozelante.tekken7.offline.tg.bot;

import java.util.Map;

import io.micronaut.runtime.Micronaut;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class App {

    public static void main(String[] args) {
        Map<String, String> environmentVariables = System.getenv();

        for (Map.Entry<String, String> entry : environmentVariables.entrySet()) {
            String environmentVariableName = entry.getKey();
            String environmentVariableValue = entry.getValue();

            log.debug(environmentVariableName + " = " + environmentVariableValue);
        }
        Micronaut.run(App.class, args);
    }
}
