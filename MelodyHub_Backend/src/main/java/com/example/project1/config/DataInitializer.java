package com.example.project1.config;

import com.example.project1.entity.User;
import com.example.project1.enums.Role;
import com.example.project1.repository.UserRepository;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * Public registration can no longer create ADMIN accounts, so a brand-new
 * database needs one admin to start with. If (and only if) there is no ADMIN
 * user yet, one is created from the properties below. Change the password
 * after the first login, or disable this with
 * melodyhub.bootstrap-admin.enabled=false.
 */
@Component
public class DataInitializer implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DataInitializer.class);

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${melodyhub.bootstrap-admin.enabled:true}")
    private boolean enabled;

    @Value("${melodyhub.bootstrap-admin.name:MelodyHub Admin}")
    private String name;

    @Value("${melodyhub.bootstrap-admin.email:admin@melodyhub.com}")
    private String email;

    @Value("${melodyhub.bootstrap-admin.password:Admin@12345}")
    private String password;

    public DataInitializer(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {

        if (!enabled || userRepository.existsByRole(Role.ADMIN)) {
            return;
        }

        if (userRepository.existsByEmail(email)) {
            log.warn("Bootstrap admin skipped: {} already exists with a different role.", email);
            return;
        }

        User admin = new User();
        admin.setName(name);
        admin.setEmail(email);
        admin.setPassword(passwordEncoder.encode(password));
        admin.setRole(Role.ADMIN);
        userRepository.save(admin);

        log.warn("No ADMIN existed - created bootstrap admin '{}'. CHANGE THIS PASSWORD after first login.", email);
    }
}
