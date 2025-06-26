package com.servicesengineer.identityservicesengineer.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EditUserRequest {
    private String password;
    private String firstName;
    private String lastName;
    private String email;
    private String phoneNumber;
    private String roleName;
}
