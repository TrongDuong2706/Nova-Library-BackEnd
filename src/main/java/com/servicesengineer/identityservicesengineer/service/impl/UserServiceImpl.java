package com.servicesengineer.identityservicesengineer.service.impl;

import com.servicesengineer.identityservicesengineer.constant.PredefinedRole;
import com.servicesengineer.identityservicesengineer.dto.request.UserRequest;
import com.servicesengineer.identityservicesengineer.dto.response.PaginatedResponse;
import com.servicesengineer.identityservicesengineer.dto.response.PermissionResponse;
import com.servicesengineer.identityservicesengineer.dto.response.RoleResponse;
import com.servicesengineer.identityservicesengineer.dto.response.UserResponse;
import com.servicesengineer.identityservicesengineer.entity.Role;
import com.servicesengineer.identityservicesengineer.entity.User;
import com.servicesengineer.identityservicesengineer.entity.UserStatus;
import com.servicesengineer.identityservicesengineer.exception.AppException;
import com.servicesengineer.identityservicesengineer.exception.ErrorCode;
import com.servicesengineer.identityservicesengineer.repository.RoleRepository;
import com.servicesengineer.identityservicesengineer.repository.UserRepository;
import com.servicesengineer.identityservicesengineer.service.UserService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;
@Service
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;

    private final RoleRepository roleRepository;

    private final PasswordEncoder passwordEncoder;

    public UserServiceImpl(UserRepository userRepository, RoleRepository roleRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
    }
    @Override
    @PreAuthorize("hasRole('ADMIN')")
    public List<UserResponse> getUser() {
        List<User> users = userRepository.findAll();
        return users.stream().map(user -> {
            Set<RoleResponse> roleResponses = user.getRoles().stream().map(role -> {
                Set<PermissionResponse> permissionResponses = role.getPermissions().stream()
                        .map(permission -> new PermissionResponse(
                                permission.getName(),
                                permission.getDescription()))
                        .collect(Collectors.toSet());

                return new RoleResponse(
                        role.getName(),
                        role.getDescription(),
                        permissionResponses
                );
            }).collect(Collectors.toSet());

            return new UserResponse(
                    user.getId(),
                    user.getUsername(),
                    user.getFirstName(),
                    user.getLastName(),
                    user.getStudentCode(),
                    user.getEmail(),
                    user.getPhoneNumber(),
                    user.getStatus(),
                    roleResponses
            );
        }).collect(Collectors.toList());
    }
    @Override
    public UserResponse addUser(UserRequest userRequest) {
        if (userRepository.findByUsername(userRequest.getUsername()).isPresent()) {
            throw new AppException(ErrorCode.USERNAME_HAS_EXISTED);
        }
        if (!userRequest.getEmail().matches("^[a-zA-Z0-9._%+-]+@gmail\\.com$")) {
            throw new AppException(ErrorCode.INVALID_EMAIL_FORMAT);
        }

        User user = new User();
        user.setUsername(userRequest.getUsername());
        user.setPassword(passwordEncoder.encode(userRequest.getPassword()));
        user.setFirstName(userRequest.getFirstName());
        user.setLastName(userRequest.getLastName());
        user.setStudentCode(generateStudentCode());
        user.setEmail(userRequest.getEmail());
        user.setPhoneNumber(userRequest.getPhoneNumber());
        user.setStatus(UserStatus.ACTIVE);

        // Lấy Role USER mặc định từ DB
        HashSet<Role> roles = new HashSet<>();
        roleRepository.findById(PredefinedRole.USER_ROLE).ifPresent(roles::add);

        user.setRoles(roles);
        User savedUser = userRepository.save(user);

        // Chuyển đổi Set<Role> thành Set<RoleResponse>
        Set<RoleResponse> roleResponses = savedUser.getRoles().stream()
                .map(role -> new RoleResponse(
                        role.getName(),
                        role.getDescription(),
                        Collections.emptySet() // permissions rỗng
                ))
                .collect(Collectors.toSet());

        return new UserResponse(
                savedUser.getId(),
                savedUser.getUsername(),
                savedUser.getFirstName(),
                savedUser.getLastName(),
                savedUser.getStudentCode(),
                savedUser.getEmail(),
                savedUser.getPhoneNumber(),
                savedUser.getStatus(),
                roleResponses
        );
    }
    private String generateStudentCode() {
        return "STU-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }


    @Override
    public UserResponse getMyInfor() {
        var context = SecurityContextHolder.getContext();
        String username = context.getAuthentication().getName();

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));

        Set<RoleResponse> roleResponses = user.getRoles().stream().map(role -> {
            Set<PermissionResponse> permissionResponses = role.getPermissions().stream()
                    .map(permission -> new PermissionResponse(
                            permission.getName(),
                            permission.getDescription()))
                    .collect(Collectors.toSet());

            return new RoleResponse(
                    role.getName(),
                    role.getDescription(),
                    permissionResponses
            );
        }).collect(Collectors.toSet());

        return new UserResponse(
                user.getId(),
                user.getUsername(),
                user.getFirstName(),
                user.getLastName(),
                user.getStudentCode(),
                user.getEmail(),
                user.getPhoneNumber(),
                user.getStatus(),
                roleResponses
        );
    }
    @Override
    public PaginatedResponse<UserResponse> getAllUserWithFilter(String name, String studentCode, String phoneNumber, int page, int size) {
        PageRequest pageRequest = PageRequest.of(page, size);
        Page<User> users = userRepository.findByNameAndStudentCodeAndPhoneNumber(name, studentCode, phoneNumber, pageRequest);

        var userResponses = users.getContent().stream().map(user -> {
            var roleResponses = user.getRoles().stream().map(role -> {
                var permissionResponses = role.getPermissions().stream()
                        .map(permission -> new PermissionResponse(
                                permission.getName(),
                                permission.getDescription()))
                        .collect(Collectors.toSet());
                return new RoleResponse(
                        role.getName(),
                        role.getDescription(),
                        permissionResponses
                );
            }).collect(Collectors.toSet());

            return new UserResponse(
                    user.getId(),
                    user.getUsername(),
                    user.getFirstName(),
                    user.getLastName(),
                    user.getStudentCode(),
                    user.getEmail(),
                    user.getPhoneNumber(),
                    user.getStatus(),
                    roleResponses
            );
        }).toList();

        return PaginatedResponse.<UserResponse>builder()
                .totalItems((int) users.getTotalElements())
                .currentPage(users.getNumber())
                .hasNextPage(users.hasNext())
                .hasPreviousPage(users.hasPrevious())
                .totalPages(users.getTotalPages())
                .pageSize(users.getSize())
                .elements(userResponses)
                .build();
    }

}
