package com.campus.eventhub.service;

import com.campus.eventhub.domain.*;
import com.campus.eventhub.exception.BusinessException;
import com.campus.eventhub.exception.ResourceNotFoundException;
import com.campus.eventhub.repository.EventRepository;
import com.campus.eventhub.repository.RegistrationRepository;
import com.campus.eventhub.security.CustomUserDetails;
import com.campus.eventhub.web.dto.CreateRegistrationRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RegistrationServiceTest {

    @Mock private RegistrationRepository registrationRepository;
    @Mock private EventRepository eventRepository;
    @Mock private ParticipantService participantService;

    private Clock fixedClock;
    private RegistrationService registrationService;

    private Participant testParticipant;
    private Event testEvent;

    @BeforeEach
    void setUp() {
        // Cố định thời gian test: 2026-07-22T10:00:00Z
        Instant fixedInstant = Instant.parse("2026-07-22T10:00:00Z");
        fixedClock = Clock.fixed(fixedInstant, ZoneId.of("UTC"));

        registrationService = new RegistrationService(
                registrationRepository, eventRepository, participantService, fixedClock);

        testParticipant = new Participant();
        testParticipant.setId(1L);
        testParticipant.setEmail("student@campus.edu.vn");

        testEvent = new Event();
        testEvent.setId(100L);
        testEvent.setTitle("Tech Workshop 2026");
        testEvent.setStatus(EventStatus.OPEN);
        testEvent.setCapacity(50);
        testEvent.setAvailableSeats(50);
        // Đặt thời gian bắt đầu sau thời điểm test 2 ngày
        testEvent.setStartAt(LocalDateTime.now(fixedClock).plusDays(2));
    }

    @Test
    @DisplayName("Đăng ký thành công: Giảm ghế trống và lưu bản ghi ACTIVE")
    void register_Success() {
        CreateRegistrationRequest request = new CreateRegistrationRequest(1L);

        given(eventRepository.findById(100L)).willReturn(Optional.of(testEvent));
        given(participantService.findById(1L)).willReturn(testParticipant);
        given(registrationRepository.existsByEventIdAndParticipantIdAndStatus(100L, 1L, RegistrationStatus.ACTIVE))
                .willReturn(false);

        registrationService.register(100L, request);

        // Verify ghế trống bị giảm đúng 1
        assertThat(testEvent.getAvailableSeats()).isEqualTo(49);

        // Capture và verify dữ liệu được lưu vào DB
        ArgumentCaptor<Registration> regCaptor = ArgumentCaptor.forClass(Registration.class);
        verify(registrationRepository).save(regCaptor.capture());

        Registration savedReg = regCaptor.getValue();
        assertThat(savedReg.getParticipant()).isEqualTo(testParticipant);
        assertThat(savedReg.getEvent()).isEqualTo(testEvent);
        assertThat(savedReg.getStatus()).isEqualTo(RegistrationStatus.ACTIVE);
        assertThat(savedReg.getRegisteredAt()).isEqualTo(LocalDateTime.now(fixedClock));
    }

    @Test
    @DisplayName("Thất bại: Sự kiện không ở trạng thái OPEN")
    void register_StatusNotOpen() {
        testEvent.setStatus(EventStatus.CLOSED); // Hoặc trạng thái khác khác OPEN
        CreateRegistrationRequest request = new CreateRegistrationRequest(1L);

        given(eventRepository.findById(100L)).willReturn(Optional.of(testEvent));

        assertThatThrownBy(() -> registrationService.register(100L, request))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("không ở trạng thái mở đăng ký");

        verify(registrationRepository, never()).save(any());
    }

    @Test
    @DisplayName("Thất bại: Sự kiện đã/đang diễn ra")
    void register_EventAlreadyStarted() {
        testEvent.setStartAt(LocalDateTime.now(fixedClock).minusHours(1));
        CreateRegistrationRequest request = new CreateRegistrationRequest(1L);

        given(eventRepository.findById(100L)).willReturn(Optional.of(testEvent));
        given(participantService.findById(1L)).willReturn(testParticipant);

        assertThatThrownBy(() -> registrationService.register(100L, request))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("Không thể đăng ký sự kiện đã hoặc đang diễn ra");

        verify(registrationRepository, never()).save(any());
    }

    @Test
    @DisplayName("Thất bại: Sự kiện đã hết chỗ trống")
    void register_EventFull() {
        testEvent.setAvailableSeats(0);
        CreateRegistrationRequest request = new CreateRegistrationRequest(1L);

        given(eventRepository.findById(100L)).willReturn(Optional.of(testEvent));
        given(participantService.findById(1L)).willReturn(testParticipant);

        assertThatThrownBy(() -> registrationService.register(100L, request))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("Sự kiện đã hết chỗ trống");

        verify(registrationRepository, never()).save(any());
    }

    @Test
    @DisplayName("Thất bại: Người tham gia đã có lượt đăng ký ACTIVE trước đó")
    void register_AlreadyActive() {
        CreateRegistrationRequest request = new CreateRegistrationRequest(1L);

        given(eventRepository.findById(100L)).willReturn(Optional.of(testEvent));
        given(participantService.findById(1L)).willReturn(testParticipant);
        given(registrationRepository.existsByEventIdAndParticipantIdAndStatus(100L, 1L, RegistrationStatus.ACTIVE))
                .willReturn(true);

        assertThatThrownBy(() -> registrationService.register(100L, request))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("đã có lượt đăng ký ACTIVE");

        assertThat(testEvent.getAvailableSeats()).isEqualTo(50);
        verify(registrationRepository, never()).save(any());
    }

    @Test
    @DisplayName("Hủy đăng ký thành công: Chính chủ thực hiện hủy, cộng lại ghế trống")
    void cancelRegistration_Success_Owner() {
        testEvent.setAvailableSeats(45);
        Registration registration = new Registration();
        registration.setId(10L);
        registration.setEvent(testEvent);
        registration.setParticipant(testParticipant);
        registration.setStatus(RegistrationStatus.ACTIVE);

        CustomUserDetails mockUser = mock(CustomUserDetails.class);
        given(mockUser.getAuthorities()).willReturn((List) List.of(new SimpleGrantedAuthority("ROLE_USER")));
        given(mockUser.getParticipantId()).willReturn(1L); // Cùng ID với testParticipant

        given(registrationRepository.findById(10L)).willReturn(Optional.of(registration));

        Registration cancelledReg = registrationService.cancelRegistration(10L, mockUser);

        assertThat(cancelledReg.getStatus()).isEqualTo(RegistrationStatus.CANCELLED);
        assertThat(cancelledReg.getCancelledAt()).isEqualTo(LocalDateTime.now(fixedClock));
        assertThat(testEvent.getAvailableSeats()).isEqualTo(46); // Ghế được cộng lại 1
    }

    @Test
    @DisplayName("Thất bại: Không có quyền hủy lượt đăng ký của người khác")
    void cancelRegistration_Forbidden() {
        Registration registration = new Registration();
        registration.setId(10L);
        registration.setParticipant(testParticipant); // Participant ID là 1L

        CustomUserDetails mockUser = mock(CustomUserDetails.class);
        given(mockUser.getAuthorities()).willReturn((List) List.of(new SimpleGrantedAuthority("ROLE_USER")));
        given(mockUser.getParticipantId()).willReturn(99L); // Khác ID

        given(registrationRepository.findById(10L)).willReturn(Optional.of(registration));

        assertThatThrownBy(() -> registrationService.cancelRegistration(10L, mockUser))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("Bạn không có quyền hủy đăng ký");
    }
}