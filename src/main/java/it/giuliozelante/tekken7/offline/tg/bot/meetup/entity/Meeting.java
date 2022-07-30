package it.giuliozelante.tekken7.offline.tg.bot.meetup.entity;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.Set;

@Entity
@Getter
@Setter
@NoArgsConstructor
@ToString
public class Meeting {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    private String name;
    private String description;
    private LocalDateTime dateTime;

    private Double latitude;
    private Double longitude;

    @OneToMany(mappedBy = "meeting")
    @ToString.Exclude
    private Set<Participant> participants = new java.util.LinkedHashSet<>();
}