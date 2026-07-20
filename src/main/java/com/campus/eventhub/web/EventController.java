package com.campus.eventhub.web;

import com.campus.eventhub.domain.Event;
import com.campus.eventhub.domain.EventStatus;
import com.campus.eventhub.service.EventService;
import com.campus.eventhub.web.dto.*;
import jakarta.validation.Valid;
import org.springframework.data.domain.*;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import java.net.URI;
import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/v1/events")
public class EventController {

    private static final int MAX_PAGE_SIZE = 50;
    private final EventService eventService;
    public EventController(EventService eventService) {
        this.eventService = eventService;
    }

    @GetMapping
    public ResponseEntity<Page<EventListResponse>> getAllEvents(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) EventStatus status,
            @RequestParam(required = false) LocalDateTime startFrom,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "startAt,asc") String[] sort
    ) {
        int boundedSize = Math.min(size, MAX_PAGE_SIZE);
        String sortField = sort[0];
        Sort.Direction direction = sort.length > 1 && "desc".equalsIgnoreCase(sort[1])
                ? Sort.Direction.DESC : Sort.Direction.ASC;

        Sort deterministicSort = Sort.by(direction, sortField).and(Sort.by(Sort.Direction.ASC, "id"));
        Pageable pageable = PageRequest.of(page, boundedSize, deterministicSort);

        Page<Event> eventPage = eventService.search(keyword, status, startFrom, pageable);
        Page<EventListResponse> dtoPage = eventPage.map(e -> new EventListResponse(
                e.getId(), e.getTitle(), e.getStartAt(), e.getAvailableSeats(), e.getStatus()
        ));
        return ResponseEntity.ok(dtoPage);
    }

    @GetMapping("/{id}")
    public ResponseEntity<EventDetailResponse> getEventById(@PathVariable Long id) {
        Event e = eventService.findById(id);
        EventDetailResponse dto = new EventDetailResponse(
                e.getId(), e.getTitle(), e.getDescription(), e.getLocation(),
                e.getStartAt(), e.getCapacity(), e.getAvailableSeats(), e.getStatus(), e.getCreatedAt()
        );
        return ResponseEntity.ok(dto);
    }
    @PostMapping
    public ResponseEntity<EventDetailResponse> createEvent(@Valid @RequestBody CreateEventRequest request) {
        Event savedEvent = eventService.create(request);

        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(savedEvent.getId())
                .toUri();

        EventDetailResponse dto = new EventDetailResponse(
                savedEvent.getId(), savedEvent.getTitle(), savedEvent.getDescription(), savedEvent.getLocation(),
                savedEvent.getStartAt(), savedEvent.getCapacity(), savedEvent.getAvailableSeats(), savedEvent.getStatus(), savedEvent.getCreatedAt()
        );

        return ResponseEntity.created(location).body(dto);
    }
}