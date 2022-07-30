package it.giuliozelante.tekken7.offline.tg.bot.meetup.repository;

import io.micronaut.data.annotation.Repository;
import io.micronaut.data.repository.PageableRepository;
import it.giuliozelante.tekken7.offline.tg.bot.meetup.entity.TelegramGroup;
import it.giuliozelante.tekken7.offline.tg.bot.meetup.entity.Poll;

import java.util.Optional;

@Repository
public interface PollRepository extends PageableRepository<Poll, Long> {
    Optional<Poll> findByTelegramGroup(TelegramGroup group);
}
