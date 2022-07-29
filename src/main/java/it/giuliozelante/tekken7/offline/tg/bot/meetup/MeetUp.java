package it.giuliozelante.tekken7.offline.tg.bot.meetup;

import lombok.extern.log4j.Log4j2;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

@Log4j2
public class MeetUp extends TelegramLongPollingBot {
    @Override
    public void onUpdateReceived(Update update) {
        // We check if the update has a message and the message has text
        if (update.hasMessage() && update.getMessage().hasText()) {
            // Set variables
            String message_text = update.getMessage().getText();
            long chatId = update.getMessage().getFrom().getId();//.getChatId();

            SendMessage message = new SendMessage(); // Create a message object object
            message.setChatId(String.valueOf(chatId));
            message.setText(message_text);
            try {
                execute(message); // Sending our message object to user
            } catch (TelegramApiException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public String getBotUsername() {
        return "T7OfflineMeetUpBot";
    }

    @Override
    public String getBotToken() {
        return "2133956905:AAHBklcnBjy3bOSF2lsDLSp5wtddITOSPRY";
    }
}