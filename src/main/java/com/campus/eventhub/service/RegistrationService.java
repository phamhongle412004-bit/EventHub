package com.campus.eventhub.service;

import com.campus.eventhub.domain.*;
import com.campus.eventhub.exception.BusinessException;
import com.campus.eventhub.exception.ResourceNotFoundException;
import com.campus.eventhub.repository.EventRepository;
import com.campus.eventhub.repository.RegistrationRepository;
import com.campus.eventhub.security.CustomUserDetails;
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
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy sự kiện với ID: " + eventId));

        Participant participant = participantService.findById(request.participantId());

        if (event.getStatus() != EventStatus.OPEN) {
            throw new BusinessException("Sự kiện hiện không ở trạng thái mở đăng ký (OPEN).");
        }

        LocalDateTime now = LocalDateTime.now(clock);
        if (event.getStartAt().isBefore(now)) {
            throw new BusinessException("Không thể đăng ký sự kiện đã hoặc đang diễn ra.");
        }

        if (event.getAvailableSeats() <= 0) {
            throw new BusinessException("Sự kiện đã hết chỗ trống.");
        }

        boolean hasActive = registrationRepository
                .existsByEventIdAndParticipantIdAndStatus(eventId, participant.getId(), RegistrationStatus.ACTIVE);
        if (hasActive) {
            throw new BusinessException("Người tham gia đã có lượt đăng ký ACTIVE cho sự kiện này.");
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
    public Registration cancelRegistration(Long registrationId, CustomUserDetails currentUser) {
        Registration registration = registrationRepository.findById(registrationId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy đơn đăng ký với ID: " + registrationId));

        // 1. Kiểm tra quyền sở hữu (Ownership Check)
        boolean isAdmin = currentUser.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_EVENT_ADMIN"));

        if (!isAdmin && !registration.getParticipant().getId().equals(currentUser.getParticipantId())) {
            throw new BusinessException("Bạn không có quyền hủy đăng ký của người tham gia khác.");
        }

        // 2. Kiểm tra trạng thái đơn
        if (registration.getStatus() == RegistrationStatus.CANCELLED) {
            throw new BusinessException("Lượt đăng ký này đã được hủy trước đó.");
        }

        // 3. Thực hiện hủy và cộng lại số chỗ trống
        registration.setStatus(RegistrationStatus.CANCELLED);
        registration.setCancelledAt(LocalDateTime.now(clock));

        Event event = registration.getEvent();
        event.setAvailableSeats(event.getAvailableSeats() + 1);

        return registration;
    }
}