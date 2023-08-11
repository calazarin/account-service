package account.controller;

import account.dto.PaymentDto;
import account.dto.StatusDto;
import account.entity.AppUser;
import account.service.PaymentService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Validated
@RestController
public class PaymentController {

    private final PaymentService paymentService;

    @Autowired
    public PaymentController(PaymentService paymentService){
        this.paymentService = paymentService;
    }

    @Operation(summary = "Adds a new payment to logged in user")
    @PostMapping("/api/acct/payments")
    public StatusDto addPayments(@AuthenticationPrincipal AppUser details,
                                 @RequestBody List<@Valid PaymentDto> paymentDtos){
        paymentService.addNewPayments(paymentDtos);
        return new StatusDto("Added successfully!");
    }

    @Operation(summary = "Updates user´s payments")
    @PutMapping("/api/acct/payments")
    public StatusDto updatePayments(@RequestBody PaymentDto paymentDto){
        paymentService.updateUserPayment(paymentDto.getEmployee(), paymentDto.getPeriod(), paymentDto.getSalary());
        return new StatusDto("Updated successfully!");
    }

}
