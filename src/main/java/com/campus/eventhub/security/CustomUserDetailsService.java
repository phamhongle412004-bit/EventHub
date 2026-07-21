package com.campus.eventhub.security;

import com.campus.eventhub.domain.UserAccount;
import com.campus.eventhub.repository.UserAccountRepository;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.*;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final UserAccountRepository userAccountRepository;

    public CustomUserDetailsService(UserAccountRepository userAccountRepository) {
        this.userAccountRepository = userAccountRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        UserAccount account = userAccountRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("Không tìm thấy người dùng với email: " + email));

        return new CustomUserDetails(
                account.getId(),
                account.getEmail(),
                account.getPasswordHash(),
                account.isEnabled(),
                List.of(new SimpleGrantedAuthority("ROLE_" + account.getRole().name())),
                account.getParticipant() != null ? account.getParticipant().getId() : null
        );
    }
}