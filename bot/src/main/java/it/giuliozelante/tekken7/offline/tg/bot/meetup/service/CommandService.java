package it.giuliozelante.tekken7.offline.tg.bot.meetup.service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.telegram.telegrambots.meta.api.methods.commands.GetMyCommands;
import org.telegram.telegrambots.meta.api.methods.commands.SetMyCommands;
import org.telegram.telegrambots.meta.api.objects.commands.BotCommand;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import it.giuliozelante.tekken7.offline.tg.bot.meetup.MeetUp;
import it.giuliozelante.tekken7.offline.tg.bot.meetup.entity.Command;
import it.giuliozelante.tekken7.offline.tg.bot.meetup.repository.CommandRepository;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import lombok.extern.slf4j.Slf4j;

@Singleton
@Slf4j
public class CommandService {
    @Inject
    private CommandRepository commamdRepository;

    public void updateCommands(MeetUp meetUp) {
        try {
            GetMyCommands getMyCommands = new GetMyCommands();
            List<BotCommand> botCommands = meetUp.execute(getMyCommands);
            List<Command> commands = this.commamdRepository.findAll();

            Map<String, String> existingCommands = botCommands.stream()
                    .collect(Collectors.toMap(BotCommand::getCommand, BotCommand::getDescription));
            Map<String, String> newCommands = commands.stream()
                    .collect(Collectors.toMap(Command::getName, Command::getDescription));
            if (!existingCommands.equals(newCommands)) {
                SetMyCommands setMyCommands = new SetMyCommands();
                setMyCommands.setCommands(commands.stream()
                        .map(command -> new BotCommand(command.getName(), command.getDescription()))
                        .collect(Collectors.toList()));
                meetUp.execute(setMyCommands);
            }
            // this.commamdRepository.saveAll(commands);
        } catch (TelegramApiException e) {
            log.error(e.getMessage(), e);
        }

    }

    public List<Command> getCommands() {
        return this.commamdRepository.findAll();
    }
}
