package com.campus.eventhub.service;

import com.campus.eventhub.domain.Participant;
import com.campus.eventhub.repository.ParticipantRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.Clock;
import java.time.LocalDateTime;

@Service
@Transactional(readOnly = true)
public class ParticipantService {

    private final ParticipantRepository participantRepository;
    private final Clock clock;

    public ParticipantService(ParticipantRepository participantRepository, Clock clock) {
        this.participantRepository = participantRepository;
        this.clock = clock;
    }
    public Page<Participant> search(String keyword, Pageable pageable) {
        return participantRepository.searchParticipants(keyword, pageable);
    }

    public Participant findById(Long id) {
        return participantRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy người tham gia với ID: " + id));
    }

    @Transactional
    public Participant create(String fullName, String email) {
        Participant participant = new Participant();
        participant.setFullName(fullName);
        participant.setEmail(email);
        participant.setCreatedAt(LocalDateTime.now(clock));
        return participantRepository.save(participant);
    }
}