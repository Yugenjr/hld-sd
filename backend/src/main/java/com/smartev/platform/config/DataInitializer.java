package com.smartev.platform.config;

import com.smartev.platform.user.Role;
import com.smartev.platform.user.User;
import com.smartev.platform.user.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class DataInitializer implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DataInitializer.class);

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public DataInitializer(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        ensureUserExists("Alex Morgan", "demo@smartev.com", "Password123!", Role.ROLE_USER);
        ensureUserExists("System Administrator", "admin@smartev.com", "AdminPassword123!", Role.ROLE_ADMIN);
    }

    private void ensureUserExists(String name, String email, String rawPassword, Role role) {
        userRepository.findByEmail(email).ifPresentOrElse(
                user -> {
                    // Update password to ensure standard hash matches
                    user.setPasswordHash(passwordEncoder.encode(rawPassword));
                    userRepository.save(user);
                    log.info("Verified seed user: {}", email);
                },
                () -> {
                    User newUser = new User(name, email, passwordEncoder.encode(rawPassword), role);
                    userRepository.save(newUser);
                    log.info("Created seed user: {}", email);
                }
        );
    }
}
