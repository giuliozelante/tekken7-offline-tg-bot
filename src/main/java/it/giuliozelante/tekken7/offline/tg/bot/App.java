package it.giuliozelante.tekken7.offline.tg.bot;

import io.micronaut.runtime.Micronaut;
import it.giuliozelante.tekken7.offline.tg.bot.meetup.MeetUp;
import lombok.extern.log4j.Log4j2;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;

@Log4j2
public class App {

    public static void main(String[] args) {
        Micronaut.run(App.class, args);// Instantiate Telegram Bots API
//        try {
//            TelegramBotsApi botsApi = new TelegramBotsApi(DefaultBotSession.class);
//            // Register our bot
//            botsApi.registerBot(new MeetUp());
//        } catch (TelegramApiException e) {
//            e.printStackTrace();
//        }
    }
}
