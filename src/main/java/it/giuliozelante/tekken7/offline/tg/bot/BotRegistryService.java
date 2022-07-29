package it.giuliozelante.tekken7.offline.tg.bot;

import io.micronaut.context.event.ApplicationEventListener;
import io.micronaut.context.event.StartupEvent;
import it.giuliozelante.tekken7.offline.tg.bot.meetup.MeetUp;
import lombok.extern.log4j.Log4j2;
import org.telegram.telegrambots.bots.TelegramWebhookBot;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.generics.LongPollingBot;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.List;

@Singleton
@Log4j2
public class BotRegistryService implements ApplicationEventListener<StartupEvent> {

//    @Inject
//    List<LongPollingBot> longPollingBots;

//    @Inject
//    List<TelegramWebhookBot> webhookBots;

    public void onApplicationEvent(final StartupEvent event) {
        try {
            TelegramBotsApi telegramBotsApi = new TelegramBotsApi(DefaultBotSession.class);

            log.info("Registering Longpolling Bots");
//            for (LongPollingBot bot : longPollingBots) {
                try {
                    MeetUp meetUp = new MeetUp();
                    telegramBotsApi.registerBot(new MeetUp());
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
}