package it.giuliozelante.tekken7.offline.tg.bot.meetup.model.virustotal;

import io.micronaut.serde.annotation.Serdeable;

@Serdeable
public record UrlInfo(String url, String id) {
}
