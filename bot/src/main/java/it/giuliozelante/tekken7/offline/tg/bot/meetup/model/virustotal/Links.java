package it.giuliozelante.tekken7.offline.tg.bot.meetup.model.virustotal;

import io.micronaut.serde.annotation.Serdeable;

@Serdeable
public record Links(String self, String item) {
}