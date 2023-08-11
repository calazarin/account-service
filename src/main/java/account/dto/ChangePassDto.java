package account.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ChangePassDto {

    @JsonProperty("new_password")
    @NotEmpty(message = "Password cannot be null or empty")
    @Size(min=12, message = "Password length must be 12 chars minimum!")
    private String newPassword;
}
