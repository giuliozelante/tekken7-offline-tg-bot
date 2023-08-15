package it.giuliozelante.tekken7.offline.tg.bot.meetup.repository;

import java.util.Optional;

import io.micronaut.data.annotation.Repository;
import io.micronaut.data.repository.PageableRepository;
import it.giuliozelante.tekken7.offline.tg.bot.meetup.entity.TelegramGroup;

@Repository
public interface GroupRepository extends PageableRepository<TelegramGroup, Long> {
    Optional<TelegramGroup> findByChatId(Long chatId);
}
