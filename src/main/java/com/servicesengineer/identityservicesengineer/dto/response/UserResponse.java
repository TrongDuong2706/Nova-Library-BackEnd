package com.servicesengineer.identityservicesengineer.dto.response;

import com.servicesengineer.identityservicesengineer.entity.UserStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.Set;
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserResponse {
    private String id;
    private String username;
    private String firstName;
    private String lastName;
    private String studentCode;
    private String email;
    private String phoneNumber;
    private UserStatus status;
    Set<RoleResponse> roles;


}
