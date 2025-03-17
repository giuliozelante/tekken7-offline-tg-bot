package it.giuliozelante.tekken7.offline.tg.bot.meetup;

import java.io.InputStream;
import java.util.Collections;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.GetFile;
import org.telegram.telegrambots.meta.api.methods.groupadministration.GetChatAdministrators;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.Voice;
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
import it.giuliozelante.tekken7.offline.tg.bot.meetup.service.CommandService;
import it.giuliozelante.tekken7.offline.tg.bot.meetup.service.GroupService;
import it.giuliozelante.tekken7.offline.tg.bot.meetup.service.PollService;
import it.giuliozelante.tekken7.offline.tg.bot.meetup.service.VirusTotalApiClient;
import it.giuliozelante.tekken7.offline.tg.bot.meetup.service.speech.SpeechToTextService;
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
    private final GroupService groupService;
    private final PollService pollService;
    private final CommandService commandService;
    private final VirusTotalApiClient virusTotalApiClient;
    private final SpeechToTextService speechToTextService;

    private final Pattern urlPattern = Pattern.compile(
            "[(http(s)?):\\/\\/(www\\.)?a-zA-Z0-9@:%._\\+~#=]{2,256}\\.[a-z]{2,6}\\b([-a-zA-Z0-9@:%_\\+.~#?&//=]*)");

    @Override
    @Transactional
    public void onUpdateReceived(Update update) {
        if (!update.hasMessage()) {
            return;
        }

        long chatId = update.getMessage().getChatId();
        TelegramGroup group = groupService.findByChatId(chatId).orElse(null);

        // Handle voice messages
        if (update.getMessage().hasVoice()) {
            handleVoiceMessage(update, chatId);
            return;
        }

        // Handle text messages
        if (update.getMessage().hasText()) {
            String textMessage = update.getMessage().getText();
            if (update.getMessage().isCommand()) {
                if (isAdmin(update, chatId)) {
                    textMessage = textMessage.substring(1);
                    switch (textMessage) {
                        case "start_meet_up" -> handleStartMeetUp(group, chatId);
                        case "start_meet_up_poll" -> handleStartMeetUpPoll(group);
                        case "stop_meet_up" -> handleStopMeetUp(group, chatId);
                        case "help_meet_up" -> handleHelp(chatId);
                        case "voice_message_help" -> handleVoiceMessageHelp(chatId);
                        default -> handleDefault(chatId);
                    }
                }
            } else {
                handleOtherMessages(chatId, textMessage, update.getMessage().getMessageId());
            }
        }
    }

    /**
     * Handles voice messages by converting them to text and processing the text.
     * 
     * @param update The update containing the voice message
     * @param chatId The chat ID where the voice message was sent
     */
    private void handleVoiceMessage(Update update, long chatId) {
        try {
            Voice voice = update.getMessage().getVoice();
            log.info("Received voice message: duration={} seconds, mime-type={}",
                    voice.getDuration(), voice.getMimeType());

            // Get the voice file
            GetFile getFile = new GetFile();
            getFile.setFileId(voice.getFileId());
            org.telegram.telegrambots.meta.api.objects.File voiceFile = execute(getFile);

            // Download the voice file
            InputStream voiceStream = downloadFileAsStream(voiceFile);

            // Convert speech to text
            String recognizedText = speechToTextService.convertSpeechToText(voiceStream);

            if (recognizedText != null && !recognizedText.isEmpty()) {
                log.info("Voice message converted to text: {}", recognizedText);

                // Reply with the recognized text
                sendMessage(chatId, "\"" + recognizedText + "\"", update.getMessage().getMessageId());

                // Process the recognized text as if it was a text message
                if (recognizedText.startsWith("/")) {
                    // Handle as command
                    update.getMessage().setText(recognizedText);
                    onUpdateReceived(update);
                } else {
                    // Handle as regular message
                    handleOtherMessages(chatId, recognizedText, update.getMessage().getMessageId());
                }
            } else {
                log.warn("Could not recognize speech in voice message");
                sendMessage(chatId, "Sorry, I couldn't understand what you said.", update.getMessage().getMessageId());
            }
        } catch (TelegramApiException e) {
            log.error("Error processing voice message", e);
            sendMessage(chatId, "Sorry, there was an error processing your voice message.",
                    update.getMessage().getMessageId());
        }
    }

    private boolean isUrlMalicious(String url) {
        boolean isUrlMalicious = virusTotalApiClient
                .getUrlOrFileAnalysis(virusTotalApiClient.scanUrl(url).data().id()).data().attributes()
                .stats().get("malicious") > 0;
        log.debug("{} is malicious: {}", url, isUrlMalicious);
        return isUrlMalicious;
    }

    private void handleStartMeetUp(TelegramGroup group, long chatId) {
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

    private void handleVoiceMessageHelp(long chatId) {
        StringBuilder message = new StringBuilder("Voice Message Recognition Feature:\n\n");
        message.append("This bot can now understand voice messages! Here's how it works:\n\n");
        message.append("1. Send a voice message to the bot\n");
        message.append("2. The bot will convert your speech to text using CMU Sphinx\n");
        message.append("3. The bot will respond to your voice message as if you had typed it\n\n");
        message.append("You can even send commands via voice messages by starting with a slash (/)\n\n");
        message.append(
                "Note: This feature is optimized for Italian language recognition, but also supports English as a fallback.\n");
        message.append("Speech recognition works best in quiet environments with clear speech.");
        sendMessage(chatId, message.toString());
    }

    private void handleHelp(long chatId) {
        List<Command> commands = this.commandService.getCommands();
        String commandsList = commands.stream().map(command -> command.getName() + ": " + command.getDescription())
                .collect(Collectors.joining("\n"));
        StringBuilder message = new StringBuilder("Available commands are: \n");
        message.append(commandsList);
        sendMessage(chatId, message.toString());
    }

    private void handleDefault(long chatId) {
        sendMessage(chatId, "Unhandled command. Please use '/helpMeetUp' to see the list of valid commands.");
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