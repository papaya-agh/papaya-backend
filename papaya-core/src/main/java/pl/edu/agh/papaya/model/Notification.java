package pl.edu.agh.papaya.model;

import java.time.LocalDateTime;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Entity
public class Notification extends BaseEntity {

    @ManyToOne
    @JoinColumn(nullable = false)
    private Sprint sprint;

    @Column(nullable = false)
    private LocalDateTime lastNotificationDate;

    @Enumerated(EnumType.STRING)
    @Column(length = 64, nullable = false)
    private NotificationType type;
}
