package account.controller;

import account.dto.SecurityEventDto;
import account.service.SecurityEventsService;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Collectors;

@RestController
public class SecurityEventsController {

    private final SecurityEventsService securityEventsService;

    @Autowired
    public SecurityEventsController(SecurityEventsService securityEventsService) {
        this.securityEventsService = securityEventsService;
    }

    @Operation(summary = "Retrieves all events in the system for auditing purposes")
    @GetMapping("/api/security/events/")
    public List<SecurityEventDto> getAllSecurityEvents(){
        return securityEventsService.getAllSecurityEvents().stream()
                .map(SecurityEventDto::toDto).collect(Collectors.toList());
    }
}
