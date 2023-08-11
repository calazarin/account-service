package account.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.time.LocalDate;

@Getter
@Setter
@Entity
@AllArgsConstructor
@ToString
@NoArgsConstructor
public class SecurityEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private LocalDate date;
    private String action;
    private String subject;
    private String object;
    private String path;

    public SecurityEvent(String action, String subject, String object, String path) {
        this.date = LocalDate.now();
        this.action = action;
        this.subject = subject;
        this.object = object;
        this.path = path;
    }
}
