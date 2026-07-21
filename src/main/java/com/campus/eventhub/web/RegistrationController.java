package com.campus.eventhub.web;

import com.campus.eventhub.domain.Registration;
import com.campus.eventhub.domain.RegistrationStatus;
import com.campus.eventhub.service.RegistrationService;
import com.campus.eventhub.web.dto.CreateRegistrationRequest;
import com.campus.eventhub.web.dto.RegistrationResponse;
import jakarta.validation.Valid;
import org.springframework.data.domain.*;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import java.net.URI;

@RestController
@RequestMapping("/api/v1")
public class RegistrationController {

    private final RegistrationService registrationService;
    public RegistrationController(RegistrationService registrationService) {
        this.registrationService = registrationService;
    }
    @GetMapping("/registrations")
    public ResponseEntity<Page<RegistrationResponse>> getAllRegistrations(
            @RequestParam(required = false) Long eventId,
            @RequestParam(required = false) RegistrationStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "registeredAt,desc") String[] sort
    ) {
        int boundedSize = Math.min(size, 50);

        String sortField = sort[0];
        Sort.Direction direction = sort.length > 1 && "desc".equalsIgnoreCase(sort[1])
                ? Sort.Direction.DESC : Sort.Direction.ASC;
        Sort deterministicSort = Sort.by(direction, sortField).and(Sort.by(Sort.Direction.ASC, "id"));

        Pageable pageable = PageRequest.of(page, boundedSize, deterministicSort);
        Page<Registration> registrationPage = registrationService.search(eventId, status, pageable);

        Page<RegistrationResponse> dtoPage = registrationPage.map(r -> new RegistrationResponse(
                r.getId(), r.getEvent().getId(), r.getEvent().getTitle(),
                r.getParticipant().getId(), r.getParticipant().getFullName(),
                r.getRegisteredAt(), r.getStatus()
        ));

        return ResponseEntity.ok(dtoPage);
    }
    @PostMapping("/events/{eventId}/registrations")
    public ResponseEntity<RegistrationResponse> registerEvent(
            @PathVariable Long eventId,
            @Valid @RequestBody CreateRegistrationRequest request
    ) {
        Registration reg = registrationService.register(eventId, request);

        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(reg.getId())
                .toUri();

        RegistrationResponse dto = new RegistrationResponse(
                reg.getId(), reg.getEvent().getId(), reg.getEvent().getTitle(),
                reg.getParticipant().getId(), reg.getParticipant().getFullName(),
                reg.getRegisteredAt(), reg.getStatus()
        );

        return ResponseEntity.created(location).body(dto);
    }
    @DeleteMapping("/registrations/{id}")
    public ResponseEntity<Void> cancelRegistration(@PathVariable Long id) {
        return ResponseEntity.noContent().build();
    }
}