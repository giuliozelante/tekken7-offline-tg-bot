package it.giuliozelante.tekken7.offline.tg.bot;

import java.util.Map;

import io.micronaut.runtime.Micronaut;

public class App {

    public static void main(String[] args) {
        Map<String, String> environmentVariables = System.getenv();

        for (String environmentVariableName : environmentVariables.keySet()) {
            String environmentVariableValue = environmentVariables.get(environmentVariableName);

            System.out.println(environmentVariableName + " = " + environmentVariableValue);
        }
        Micronaut.run(App.class, args);
    }
}
