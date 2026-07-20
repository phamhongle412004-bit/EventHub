package com.campus.eventhub.service;

import com.campus.eventhub.domain.*;
import com.campus.eventhub.repository.*;
import com.campus.eventhub.web.dto.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.Clock;
import java.time.LocalDateTime;

@Service
@Transactional(readOnly = true)
public class EventService {
    private final EventRepository eventRepository;
    private final Clock clock;

    public EventService(EventRepository eventRepository, Clock clock) {
        this.eventRepository = eventRepository;
        this.clock = clock;
    }
    public Page<Event> search(String keyword, EventStatus status, LocalDateTime startFrom, Pageable pageable) {
        return eventRepository.searchEvents(keyword, status, startFrom, pageable);
    }
    public Event findById(Long id) {
        return eventRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy sự kiện với ID: " + id));
    }
    @Transactional
    public Event create(CreateEventRequest request) {
        Event event = new Event();
        event.setTitle(request.title());
        event.setDescription(request.description());
        event.setLocation(request.location());
        event.setStartAt(request.startAt());
        event.setCapacity(request.capacity());
        event.setAvailableSeats(request.capacity());
        event.setStatus(EventStatus.DRAFT);
        event.setCreatedAt(LocalDateTime.now(clock));
        return eventRepository.save(event);
    }
}