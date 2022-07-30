package it.giuliozelante.tekken7.offline.tg.bot.meetup.repository;

import io.micronaut.data.annotation.Repository;
import io.micronaut.data.repository.PageableRepository;
import it.giuliozelante.tekken7.offline.tg.bot.meetup.entity.TelegramGroup;

import java.util.Optional;
@Repository
public interface GroupRepository extends PageableRepository<TelegramGroup, Long> {
    Optional<TelegramGroup> findByChatId(Long chatId);
}
