package com.servicesengineer.identityservicesengineer.configuration;

import com.servicesengineer.identityservicesengineer.entity.Role;
import com.servicesengineer.identityservicesengineer.entity.User;
import com.servicesengineer.identityservicesengineer.entity.UserStatus;
import com.servicesengineer.identityservicesengineer.repository.RoleRepository;
import com.servicesengineer.identityservicesengineer.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Set;

@Configuration

public class DataInitializer {
    @Bean
    CommandLineRunner initData(RoleRepository roleRepository,
                               UserRepository userRepository,
                               PasswordEncoder passwordEncoder) {
        return args -> {
            // Tạo 2 role nếu chưa có
            Role userRole = roleRepository.findById("USER")
                    .orElseGet(() -> roleRepository.save(
                            Role.builder().name("USER").description("Người dùng").build()
                    ));

            Role adminRole = roleRepository.findById("ADMIN")
                    .orElseGet(() -> roleRepository.save(
                            Role.builder().name("ADMIN").description("Quản trị viên").build()
                    ));

            // Tạo user admin nếu chưa tồn tại
            if (userRepository.findByUsername("admin").isEmpty()) {
                User admin = User.builder()
                        .username("admin")
                        .password(passwordEncoder.encode("admin123")) // Password nên lưu hashed
                        .firstName("System")
                        .lastName("Admin")
                        .email("admin@gmail.com")
                        .phoneNumber("0123456789")
                        .status(UserStatus.ACTIVE)
                        .roles(Set.of(adminRole))
                        .build();

                userRepository.save(admin);
            }
        };
    }
}
