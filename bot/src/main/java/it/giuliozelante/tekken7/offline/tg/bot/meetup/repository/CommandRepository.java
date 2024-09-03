package it.giuliozelante.tekken7.offline.tg.bot.meetup.repository;

import io.micronaut.data.annotation.Repository;
import io.micronaut.data.repository.PageableRepository;
import it.giuliozelante.tekken7.offline.tg.bot.meetup.entity.Command;

@Repository
public interface CommandRepository extends PageableRepository<Command, Long> {
}
