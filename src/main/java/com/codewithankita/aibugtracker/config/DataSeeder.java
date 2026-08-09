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

        if (!userRepository.existsByEmail("alice@bugtracker.com")) {
            User alice = User.builder()
                    .name("Alice Dev")
                    .email("alice@bugtracker.com")
                    .password(passwordEncoder.encode("dev123"))
                    .role(Role.DEVELOPER)
                    .build();
            userRepository.save(alice);
            log.info("Seeded developer: alice@bugtracker.com / dev123");
        } else {
            userRepository.findByEmail("alice@bugtracker.com").ifPresent(u -> {
                u.setName("Alice Dev");
                userRepository.save(u);
            });
        }

        if (!userRepository.existsByEmail("bob@bugtracker.com")) {
            User bob = User.builder()
                    .name("Bob Dev")
                    .email("bob@bugtracker.com")
                    .password(passwordEncoder.encode("dev123"))
                    .role(Role.DEVELOPER)
                    .build();
            userRepository.save(bob);
            log.info("Seeded developer: bob@bugtracker.com / dev123");
        } else {
            userRepository.findByEmail("bob@bugtracker.com").ifPresent(u -> {
                u.setName("Bob Dev");
                userRepository.save(u);
            });
        }
    }
}
