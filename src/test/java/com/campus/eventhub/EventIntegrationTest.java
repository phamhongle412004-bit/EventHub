package com.campus.eventhub;

import com.campus.eventhub.domain.Event;
import com.campus.eventhub.domain.EventStatus;
import com.campus.eventhub.domain.Participant;
import com.campus.eventhub.domain.Registration;
import com.campus.eventhub.repository.EventRepository;
import com.campus.eventhub.repository.ParticipantRepository;
import com.campus.eventhub.repository.RegistrationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@Transactional
class EventIntegrationTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ParticipantRepository participantRepository;
    @Autowired private EventRepository eventRepository;
    @Autowired private RegistrationRepository registrationRepository;

    private Participant savedParticipant;
    private Event savedEvent;

    @BeforeEach
    void setUpData() {
        registrationRepository.deleteAll();
        eventRepository.deleteAll();
        participantRepository.deleteAll();

        savedParticipant = participantRepository.save(
                new Participant("Le Pham", "student.test@campus.edu.vn"));

        Event event = new Event();
        event.setTitle("Hackathon 2026");
        event.setDescription("Cuộc thi lập trình");
        event.setLocation("Hall A");
        event.setCapacity(10);
        event.setAvailableSeats(10);
        event.setStatus(EventStatus.OPEN);
        event.setStartAt(LocalDateTime.now().plusDays(3));
        event.setCreatedAt(LocalDateTime.now());

        savedEvent = eventRepository.save(event);
    }

    @Test
    @WithMockUser(username = "admin@campus.edu.vn", roles = "EVENT_ADMIN")
    @DisplayName("End-to-End: Luồng Đăng ký hoàn chỉnh từ HTTP POST xuống DB thực tế")
    void fullRegistrationFlow_Success() throws Exception {
        String requestJson = String.format("{\"participantId\": %d}", savedParticipant.getId());

        mockMvc.perform(post("/api/v1/events/{eventId}/registrations", savedEvent.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isCreated());

        Event updatedEvent = eventRepository.findById(savedEvent.getId()).orElseThrow();
        assertThat(updatedEvent.getAvailableSeats()).isEqualTo(9);

        List<Registration> registrations = registrationRepository.findAll();
        assertThat(registrations).hasSize(1);
        assertThat(registrations.get(0).getParticipant().getId()).isEqualTo(savedParticipant.getId());
    }
}