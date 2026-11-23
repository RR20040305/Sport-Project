package ru.mirea.sport.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import ru.mirea.sport.domain.Role;
import ru.mirea.sport.domain.RoleName;
import ru.mirea.sport.domain.User;
import ru.mirea.sport.repository.RoleRepository;
import ru.mirea.sport.repository.UserRepository;

import java.util.HashSet;
import java.util.UUID;

@Component
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${app.bootstrap.email}")
    private String adminEmail;
    @Value("${app.bootstrap.name}")
    private String adminName;
    @Value("${app.bootstrap.password}")
    private String adminPassword;

    public DataInitializer(UserRepository userRepository, RoleRepository roleRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        if (userRepository.findByEmail(adminEmail).isEmpty()) {
            Role adminRole = roleRepository.findByName(RoleName.ROLE_ADMIN)
                    .orElseGet(() -> createRole(RoleName.ROLE_ADMIN));
            Role editorRole = roleRepository.findByName(RoleName.ROLE_EDITOR)
                    .orElseGet(() -> createRole(RoleName.ROLE_EDITOR));
            Role viewerRole = roleRepository.findByName(RoleName.ROLE_VIEWER)
                    .orElseGet(() -> createRole(RoleName.ROLE_VIEWER));

            User admin = new User();
            admin.setId(UUID.randomUUID());
            admin.setEmail(adminEmail);
            admin.setFullName(adminName);
            admin.setPasswordHash(passwordEncoder.encode(adminPassword));
            admin.setActive(true);
            admin.setPasswordChangeRequired(true);
            
            HashSet<Role> roles = new HashSet<>();
            roles.add(adminRole);
            admin.setRoles(roles);

            userRepository.save(admin);
            System.out.println("Bootstrap admin created: " + adminEmail);
        }
    }

    private Role createRole(RoleName name) {
        Role role = new Role();
        role.setId(UUID.randomUUID());
        role.setName(name);
        return roleRepository.save(role);
    }
}