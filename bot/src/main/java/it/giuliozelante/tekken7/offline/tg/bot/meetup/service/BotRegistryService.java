package it.giuliozelante.tekken7.offline.tg.bot.meetup.service;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.StreamSupport;

import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;

import io.micronaut.context.event.ApplicationEventListener;
import io.micronaut.context.event.StartupEvent;
import io.micronaut.scheduling.annotation.Scheduled;
import it.giuliozelante.tekken7.offline.tg.bot.meetup.MeetUp;
import it.giuliozelante.tekken7.offline.tg.bot.meetup.entity.Poll;
import it.giuliozelante.tekken7.offline.tg.bot.meetup.entity.TelegramGroup;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import lombok.extern.slf4j.Slf4j;

@Singleton
@Slf4j
public class BotRegistryService implements ApplicationEventListener<StartupEvent> {

    @Inject
    MeetUp meetUp;

    @Inject
    GroupService groupService;

    @Inject
    PollService pollService;

    @Inject
    CommandService commandService;

    public void onApplicationEvent(final StartupEvent event) {
        try {
            TelegramBotsApi telegramBotsApi = new TelegramBotsApi(DefaultBotSession.class);
            log.info("Registering Bots");
            registerBot(telegramBotsApi);
            log.info("Finished registering bots");
        } catch (TelegramApiException e) {
            log.error(e.getMessage(), e);
        }
    }

    @Scheduled(cron = "0 9 * * MON", zoneId = "Europe/Paris")
    protected void startPolls() {
        Iterable<TelegramGroup> groups = groupService.findAll();
        StreamSupport.stream(groups.spliterator(), true).forEach(group -> pollService.startPoll(group, meetUp));
    }

    @Scheduled(cron = "0 0 * * SUN", zoneId = "Europe/Paris")
    protected void stopPolls() {
        Iterable<TelegramGroup> groups = groupService.findAll();
        StreamSupport.stream(groups.spliterator(), true).forEach(group -> {
            List<Poll> polls = pollService.findByTelegramGroupAndActive(group, true);
            AtomicReference<List<Poll>> updatedPolls = new AtomicReference<>(new ArrayList<>());
            polls.parallelStream().forEachOrdered(poll -> pollService.stopPoll(group, meetUp));
            pollService.updateAll(updatedPolls.get());
        });
    }

    private void registerBot(TelegramBotsApi telegramBotsApi) {
        try {
            telegramBotsApi.registerBot(meetUp);
            log.info("Registered {}", meetUp.getBotUsername());
            commandService.updateCommands(meetUp);
        } catch (TelegramApiException e) {
            log.error(e.getMessage(), e);
        }
    }
}