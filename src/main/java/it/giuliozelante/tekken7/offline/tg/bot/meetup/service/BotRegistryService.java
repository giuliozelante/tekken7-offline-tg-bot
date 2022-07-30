package it.giuliozelante.tekken7.offline.tg.bot.meetup.service;

import io.micronaut.context.event.ApplicationEventListener;
import io.micronaut.context.event.StartupEvent;
import io.micronaut.scheduling.annotation.Scheduled;
import it.giuliozelante.tekken7.offline.tg.bot.meetup.MeetUp;
import it.giuliozelante.tekken7.offline.tg.bot.meetup.entity.TelegramGroup;
import it.giuliozelante.tekken7.offline.tg.bot.meetup.entity.Poll;
import it.giuliozelante.tekken7.offline.tg.bot.meetup.repository.GroupRepository;
import it.giuliozelante.tekken7.offline.tg.bot.meetup.repository.PollRepository;
import lombok.extern.log4j.Log4j2;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.api.methods.polls.SendPoll;
import org.telegram.telegrambots.meta.api.methods.polls.StopPoll;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.StreamSupport;

@Singleton
@Log4j2
public class BotRegistryService implements ApplicationEventListener<StartupEvent> {

//    @Inject
//    List<LongPollingBot> longPollingBots;

//    @Inject
//    List<TelegramWebhookBot> webhookBots;
    @Inject
    MeetUp meetUp;

    @Inject
    GroupRepository groupRepository;

    @Inject
    PollRepository pollRepository;

    public void onApplicationEvent(final StartupEvent event) {
        try {
            TelegramBotsApi telegramBotsApi = new TelegramBotsApi(DefaultBotSession.class);

            log.info("Registering Longpolling Bots");
//            for (LongPollingBot bot : longPollingBots) {
                try {
                    telegramBotsApi.registerBot(meetUp);
                    log.info("Registered {}", meetUp.getBotUsername());
                } catch (TelegramApiException e) {
                    log.error(e.getMessage(), e);
                }
//            }

//            log.info("Registering Webhook Bots");
//            for (TelegramWebhookBot bot : webhookBots) {
//                try {
//                    telegramBotsApi.registerBot(bot);
//                    log.info("Registered {}", bot.getBotUsername());
//                } catch (TelegramApiException e) {
//                    log.error(e.getMessage(), e);
//                }
//            }
//            log.info("Finished registering bots");
        } catch (TelegramApiException e) {
            log.error(e.getMessage(), e);
        }
    }

    //@Scheduled(cron="0 9 ? * MON", zoneId = "Europe/Paris")
    @Scheduled(cron="18 10 * * SAT", zoneId = "Europe/Paris")
    void sendPoll(){
        List<String> options = new ArrayList<>();
        options.add("Venerdì");
        options.add("Sabato");
        options.add("Domenica");

        Iterable<TelegramGroup> groups = groupRepository.findAll();
        StreamSupport.stream(groups.spliterator(), true).forEach( group -> {
            try {
                SendPoll meetingDay = new SendPoll(String.valueOf(group.getChatId()), "Quando ci vediamo al BarberShop?", options);
                Message pollMessage = meetUp.execute(meetingDay);
                Poll poll = new Poll();
                poll.setMessageId(pollMessage.getMessageId().longValue());
                poll.setTelegramGroup(group);
                pollRepository.save(poll);
            } catch (TelegramApiException e) {
                log.error(e.getMessage(), e);
            }
        });
    }

    //@Scheduled(cron="0 0 ? * WED", zoneId = "Europe/Paris")
    @Scheduled(cron="20 10 * * SAT", zoneId = "Europe/Paris")
    void stopPoll(){
        Iterable<TelegramGroup> groups = groupRepository.findAll();
        StreamSupport.stream(groups.spliterator(), true).forEach( group -> {
            try {
                Poll poll = pollRepository.findByTelegramGroup(group).orElse(null);
                StopPoll meetingDay = new StopPoll(String.valueOf(group.getChatId()), poll.getMessageId().intValue());
                meetUp.execute(meetingDay);
            } catch (TelegramApiException e) {
                log.error(e.getMessage(), e);
            }
        });
    }
}