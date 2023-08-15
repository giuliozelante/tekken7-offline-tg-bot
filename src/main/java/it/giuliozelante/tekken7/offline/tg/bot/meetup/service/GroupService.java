package it.giuliozelante.tekken7.offline.tg.bot.meetup.service;

import java.util.List;
import java.util.Optional;

import io.micronaut.core.annotation.NonNull;
import it.giuliozelante.tekken7.offline.tg.bot.meetup.entity.TelegramGroup;
import it.giuliozelante.tekken7.offline.tg.bot.meetup.repository.GroupRepository;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;

@Singleton
public class GroupService {
    @Inject
    private GroupRepository groupRepository;

    public @NonNull List<TelegramGroup> findAll() {
        return this.groupRepository.findAll();
    }

    public Optional<TelegramGroup> findByChatId(Long chatId) {
        return this.groupRepository.findByChatId(chatId);
    }

    public void save(TelegramGroup group) {
        this.groupRepository.save(group);
    }
}
