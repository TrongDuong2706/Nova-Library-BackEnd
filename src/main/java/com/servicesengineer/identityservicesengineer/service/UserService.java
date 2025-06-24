package com.servicesengineer.identityservicesengineer.service;

import com.servicesengineer.identityservicesengineer.dto.request.UserRequest;
import com.servicesengineer.identityservicesengineer.dto.response.UserResponse;
import org.springframework.stereotype.Service;

import java.util.List;

public interface UserService {
    List<UserResponse> getUser();
    UserResponse addUser(UserRequest userRequest);
    UserResponse getMyInfor();
}
