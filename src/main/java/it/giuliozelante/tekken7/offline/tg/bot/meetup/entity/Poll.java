package it.giuliozelante.tekken7.offline.tg.bot.meetup.entity;


import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import javax.persistence.*;

@Entity
@Getter
@Setter
@NoArgsConstructor
@ToString
public class Poll {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    private Long messageId;
    @OneToOne
    @JoinColumn(name = "group_id")
    private TelegramGroup telegramGroup;
}