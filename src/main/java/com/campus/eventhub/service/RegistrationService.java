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

        if (event.getStatus() != EventStatus.OPEN) {
            throw new IllegalStateException("Sự kiện hiện không ở trạng thái mở đăng ký (OPEN).");
        }

        LocalDateTime now = LocalDateTime.now(clock);
        if (event.getStartAt().isBefore(now)) {
            throw new IllegalStateException("Không thể đăng ký sự kiện đã hoặc đang diễn ra.");
        }

        if (event.getAvailableSeats() <= 0) {
            throw new IllegalStateException("Sự kiện đã hết chỗ trống.");
        }
        boolean hasActive = registrationRepository
                .existsByEventIdAndParticipantIdAndStatus(eventId, participant.getId(), RegistrationStatus.ACTIVE);
        if (hasActive) {
            throw new IllegalStateException("Người tham gia đã có lượt đăng ký ACTIVE cho sự kiện này.");
        }
        event.setAvailableSeats(event.getAvailableSeats() - 1);

        Registration registration = new Registration();
        registration.setEvent(event);
        registration.setParticipant(participant);
        registration.setStatus(RegistrationStatus.ACTIVE);
        registration.setRegisteredAt(now);

        return registrationRepository.save(registration);
    }
    @Transactional
    public Registration cancel(Long eventId, Long registrationId) {
        Registration registration = registrationRepository.findByIdAndEventId(registrationId, eventId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Không tìm thấy lượt đăng ký với ID " + registrationId + " thuộc sự kiện ID " + eventId));
        if (registration.getStatus() == RegistrationStatus.CANCELLED) {
            throw new IllegalStateException("Lượt đăng ký này đã được hủy trước đó, không thể hoàn trả thêm ghế.");
        }
        registration.setStatus(RegistrationStatus.CANCELLED);
        registration.setCancelledAt(LocalDateTime.now(clock));

        Event event = registration.getEvent();
        event.setAvailableSeats(event.getAvailableSeats() + 1);
        return registration;
    }
}