package com.servicesengineer.identityservicesengineer.controller;

import com.servicesengineer.identityservicesengineer.dto.ApiResponse;
import com.servicesengineer.identityservicesengineer.dto.request.UserRequest;
import com.servicesengineer.identityservicesengineer.dto.response.UserResponse;
import com.servicesengineer.identityservicesengineer.service.UserService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController()
@RequestMapping("/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping()
    ApiResponse<List<UserResponse>> getUser() {
        ApiResponse apiResponse = ApiResponse.<List<UserResponse>>builder()
                .code(200)
                .message("Get User Success")
                .result(userService.getUser())
                .build();
        return apiResponse;
    }
    @PostMapping()
    ApiResponse<UserResponse> createUser(@RequestBody UserRequest userRequest) {
        System.out.println("🔥🔥🔥 CreateUser API called");
        ApiResponse apiResponse = ApiResponse.<UserResponse>builder()
                .code(200)
                .message("Create User Success")
                .result(userService.addUser(userRequest))
                .build();
        return apiResponse;
    }
    @GetMapping("/getMyInfor")
    ApiResponse<UserResponse> getMyInfor() {
        ApiResponse apiResponse = ApiResponse.<UserResponse>builder()
                .code(200)
                .message("Get User Success")
                .result(userService.getMyInfor())
                .build();
        return apiResponse;
    }
}
