package account.dto;

import account.entity.SecurityEvent;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.time.LocalDate;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
@EqualsAndHashCode
public class SecurityEventDto {

    private Long id;
    private LocalDate date;
    private String action;
    private String subject;
    private String object;
    private String path;

    public static SecurityEventDto toDto(SecurityEvent event){
        return new SecurityEventDto(event.getId(), event.getDate(), event.getAction(),
                event.getSubject(), event.getObject(), event.getPath());
    }
}
