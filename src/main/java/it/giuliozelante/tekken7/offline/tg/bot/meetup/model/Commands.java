package it.giuliozelante.tekken7.offline.tg.bot.meetup.model;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public enum Commands {
    START_MEET_UP_SCHEDULE,
    START_MEET_UP_POLL,
    STOP_MEET_UP,
    HELP_MEET_UP,
    UNHANDLED;
}
