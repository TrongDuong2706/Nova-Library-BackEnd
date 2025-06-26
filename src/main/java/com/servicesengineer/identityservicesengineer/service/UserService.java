package com.servicesengineer.identityservicesengineer.service;

import com.servicesengineer.identityservicesengineer.dto.request.EditUserRequest;
import com.servicesengineer.identityservicesengineer.dto.request.UserRequest;
import com.servicesengineer.identityservicesengineer.dto.response.PaginatedResponse;
import com.servicesengineer.identityservicesengineer.dto.response.UserResponse;
import org.springframework.stereotype.Service;

import java.util.List;

public interface UserService {
    List<UserResponse> getUser();
    UserResponse addUser(UserRequest userRequest);
    UserResponse getMyInfor();
    PaginatedResponse<UserResponse> getAllUserWithFilter(String name, String studentCode, String phoneNumber, int page, int size);
    UserResponse updateUser (String userId, EditUserRequest userRequest);
    UserResponse getOneUser(String userId);
    void softDeleteUser(String userId);
}
