package com.campus.eventhub.web;

import com.campus.eventhub.domain.Participant;
import com.campus.eventhub.service.ParticipantService;
import com.campus.eventhub.web.dto.ParticipantResponse;
import org.springframework.data.domain.*;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/participants")
public class ParticipantController {

    private final ParticipantService participantService;
    public ParticipantController(ParticipantService participantService) {
        this.participantService = participantService;
    }

    @GetMapping
    public ResponseEntity<Page<ParticipantResponse>> getAllParticipants(
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "fullName,asc") String[] sort
    ) {
        int boundedSize = Math.min(size, 50);
        String sortField = sort[0];
        Sort.Direction direction = sort.length > 1 && "desc".equalsIgnoreCase(sort[1])
                ? Sort.Direction.DESC : Sort.Direction.ASC;
        Sort deterministicSort = Sort.by(direction, sortField).and(Sort.by(Sort.Direction.ASC, "id"));

        Pageable pageable = PageRequest.of(page, boundedSize, deterministicSort);
        Page<Participant> participantPage = participantService.search(keyword, pageable);

        Page<ParticipantResponse> dtoPage = participantPage.map(p -> new ParticipantResponse(
                p.getId(), p.getFullName(), p.getEmail(), p.getCreatedAt()
        ));

        return ResponseEntity.ok(dtoPage);
    }
}