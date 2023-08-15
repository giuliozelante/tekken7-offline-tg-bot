package it.giuliozelante.tekken7.offline.tg.bot.meetup.entity;

import java.time.LocalDateTime;
import java.util.Set;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

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