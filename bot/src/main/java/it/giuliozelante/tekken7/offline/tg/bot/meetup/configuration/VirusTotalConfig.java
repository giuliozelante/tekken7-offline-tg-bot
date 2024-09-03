package it.giuliozelante.tekken7.offline.tg.bot.meetup.configuration;

import io.micronaut.context.annotation.ConfigurationProperties;
import lombok.Getter;
import lombok.Setter;

@ConfigurationProperties(VirusTotalConfig.PREFIX)
@Getter
@Setter
public class VirusTotalConfig {
    public static final String PREFIX = "virustotal";
    private String apiKey;
    private String apiRoot;

}
