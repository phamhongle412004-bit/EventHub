package com.campus.eventhub.repository;

import com.campus.eventhub.domain.Event;
import com.campus.eventhub.domain.EventStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;

@Repository
public interface EventRepository extends JpaRepository<Event, Long> {

    @Query("SELECT e FROM Event e WHERE " +
            "(:keyword IS NULL OR LOWER(e.title) LIKE LOWER(CONCAT('%', :keyword, '%'))) AND " +
            "(:status IS NULL OR e.status = :status) AND " +
            "(:startFrom IS NULL OR e.startAt >= :startFrom)")
    Page<Event> searchEvents(
            @Param("keyword") String keyword,
            @Param("status") EventStatus status,
            @Param("startFrom") LocalDateTime startFrom,
            Pageable pageable
    );
}