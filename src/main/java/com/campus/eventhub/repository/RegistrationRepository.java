package com.campus.eventhub.repository;

import com.campus.eventhub.domain.Registration;
import com.campus.eventhub.domain.RegistrationStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface RegistrationRepository extends JpaRepository<Registration, Long> {
    boolean existsByEventIdAndParticipantIdAndStatus(Long eventId, Long participantId, RegistrationStatus status);
    Page<Registration> findByEventId(Long eventId, Pageable pageable);
    Optional<Registration> findByIdAndEventId(Long id, Long eventId);

    @Query("SELECT r FROM Registration r WHERE " +
            "(:eventId IS NULL OR r.event.id = :eventId) AND " +
            "(:status IS NULL OR r.status = :status)")
    Page<Registration> searchRegistrations(
            @Param("eventId") Long eventId,
            @Param("status") RegistrationStatus status,
            Pageable pageable
    );
}