package account.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.io.Serializable;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@ToString
@JsonInclude(JsonInclude.Include.NON_NULL)
public class UserDto implements Serializable {

    private Long id;

    @NotEmpty(message = "Name cannot be null or empty")
    private String name;

    @NotEmpty(message = "Lastname cannot be null or empty")
    private String lastname;

    @Email(message = "Not a valid e-mail format", regexp = "^[a-zA-Z0-9_\\.]+@acme.com$")
    @NotEmpty(message = "Email cannot be null or empty")
    private String email;

    @NotEmpty(message = "Password cannot be null or empty")
    @Size(min=12, message = "The password length must be at least 12 chars!")
    private String password;

    @JsonFormat(without = JsonFormat.Feature.WRITE_SINGLE_ELEM_ARRAYS_UNWRAPPED)
    private List<String> roles;

    public UserDto(Long id, String name, String lastname, String email) {
        this.id = id;
        this.name = name;
        this.lastname = lastname;
        this.email = email;
    }

    public UserDto(Long id, String name, String lastname, String email, String password, List<String> roles) {
        this.id = id;
        this.name = name;
        this.lastname = lastname;
        this.email = email;
        this.password = password;
        this.roles = roles;
    }

    public UserDto(Long id, String name, String lastname, String email, List<String> roles) {
        this.id = id;
        this.name = name;
        this.lastname = lastname;
        this.email = email;
        this.roles = roles;
    }
}
