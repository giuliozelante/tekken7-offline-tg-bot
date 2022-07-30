package it.giuliozelante.tekken7.offline.tg.bot.meetup;

import it.giuliozelante.tekken7.offline.tg.bot.meetup.entity.TelegramGroup;
import it.giuliozelante.tekken7.offline.tg.bot.meetup.repository.GroupRepository;
import lombok.extern.log4j.Log4j2;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.groupadministration.GetChatAdministrators;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.chatmember.ChatMember;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import javax.inject.Inject;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Log4j2
public class MeetUp extends TelegramLongPollingBot {
    @Inject
    GroupRepository groupRepository;

    @Override
    public void onUpdateReceived(Update update) {
        // We check if the update has a message and the message has text

        if (update.hasMessage() && update.getMessage().hasText() && update.getMessage().isCommand() &&
                this.getChatAdministrators(update.getMessage().getChatId())
                        .stream().map(ChatMember::getUser).collect(Collectors.toSet())
                        .contains(update.getMessage().getFrom())) {
            String textMessage = update.getMessage().getText().substring(1);
            long chatId = update.getMessage().getChatId();
            TelegramGroup group = groupRepository.findByChatId(chatId).orElse(null);
            switch (textMessage) {
                // Set variables
                case "startMeetUp":
                    if (group == null) {
                        group = new TelegramGroup();
                        group.setChatId(chatId);
                        groupRepository.save(group);
                        SendMessage message = new SendMessage(); // Create a message object object
                        message.setChatId(String.valueOf(chatId));
                        StringBuilder sb = new StringBuilder("Started Scheduled Job for the meetings.\n");
                        sb.append("There will be a new Poll every Monday.\n");
                        sb.append("Based on the aforementioned Poll, on Wednesday a meeting event ");
                        sb.append("will be created.\n");
                        sb.append("To stop the scheduled job write /stopMeetUp.\n");
                        this.sendMessage(chatId, sb.toString());
                    } else {
                        this.sendMessage(chatId, "Already started Scheduled Job for the meetings");
                    }
                    break;
                case "stopMeetUp":
                    if(group.isStarted()) {
                        group.setStarted(false);
                    }
                    groupRepository.save(group);
                    this.sendMessage(chatId, "The scheduled job for the meetings has been stopped");

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

    private List<ChatMember> getChatAdministrators(Long chatId) {
        List<ChatMember> chatAdministrators = Collections.emptyList();
        try {
            chatAdministrators = execute(new GetChatAdministrators(String.valueOf(chatId)));
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
        return chatAdministrators;
    }

    private void sendMessage(Long chatId, String textMessage) {
        SendMessage message = new SendMessage(); // Create a message object object
        message.setChatId(String.valueOf(chatId));
        message.setText(textMessage);
        try {
            execute(message); // Sending our message object to user
        } catch (TelegramApiException e) {
            log.error(e.getMessage(), e);
        }
    }
}