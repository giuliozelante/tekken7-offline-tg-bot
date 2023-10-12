package it.giuliozelante.tekken7.offline.tg.bot.meetup.model.virustotal;

import java.util.Map;

import io.micronaut.serde.annotation.Serdeable;

@Serdeable
public record Attributes(long date, String status, Map<String, Integer> stats) {

}
