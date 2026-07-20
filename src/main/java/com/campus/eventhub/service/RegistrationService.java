package com.campus.eventhub.service;

import com.campus.eventhub.repository.RegistrationRepository;
import org.springframework.stereotype.Service;
import java.time.Clock;

@Service
public class RegistrationService {
    private final RegistrationRepository registrationRepository;
    private final Clock clock;
    public RegistrationService(RegistrationRepository registrationRepository, Clock clock) {
        this.registrationRepository = registrationRepository;
        this.clock = clock;
    }

}