package tekken7.offline.tg.bot;

import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;
import tekken7.offline.tg.bot.meetup.MeetUp;

public class App {

    public static void main(String[] args) {
        // Instantiate Telegram Bots API
        try {
            TelegramBotsApi botsApi = new TelegramBotsApi(DefaultBotSession.class);
            // Register our bot
            botsApi.registerBot(new MeetUp());
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }
}
