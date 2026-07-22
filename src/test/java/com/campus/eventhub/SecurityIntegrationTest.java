package com.campus.eventhub;

import com.campus.eventhub.web.dto.LoginRequest;
import com.campus.eventhub.web.dto.RegisterRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
public class SecurityIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @DisplayName("1. Đăng ký Participant mã hóa mật khẩu băm & không gán được ADMIN")
    void registerParticipantSuccess() throws Exception {
        RegisterRequest req = new RegisterRequest("Nguyen Van A", "testuser@gmail.com", "password123");

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated());
    }

    @Test
    @DisplayName("2. Đăng nhập sai mật khẩu trả về 401 Unauthorized")
    void loginFailedBadCredentials() throws Exception {
        LoginRequest req = new LoginRequest("admin@campus.edu.vn", "WrongPassword!");

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.errorCode").value("UNAUTHORIZED"));
    }

    @Test
    @DisplayName("3. Endpoint công khai Get Events truy cập thành công không cần Token")
    void getPublicEventsSuccess() throws Exception {
        mockMvc.perform(get("/api/v1/events"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("4. Endpoint bảo vệ POST /events thiếu Token trả về 401")
    void createEventWithoutTokenFails() throws Exception {
        mockMvc.perform(post("/api/v1/events")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("5. Token không hợp lệ / bị sửa đổi (tampered) trả về 401")
    void tamperedTokenFails() throws Exception {
        mockMvc.perform(post("/api/v1/events")
                        .header("Authorization", "Bearer invalid.tampered.token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isUnauthorized());
    }
}