package it.giuliozelante.tekken7.offline.tg.bot.meetup.service;

import io.micronaut.context.event.ApplicationEventListener;
import io.micronaut.context.event.StartupEvent;
import it.giuliozelante.tekken7.offline.tg.bot.meetup.entity.Command;
import it.giuliozelante.tekken7.offline.tg.bot.meetup.repository.CommandRepository;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import lombok.extern.slf4j.Slf4j;

/**
 * Service to initialize commands in the database.
 */
@Singleton
@Slf4j
public class CommandInitializationService implements ApplicationEventListener<StartupEvent> {

    @Inject
    private CommandRepository commandRepository;

    @Override
    public void onApplicationEvent(StartupEvent event) {
        initializeVoiceMessageCommand();
    }

    /**
     * Initializes the voice message command in the database if it doesn't exist.
     */
    private void initializeVoiceMessageCommand() {
        // Check if the voice message command already exists
        boolean voiceCommandExists = commandRepository.findAll().stream()
                .anyMatch(command -> command.getName().equals("voice_message_help"));

        if (!voiceCommandExists) {
            log.info("Initializing voice message command");
            Command voiceCommand = new Command();
            voiceCommand.setName("voice_message_help");
            voiceCommand.setDescription("Shows information about voice message recognition feature");
            commandRepository.save(voiceCommand);
            log.info("Voice message command initialized");
        }
    }
} 