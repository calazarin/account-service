package account.dto;

import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class RoleActionDto {

    @NotEmpty(message = "User cannot be null or empty")
    private String user;
    @NotEmpty(message = "Role cannot be null or empty")
    private String role;
    @NotEmpty(message = "Operation cannot be null or empty")
    private String operation;
}
