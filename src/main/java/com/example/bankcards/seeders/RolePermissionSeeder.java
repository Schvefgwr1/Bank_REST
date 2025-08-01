package com.example.bankcards.seeders;


import com.example.bankcards.entity.Permission;
import com.example.bankcards.entity.Role;
import com.example.bankcards.entity.User;
import com.example.bankcards.repository.PermissionRepository;
import com.example.bankcards.repository.RoleRepository;
import com.example.bankcards.repository.UserRepository;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class RolePermissionSeeder {

    private final PermissionRepository permissionRepository;
    private final RoleRepository roleRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @PostConstruct
    public void seedRolesAndPermissions() {
        List<String> allPermissions = List.of(
                "CARD_READ", "CARD_CREATE", "CARD_UPDATE", "CARD_DELETE", "CARD_READ_MY",
                "USER_CREATE", "USER_READ", "USER_UPDATE", "USER_DELETE",
                "REFRESH_SESSION",
                "TRANSFER_CREATE", "BLOCK_REQUEST_CREATE", "BLOCK_REQUEST_READ", "BLOCK_REQUEST_UPDATE"
        );

        List<Permission> savedPermissions = allPermissions.stream()
                .map(name -> permissionRepository.findByName(name)
                        .orElseGet(() -> permissionRepository.save(Permission.builder().name(name).build())))
                .toList();

        if (roleRepository.findByName("ADMIN").isEmpty()) {
            saveAdminRole(savedPermissions);
        }

        if (roleRepository.findByName("USER").isEmpty()) {
            List<Permission> userPermissions = savedPermissions.stream()
                    .filter(p -> List.of(
                            "CARD_READ_MY",
                            "REFRESH_SESSION",
                            "TRANSFER_CREATE",
                            "BLOCK_REQUEST_CREATE"
                    ).contains(p.getName()))
                    .toList();

            Role user = Role.builder()
                    .name("USER")
                    .permissions(userPermissions)
                    .build();
            roleRepository.save(user);
        }

        if (!userRepository.existsByLogin("admin")) {
            Role adminRole = roleRepository.findByName("ADMIN")
                            .orElseGet(() -> saveAdminRole(savedPermissions));
            userRepository.save(User.builder()
                    .login("admin")
                    .password(passwordEncoder.encode("test"))
                    .role(adminRole)
                    .build()
            );
        }
    }

    private Role saveAdminRole(List<Permission> savedPermissions) {
        Role admin = Role.builder()
                .name("ADMIN")
                .permissions(savedPermissions)
                .build();
        return roleRepository.save(admin);
    }
}

