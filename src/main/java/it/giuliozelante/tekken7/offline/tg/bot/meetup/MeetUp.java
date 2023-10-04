package it.giuliozelante.tekken7.offline.tg.bot.meetup;

import java.util.Collections;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.groupadministration.GetChatAdministrators;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.chatmember.ChatMember;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboard;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import io.micronaut.context.annotation.Bean;
import io.micronaut.context.annotation.Value;
import it.giuliozelante.tekken7.offline.tg.bot.meetup.entity.Command;
import it.giuliozelante.tekken7.offline.tg.bot.meetup.entity.TelegramGroup;
import it.giuliozelante.tekken7.offline.tg.bot.meetup.model.Commands;
import it.giuliozelante.tekken7.offline.tg.bot.meetup.service.CommandService;
import it.giuliozelante.tekken7.offline.tg.bot.meetup.service.GroupService;
import it.giuliozelante.tekken7.offline.tg.bot.meetup.service.PollService;
import it.giuliozelante.tekken7.offline.tg.bot.meetup.service.VirusTotalApiClient;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Bean
@RequiredArgsConstructor
@Getter
public class MeetUp extends TelegramLongPollingBot {
    @Value("${telegram.bot.username}")
    private String botUsername;
    @Value("${telegram.bot.token}")
    private String botToken;
    @Inject
    protected GroupService groupService;
    @Inject
    protected PollService pollService;
    @Inject
    protected CommandService commandService;

    @Inject
    protected VirusTotalApiClient virusTotalApiClient;
    private final Pattern URL_PATTERN = Pattern.compile("\\b[a-zA-Z0-9]+:\\/\\/[^\\s,)]+");

    @Override
    @Transactional
    public void onUpdateReceived(Update update) {
        if (!update.hasMessage() || !update.getMessage().hasText()) {
            return;
        }

        long chatId = update.getMessage().getChatId();
        TelegramGroup group = groupService.findByChatId(chatId).orElse(null);
        String textMessage = update.getMessage().getText();
        if (update.getMessage().isCommand()) {
            if (isAdmin(update, chatId)) {
                Commands command = Commands.valueOf(textMessage.substring(1).toUpperCase());
                switch (command) {
                    case START_MEET_UP_SCHEDULE:
                        handleStartMeetUpSchedule(group, chatId);
                        break;
                    case START_MEET_UP_POLL:
                        handleStartMeetUpPoll(group);
                        break;
                    case STOP_MEET_UP:
                        handleStopMeetUp(group, chatId);
                        break;
                    case HELP_MEET_UP:
                        handleHelp(chatId);
                        break;
                    default:
                        handleDefault(chatId);
                        break;
                }
            }
        } else {
            handleOtherMessages(chatId, textMessage, update.getMessage().getMessageId());
        }

    }

    private boolean isUrlMalicious(String url) {
        boolean isUrlMalicious = virusTotalApiClient
                .getUrlOrFileAnalysis(virusTotalApiClient.scanUrl(url).data().id()).data().attributes()
                .stats().get("malicious") > 0;
        log.debug("{} is malicious: {}", url, isUrlMalicious);
        return isUrlMalicious;
    }

    private void handleStartMeetUpSchedule(TelegramGroup group, long chatId) {
        if (group == null || !group.isStarted()) {
            if (group == null) {
                group = new TelegramGroup();
                group.setChatId(chatId);
            }
            List<KeyboardRow> keyboardRows = List.of(new KeyboardRow(List.of(new KeyboardButton("Question"))));
            ReplyKeyboardMarkup replyKeyboardMarkup = new ReplyKeyboardMarkup(keyboardRows);
            sendMessage(chatId, "Poll details", replyKeyboardMarkup);
            group.setStarted(true);
            groupService.save(group);
            sendMessage(chatId, getStartMeetUpMessage());
        } else {
            sendMessage(chatId, "Already started Scheduled Job for the meetings");
        }
    }

    private void handleStartMeetUpPoll(TelegramGroup group) {
        this.pollService.stopPoll(group, this);
        this.pollService.startPoll(group, this);
    }

    private void handleStopMeetUp(TelegramGroup group, long chatId) {
        if (group != null && group.isStarted()) {
            group.setStarted(false);
            groupService.save(group);
            sendMessage(chatId, "The scheduled job for the meetings has been stopped");
        }
    }

    private void handleHelp(long chatId) {

        List<Command> commands = this.commandService.getCommands();
        String commandsList = commands.stream()
                .map(command -> "/" + command.getName() + ": " + command.getDescription())
                .collect(Collectors.joining("\n"));
        StringBuilder message = new StringBuilder("Available commands are: \n");
        message.append(commandsList);
        sendMessage(chatId, message.toString());
    }

    private void handleDefault(long chatId) {
        sendMessage(chatId, "Unhandled command. Please use '/" + Commands.HELP_MEET_UP.toString().toLowerCase()
                + "' to see the list of valid commands.");
    }

    private boolean isAdmin(Update update, Long chatId) {
        return getChatAdministrators(chatId)
                .stream()
                .map(ChatMember::getUser)
                .anyMatch(user -> user.equals(update.getMessage().getFrom()));
    }

    private void handleOtherMessages(Long chatId, String textMessage, Integer messageId) {
        // check if the message is an url
        if (URL_PATTERN.matcher(textMessage).matches() && (isUrlMalicious(textMessage))) {
            sendMessage(chatId, "The url " + textMessage + " is malicious", messageId);

        }
    }

    private String getStartMeetUpMessage() {
        StringBuilder sb = new StringBuilder("Started Scheduled Job for the meetings.\n");
        sb.append("There will be a new Poll every Monday.\n");
        sb.append("Every thursday at midnight the poll will be closed");
        sb.append("To stop the scheduled job write /stopMeetUp.\n");
        return sb.toString();
    }

    private List<ChatMember> getChatAdministrators(Long chatId) {
        List<ChatMember> chatAdministrators = Collections.emptyList();
        try {
            chatAdministrators = execute(new GetChatAdministrators(String.valueOf(chatId)));
        } catch (TelegramApiException e) {
            log.error(e.getMessage(), e);
        }
        return chatAdministrators;
    }

    private void sendMessage(Long chatId, String textMessage) {
        this.sendMessage(chatId, textMessage, null, null);
    }

    private void sendMessage(Long chatId, String textMessage, Integer messageId) {
        this.sendMessage(chatId, textMessage, null, messageId);
    }

    private void sendMessage(Long chatId, String textMessage, ReplyKeyboard replyMarkup) {
        this.sendMessage(chatId, textMessage, replyMarkup, null);
    }

    private void sendMessage(Long chatId, String textMessage, ReplyKeyboard replyMarkup, Integer messageId) {
        SendMessage message = new SendMessage();
        message.setChatId(String.valueOf(chatId));
        message.setText(textMessage);
        if (replyMarkup != null)
            message.setReplyMarkup(replyMarkup);
        if (messageId != null)
            message.setReplyToMessageId(messageId);
        try {
            execute(message);
        } catch (TelegramApiException e) {
            log.error(e.getMessage(), e);
        }

    }
}