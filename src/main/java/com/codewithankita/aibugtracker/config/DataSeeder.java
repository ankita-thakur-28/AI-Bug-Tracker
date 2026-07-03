package com.codewithankita.aibugtracker.config;

import com.codewithankita.aibugtracker.Model.Role;
import com.codewithankita.aibugtracker.Model.User;
import com.codewithankita.aibugtracker.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class DataSeeder implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        if (!userRepository.existsByEmail("admin@bugtracker.com")) {
            User admin = User.builder()
                    .name("Admin User")
                    .email("admin@bugtracker.com")
                    .password(passwordEncoder.encode("admin123"))
                    .role(Role.ADMIN)
                    .phone("+1-555-0100")
                    .build();
            userRepository.save(admin);
            log.info("Seeded admin user: admin@bugtracker.com / admin123");
        }

        if (!userRepository.existsByEmail("dev@bugtracker.com")) {
            User dev = User.builder()
                    .name("Dev User")
                    .email("dev@bugtracker.com")
                    .password(passwordEncoder.encode("dev123"))
                    .role(Role.DEVELOPER)
                    .build();
            userRepository.save(dev);
            log.info("Seeded developer: dev@bugtracker.com / dev123");
        }
    }
}
