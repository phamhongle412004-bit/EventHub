package com.campus.eventhub.web;

import com.campus.eventhub.domain.Participant;
import com.campus.eventhub.domain.Role;
import com.campus.eventhub.domain.UserAccount;
import com.campus.eventhub.exception.DuplicateResourceException;
import com.campus.eventhub.repository.UserAccountRepository;
import com.campus.eventhub.security.JwtProvider;
import com.campus.eventhub.web.dto.JwtResponse;
import com.campus.eventhub.web.dto.LoginRequest;
import com.campus.eventhub.web.dto.RegisterRequest;
import com.campus.eventhub.web.dto.UserResponseDto;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final UserAccountRepository userAccountRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtProvider jwtProvider;

    public AuthController(AuthenticationManager authenticationManager,
                          UserAccountRepository userAccountRepository,
                          PasswordEncoder passwordEncoder,
                          JwtProvider jwtProvider) {
        this.authenticationManager = authenticationManager;
        this.userAccountRepository = userAccountRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtProvider = jwtProvider;
    }

    // 1. API Đăng ký tài khoản Participant
    @PostMapping("/register")
    @Transactional
    public ResponseEntity<Void> register(@Valid @RequestBody RegisterRequest request) {
        if (userAccountRepository.existsByEmail(request.email())) {
            throw new DuplicateResourceException("Email đã được sử dụng: " + request.email());
        }

        // Ép buộc gán Role PARTICIPANT để đảm bảo an toàn bảo mật
        UserAccount account = new UserAccount(
                request.email(),
                passwordEncoder.encode(request.password()),
                Role.PARTICIPANT
        );

        Participant participant = new Participant();
        participant.setFullName(request.fullName());
        participant.setEmail(request.email());

        account.setParticipant(participant);
        userAccountRepository.save(account);

        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    // 2. API Đăng nhập và lấy Bearer JWT Token
    @PostMapping("/login")
    public ResponseEntity<JwtResponse> login(@Valid @RequestBody LoginRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.email(), request.password())
        );

        UserAccount account = userAccountRepository.findByEmail(request.email())
                .orElseThrow();

        String token = jwtProvider.generateToken(account.getEmail(), account.getRole().name(), account.getId());

        return ResponseEntity.ok(new JwtResponse(token, jwtProvider.getExpirationMs()));
    }

    // 3. API Lấy thông tin user hiện tại qua Token
    @GetMapping("/me")
    public ResponseEntity<UserResponseDto> getCurrentUser(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        UserResponseDto userDto = new UserResponseDto(
                authentication.getName(),
                authentication.getAuthorities().stream()
                        .map(GrantedAuthority::getAuthority)
                        .toList()
        );

        return ResponseEntity.ok(userDto);
    }
}