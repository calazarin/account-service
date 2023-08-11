package account.controller;

import account.dto.PaymentDetailsDto;
import account.entity.AppUser;
import account.service.PaymentService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.constraints.Pattern;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import java.util.List;
import java.util.Optional;

@Validated
@RestController
public class EmployeeController {

    private PaymentService paymentService;

    @Autowired
    public EmployeeController(PaymentService paymentService){
        this.paymentService = paymentService;
    }

    @Operation(summary = "Lists all logged in user´s payments in a specific period")
    @GetMapping("/api/empl/payment")
    public List<PaymentDetailsDto> payments(@AuthenticationPrincipal AppUser details,
                                            @RequestParam(required = false)
                                            @Pattern(regexp = "^[0-9]{1,2}-[1-9][0-9]{3}$") String period){
        return paymentService.findUserPayments(Optional.ofNullable(period), details.getUsername());
    }
}
