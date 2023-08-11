package account.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class StatusDto {

    @JsonProperty("user")
    private String userEmail;
    private String status;

    public StatusDto(String status) {
        this.status = status;
    }

    public StatusDto(String userEmail, String status) {
        this.userEmail = userEmail;
        this.status = status;
    }
}
