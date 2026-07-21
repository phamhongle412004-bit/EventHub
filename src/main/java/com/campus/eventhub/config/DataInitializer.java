package com.campus.eventhub.config;

import com.campus.eventhub.domain.Role;
import com.campus.eventhub.domain.UserAccount;
import com.campus.eventhub.repository.UserAccountRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class DataInitializer implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DataInitializer.class);

    private final UserAccountRepository userAccountRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${app.admin.email:admin@campus.edu.vn}")
    private String adminEmail;

    @Value("${ADMIN_DEFAULT_PASSWORD:AdminSecret@123}")
    private String adminPassword;

    public DataInitializer(UserAccountRepository userAccountRepository, PasswordEncoder passwordEncoder) {
        this.userAccountRepository = userAccountRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        // Chạy idempotent: Chỉ khởi tạo nếu chưa tồn tại Admin
        if (!userAccountRepository.existsByEmail(adminEmail)) {
            UserAccount admin = new UserAccount(
                    adminEmail,
                    passwordEncoder.encode(adminPassword),
                    Role.EVENT_ADMIN
            );
            userAccountRepository.save(admin);
            log.info("Khởi tạo tài khoản Event Admin ban đầu thành công cho: {}", adminEmail);
        }
    }
}