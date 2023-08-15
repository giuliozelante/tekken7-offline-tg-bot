package it.giuliozelante.tekken7.offline.tg.bot.meetup.repository;

import java.util.List;
import java.util.Optional;

import io.micronaut.data.annotation.Repository;
import io.micronaut.data.repository.PageableRepository;
import it.giuliozelante.tekken7.offline.tg.bot.meetup.entity.Poll;
import it.giuliozelante.tekken7.offline.tg.bot.meetup.entity.TelegramGroup;

@Repository
public interface PollRepository extends PageableRepository<Poll, Long> {
    Optional<Poll> findByTelegramGroup(TelegramGroup group);
    List<Poll> findByTelegramGroupAndActive(TelegramGroup group, Boolean active);
}
