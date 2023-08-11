package account.dto;

import account.entity.Payment;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class PaymentDto {

    @Email(message = "Not a valid e-mail format", regexp = "^[a-zA-Z0-9_\\.]+@acme.com$")
    @NotEmpty(message = "Email cannot be null or empty")
    private String employee;
    @Pattern(regexp = "^(0?[1-9]|1[012])-[1-9][0-9]{3}$", message = "Period must have format as MM-YYYY!")
    private String period;
    @Positive(message = "Salary must be greater than zero!")
    private Long salary;

    public static PaymentDto toDto(Payment payment){
        return new PaymentDto(payment.getUser().getUsername(), payment.getPeriod(), payment.getSalary());
    }

}