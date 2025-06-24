package com.servicesengineer.identityservicesengineer.service;

import com.servicesengineer.identityservicesengineer.configuration.JwtTokenProvider;
import com.servicesengineer.identityservicesengineer.dto.request.LoginRequest;
import com.servicesengineer.identityservicesengineer.dto.request.LogoutRequest;
import com.servicesengineer.identityservicesengineer.dto.response.LoginResponse;
import com.servicesengineer.identityservicesengineer.entity.InvalidatedToken;
import com.servicesengineer.identityservicesengineer.exception.AppException;
import com.servicesengineer.identityservicesengineer.exception.ErrorCode;
import com.servicesengineer.identityservicesengineer.repository.InvalidatedRepository;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.util.Date;

@Service
public class AuthService {

    private final AuthenticationManager authenticationManager; // Đây có phải là AuthenticationManager từ SecurityConfig?
    private final JwtTokenProvider jwtTokenProvider;
    private final InvalidatedRepository invalidatedRepository;
    @Value("${jwt.secret}")
    private String jwtSecret;

    public AuthService(AuthenticationManager authenticationManager, JwtTokenProvider jwtTokenProvider, InvalidatedRepository invalidatedRepository) {
        this.authenticationManager = authenticationManager;
        this.jwtTokenProvider = jwtTokenProvider;
        this.invalidatedRepository = invalidatedRepository;
    }

    public LoginResponse login(LoginRequest request) {

            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword())
            );
            String token = jwtTokenProvider.generateToken((UserDetails) authentication.getPrincipal());
            return new LoginResponse(token, true);

    }
    public void logout(LogoutRequest request) {
        String token = request.getToken();

        // Kiểm tra token hợp lệ
        if (!jwtTokenProvider.validateToken(token)) {
            throw new AppException(ErrorCode.INVALID_TOKEN);
        }

        try {
            // Parse claims để lấy thông tin từ token
            Claims claims = Jwts.parser()
                    .setSigningKey(jwtSecret)
                    .parseClaimsJws(token)
                    .getBody();

            // Lấy JWT ID (jit) và thời gian hết hạn
            String tokenId = claims.getId(); // Lấy JWT ID (jit)
            Date expiry = claims.getExpiration(); // Lấy thời gian hết hạn

            // Kiểm tra nếu token đã hết hạn
            if (expiry.before(new Date())) {
                throw new AppException(ErrorCode.TOKEN_EXPIRED);
            }

            // Lưu JWT ID và thời gian hết hạn vào bảng InvalidatedToken
            InvalidatedToken invalidatedToken = InvalidatedToken.builder()
                    .id(tokenId)  // Lưu JWT ID (jit)
                    .expiryTime(expiry)
                    .build();

            invalidatedRepository.save(invalidatedToken);  // Lưu vào repository
        } catch (JwtException e) {
            // Nếu token không hợp lệ hoặc parse lỗi
            throw new AppException(ErrorCode.INVALID_TOKEN);
        }
    }


}



