package com.campus.eventhub.web;

import com.campus.eventhub.domain.Event;
import com.campus.eventhub.domain.EventStatus;
import com.campus.eventhub.security.JwtProvider;
import com.campus.eventhub.service.EventService;
import com.campus.eventhub.web.dto.CreateEventRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(EventController.class)
@AutoConfigureMockMvc(addFilters = false)
class EventControllerTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;

    @MockBean private EventService eventService;
    @MockBean private JwtProvider jwtProvider;

    @Test
    @DisplayName("POST /api/v1/events - Tạo mới thành công trả về HTTP 201, Header Location và EventDetailResponse")
    void createEvent_Success() throws Exception {
        LocalDateTime startAt = LocalDateTime.now().plusDays(5);
        CreateEventRequest request = new CreateEventRequest(
                "Workshop AI 2026",
                "Hội thảo AI",
                "Hội trường A",
                startAt,
                100
        );

        Event savedEvent = new Event();
        savedEvent.setId(10L);
        savedEvent.setTitle("Workshop AI 2026");
        savedEvent.setDescription("Hội thảo AI");
        savedEvent.setLocation("Hội trường A");
        savedEvent.setStartAt(startAt);
        savedEvent.setCapacity(100);
        savedEvent.setAvailableSeats(100);
        savedEvent.setStatus(EventStatus.DRAFT);
        savedEvent.setCreatedAt(LocalDateTime.now());

        // EventService có phương thức create(request)
        given(eventService.create(any(CreateEventRequest.class))).willReturn(savedEvent);

        mockMvc.perform(post("/api/v1/events")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "http://localhost/api/v1/events/10"))
                .andExpect(jsonPath("$.id", is(10)))
                .andExpect(jsonPath("$.title", is("Workshop AI 2026")))
                .andExpect(jsonPath("$.status", is("DRAFT")))
                .andExpect(jsonPath("$.availableSeats", is(100)));
    }

    @Test
    @DisplayName("POST /api/v1/events - Request không hợp lệ (Validation 400) & Chặn không gọi tầng Service")
    void createEvent_InvalidValidation() throws Exception {
        CreateEventRequest invalidRequest = new CreateEventRequest(
                "",
                "Mô tả",
                "",
                LocalDateTime.now().minusDays(1),
                0
        );

        mockMvc.perform(post("/api/v1/events")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());

        // Đảm bảo Validation chặn ngay từ Controller, không gọi eventService.create(...)
        verify(eventService, never()).create(any());
    }

    @Test
    @DisplayName("GET /api/v1/events/{id} - Lấy chi tiết sự kiện thành công trả về EventDetailResponse")
    void getEventById_Success() throws Exception {
        Event event = new Event();
        event.setId(1L);
        event.setTitle("Tech Talk 2026");
        event.setDescription("Chia sẻ công nghệ");
        event.setLocation("Phòng A101");
        event.setStartAt(LocalDateTime.now().plusDays(2));
        event.setCapacity(50);
        event.setAvailableSeats(50);
        event.setStatus(EventStatus.OPEN);
        event.setCreatedAt(LocalDateTime.now());

        // EventService có phương thức findById(id)
        given(eventService.findById(1L)).willReturn(event);

        mockMvc.perform(get("/api/v1/events/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.title", is("Tech Talk 2026")))
                .andExpect(jsonPath("$.location", is("Phòng A101")))
                .andExpect(jsonPath("$.status", is("OPEN")));
    }

    @Test
    @DisplayName("GET /api/v1/events/{id} - Không tìm thấy sự kiện trả về lỗi 400/404 từ Exception Handling")
    void getEventById_NotFound() throws Exception {
        given(eventService.findById(999L))
                .willThrow(new IllegalArgumentException("Không tìm thấy sự kiện với ID: 999"));

        mockMvc.perform(get("/api/v1/events/999"))
                .andExpect(status().is4xxClientError());
    }
}