package it.giuliozelante.tekken7.offline.tg.bot.meetup.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

/*
 * "'startMeetUp': Starts a new meet up. If a meet up is already started, it will inform you.\n" +
    "'startMeetUpPoll': even if there's an active poll it will close all active ones and start a new one for the current week.\n"
    +
    "'stopMeetUp': Stops the current meet up if it's started.\n" +
    "'helpMeetUp': Shows this help message.\n" +
 *
 */
@Entity
@Getter
@Setter
@NoArgsConstructor
@ToString
public class Command {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;
    private String name;
    private String description;
}
