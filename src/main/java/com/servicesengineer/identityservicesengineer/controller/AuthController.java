package com.servicesengineer.identityservicesengineer.controller;

import com.servicesengineer.identityservicesengineer.dto.ApiResponse;
import com.servicesengineer.identityservicesengineer.dto.request.LoginRequest;
import com.servicesengineer.identityservicesengineer.dto.request.LogoutRequest;
import com.servicesengineer.identityservicesengineer.dto.response.LoginResponse;
import com.servicesengineer.identityservicesengineer.service.AuthService;
import org.apache.coyote.BadRequestException;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
public class AuthController {
    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }
    @PostMapping()
    ApiResponse<LoginResponse> login(@RequestBody LoginRequest loginRequest) {
        ApiResponse apiResponse = ApiResponse.<LoginResponse>builder()
                .code(200)
                .message("Login successful")
                .result(authService.login(loginRequest))
                .build();
        return apiResponse;
    }
    @PostMapping("/logout")
    ApiResponse<Void> logout (@RequestBody LogoutRequest logoutRequest){
        authService.logout(logoutRequest);
       return ApiResponse.<Void>builder()
                .message("Logout successful")
                .result(null)
                .build();
    }

}
