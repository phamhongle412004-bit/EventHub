package com.campus.eventhub.service;

import com.campus.eventhub.domain.*;
import com.campus.eventhub.repository.EventRepository;
import com.campus.eventhub.repository.RegistrationRepository;
import com.campus.eventhub.web.dto.CreateRegistrationRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.Clock;
import java.time.LocalDateTime;

@Service
@Transactional(readOnly = true)
public class RegistrationService {

    private final RegistrationRepository registrationRepository;
    private final EventRepository eventRepository;
    private final ParticipantService participantService;
    private final Clock clock;

    public RegistrationService(RegistrationRepository registrationRepository,
                               EventRepository eventRepository,
                               ParticipantService participantService,
                               Clock clock) {
        this.registrationRepository = registrationRepository;
        this.eventRepository = eventRepository;
        this.participantService = participantService;
        this.clock = clock;
    }

    public Page<Registration> search(Long eventId, RegistrationStatus status, Pageable pageable) {
        return registrationRepository.searchRegistrations(eventId, status, pageable);
    }
    @Transactional
    public Registration register(Long eventId, CreateRegistrationRequest request) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy sự kiện với ID: " + eventId));

        Participant participant = participantService.findById(request.participantId());

        if (event.getAvailableSeats() <= 0) {
            throw new IllegalStateException("Sự kiện đã hết chỗ trống.");
        }

        event.setAvailableSeats(event.getAvailableSeats() - 1);
        eventRepository.save(event);

        Registration registration = new Registration();
        registration.setEvent(event);
        registration.setParticipant(participant);
        registration.setStatus(RegistrationStatus.ACTIVE);
        registration.setRegisteredAt(LocalDateTime.now(clock));

        return registrationRepository.save(registration);
    }
}