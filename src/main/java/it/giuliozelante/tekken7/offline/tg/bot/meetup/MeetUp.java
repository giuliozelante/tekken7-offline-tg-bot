package it.giuliozelante.tekken7.offline.tg.bot.meetup;

import java.text.DateFormatSymbols;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.groupadministration.GetChatAdministrators;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.chatmember.ChatMember;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import io.micronaut.context.annotation.Bean;
import io.micronaut.context.annotation.Value;
import io.micronaut.core.util.StringUtils;
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
    private static KeyboardRow createKeyboardRow(String weekday) {
        KeyboardRow row = new KeyboardRow();
        KeyboardButton button = new KeyboardButton(weekday);
        row.add(button);
        return row;
    }

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

    private final Pattern urlPattern = Pattern.compile("\\b[a-zA-Z0-9]+:\\/\\/[^\\s,)]+");

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
                Commands command;
                try {
                    command = Commands.valueOf(textMessage.substring(1).toUpperCase());

                    String languageCode = update.getMessage().getFrom().getLanguageCode();
                    switch (command) {
                        case START_MEET_UP_SCHEDULE:
                            handleStartMeetUpSchedule(group, chatId, languageCode);
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
                } catch (IllegalArgumentException e) {
                    log.debug(Commands.UNHANDLED.toString(), e);
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

    private void handleStartMeetUpSchedule(TelegramGroup group, long chatId, String languageCode) {
        if (group == null || !group.isStarted()) {
            if (group == null) {
                group = new TelegramGroup();
                group.setChatId(chatId);
            }
            if (StringUtils.isEmpty(group.getLanguageCode()))
                group.setLanguageCode(languageCode);
            List<KeyboardRow> keyboardRows = Arrays
                    .stream(new DateFormatSymbols(new Locale(group.getLanguageCode())).getWeekdays())
                    .filter(wd -> !wd.equals(""))
                    .map(wd -> wd.substring(0, 1).toUpperCase() + wd.substring(1))
                    .map(MeetUp::createKeyboardRow)
                    .collect(Collectors.toList());
            keyboardRows.add(MeetUp.createKeyboardRow("Done"));
            ReplyKeyboardMarkup replyKeyboardMarkup = ReplyKeyboardMarkup.builder().keyboard(keyboardRows).build();
            sendMessage(
                    SendMessage.builder().text("Poll details").chatId(chatId).replyMarkup(replyKeyboardMarkup).build());

            group.setStarted(true);
            groupService.save(group);
            sendMessage(SendMessage.builder().text(getStartMeetUpMessage()).chatId(chatId).build());
        } else {
            sendMessage(SendMessage.builder().text("Already started Scheduled Job for the meetings").chatId(chatId)
                    .build());
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
            sendMessage(SendMessage.builder().text("The scheduled job for the meetings has been stopped").chatId(chatId)
                    .build());
        }
    }

    private void handleHelp(long chatId) {

        List<Command> commands = this.commandService.getCommands();
        String commandsList = commands.stream()
                .map(command -> "/" + command.getName() + ": " + command.getDescription())
                .collect(Collectors.joining("\n"));
        StringBuilder message = new StringBuilder("Available commands are: \n");
        message.append(commandsList);
        sendMessage(SendMessage.builder().chatId(chatId).text(message.toString()).build());
    }

    private void handleDefault(long chatId) {
        sendMessage(SendMessage.builder().chatId(chatId)
                .text("Unhandled command. Please use '/" + Commands.HELP_MEET_UP.toString().toLowerCase()
                        + "' to see the list of valid commands.")
                .build());
    }

    private boolean isAdmin(Update update, Long chatId) {
        return getChatAdministrators(chatId)
                .stream()
                .map(ChatMember::getUser)
                .anyMatch(user -> user.equals(update.getMessage().getFrom()));
    }

    private void handleOtherMessages(Long chatId, String textMessage, Integer messageId) {
        // check if the message is an url
        if (urlPattern.matcher(textMessage).matches() && (isUrlMalicious(textMessage))) {
            sendMessage(SendMessage.builder().chatId(chatId).text(textMessage).messageThreadId(messageId).build());

        }
    }

    private String getStartMeetUpMessage() {
        StringBuilder sb = new StringBuilder("Started Scheduled Job for the meetings.\n");
        sb.append("There will be a new Poll every Monday.\n");
        sb.append("Every thursday at midnight the poll will be closed");
        sb.append("To stop the scheduled job write /" + Commands.STOP_MEET_UP.toString().toLowerCase());
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

    private void sendMessage(SendMessage message) {
        try {
            execute(message);
        } catch (TelegramApiException e) {
            log.error(e.getMessage(), e);
        }

    }

}