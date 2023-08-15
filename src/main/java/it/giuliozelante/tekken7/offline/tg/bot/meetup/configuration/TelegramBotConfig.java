package it.giuliozelante.tekken7.offline.tg.bot.meetup.configuration;

import io.micronaut.context.annotation.ConfigurationProperties;
import lombok.Getter;
import lombok.Setter;

@ConfigurationProperties(TelegramBotConfig.PREFIX)
@Getter
@Setter
public class TelegramBotConfig {
    public static final String PREFIX = "telegram.bot";
    private String token;
    private String username;
}
