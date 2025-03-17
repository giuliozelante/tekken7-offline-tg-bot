package it.giuliozelante.tekken7.offline.tg.bot.meetup.service;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.atomic.AtomicReference;

import org.telegram.telegrambots.meta.api.methods.pinnedmessages.PinChatMessage;
import org.telegram.telegrambots.meta.api.methods.pinnedmessages.UnpinChatMessage;
import org.telegram.telegrambots.meta.api.methods.polls.SendPoll;
import org.telegram.telegrambots.meta.api.methods.polls.StopPoll;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import it.giuliozelante.tekken7.offline.tg.bot.meetup.MeetUp;
import it.giuliozelante.tekken7.offline.tg.bot.meetup.entity.Poll;
import it.giuliozelante.tekken7.offline.tg.bot.meetup.entity.TelegramGroup;
import it.giuliozelante.tekken7.offline.tg.bot.meetup.repository.PollRepository;
import jakarta.inject.Singleton;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Singleton
@Slf4j
@RequiredArgsConstructor
public class PollService {
    private final PollRepository pollRepository;
    private final List<String> options = generateNextWeekdayOptions().stream()
            .map(day -> {
                String location = day.toLowerCase().contains("martedì") ? "(Parrot Sushi Lan)" : "(Rampage)";
                return day + " " + location;
            })
            .toList();

    private List<String> generateNextWeekdayOptions() {
        LocalDate today = LocalDate.now();
        List<String> nextWeekdays = new ArrayList<>();
        DayOfWeek[] daysToCheck = {DayOfWeek.TUESDAY, DayOfWeek.SATURDAY, DayOfWeek.SUNDAY};

        for (DayOfWeek day : daysToCheck) {
            LocalDate nextDay = today.with(TemporalAdjusters.nextOrSame(day));
            nextWeekdays.add(nextDay.format(DateTimeFormatter.ofPattern("EEEE d MMMM", Locale.ITALIAN)));
        }

        return nextWeekdays;
    }

    public void startPoll(TelegramGroup group, MeetUp meetUp) {
        try {
            SendPoll meetingDay = new SendPoll(String.valueOf(group.getChatId()), "Quando ci vediamo?",
                    options);
            meetingDay.setIsAnonymous(false);
            meetingDay.setAllowMultipleAnswers(true);
            Message pollMessage = meetUp.execute(meetingDay);
            PinChatMessage pinChatMessage = new PinChatMessage(String.valueOf(group.getChatId()),
                    pollMessage.getMessageId());
            log.info("Poll with id {} has been started", pollMessage.getMessageId());
            boolean pinned = meetUp.execute(pinChatMessage);
            if (pinned) {
                log.info("Poll with id {} has been pinned", pollMessage.getMessageId());
            } else {
                log.warn("Poll with id {} has NOT been pinned", pollMessage.getMessageId());
            }

            Poll poll = new Poll();
            poll.setMessageId(pollMessage.getMessageId().longValue());
            poll.setTelegramGroup(group);
            poll.setActive(true);
            pollRepository.save(poll);
        } catch (TelegramApiException e) {
            log.error(e.getMessage(), e);
        }
    }

    public void stopPoll(TelegramGroup group, MeetUp meetUp) {
        List<Poll> polls = pollRepository.findByTelegramGroupAndActive(group, true);
        AtomicReference<List<Poll>> updatedPolls = new AtomicReference<>(new ArrayList<>());
        polls.parallelStream().forEachOrdered(poll -> {
            try {
                StopPoll meetingDay = new StopPoll(String.valueOf(group.getChatId()),
                        poll.getMessageId().intValue());

                poll.setActive(false);
                updatedPolls.get().add(poll);
                meetUp.execute(meetingDay);
                log.info("Poll with id {} has been stopped", meetingDay.getMessageId());
                UnpinChatMessage unpinChatMessage = new UnpinChatMessage(String.valueOf(group.getChatId()),
                        meetingDay.getMessageId());
                boolean unpinned = meetUp.execute(unpinChatMessage);
                if (unpinned) {
                    log.info("Poll with id {} has been unpinned", meetingDay.getMessageId());
                } else {
                    log.warn("Poll with id {} has NOT been unpinned", meetingDay.getMessageId());
                }
            } catch (TelegramApiException e) {
                log.error(e.getMessage(), e);
            }
        });

        pollRepository.updateAll(updatedPolls.get());
    }

    public List<Poll> findByTelegramGroupAndActive(TelegramGroup group, boolean active) {
        return pollRepository.findByTelegramGroupAndActive(group, active);
    }

    public List<Poll> findAll() {
        return pollRepository.findAll();
    }

    public void updateAll(List<Poll> list) {
        this.pollRepository.updateAll(list);
    }
}
