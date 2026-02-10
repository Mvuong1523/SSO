package com.example.demoBe.service;

import com.example.demoBe.dto.AuthResponse;
import com.example.demoBe.dto.LoginRequest;
import com.example.demoBe.entity.User;
import com.example.demoBe.mapper.UserMapper;
import com.example.demoBe.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class AuthService {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private RedisService redisService;

    @Autowired
    private AuthenticationManager authenticationManager;

    // Local Login (returns User for session creation)
    public AuthResponse loginLocal(LoginRequest request) {
        try{
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            request.getUsername(),
                            request.getPassword()
                    )

            );
            UserDetails userDetails = (UserDetails) authentication.getPrincipal();
            User user = userMapper.findByUserId(userDetails.getUsername());

            String accessToken = jwtUtil.generateAccessToken(user);
            String refreshToken = jwtUtil.generateRefreshToken(user);

            redisService.saveRefreshToken(refreshToken, user.getUserUid());

            return buildAuthResponse(user, accessToken, refreshToken);


        } catch (BadCredentialsException e){
            throw new RuntimeException("Invalid username or password");
        } catch (Exception e){
            throw new RuntimeException("Authentication failed: " + e.getMessage());

        }
    }

    public AuthResponse refreshToken(String refreshToken) {
        if (!redisService.hasRefreshToken(refreshToken)) {
            System.out.println("token het han");
            throw new RuntimeException("Invalid or expired refresh token");

        }

        // Get User from Token
        Object data = redisService.getRefreshToken(refreshToken);
        Long userUid = Long.valueOf(data.toString());
        User user = userMapper.findByUserUid(userUid);

        // Generate new Access Token with RSA
        String newAccessToken = jwtUtil.generateAccessToken(user);
        System.out.println("tra token thanh cong");
        // Return same Refresh Token (Shared/Long-lived)
        return buildAuthResponse(user, newAccessToken, refreshToken);
    }

    public void logout(String refreshToken) {
        redisService.deleteRefreshToken(refreshToken);
    }

    public AuthResponse generateTokenForUser(Long userUid, String existingRefreshToken) {
        User user = userMapper.findByUserUid(userUid);
        String accessToken = jwtUtil.generateAccessToken(user);
        return buildAuthResponse(user, accessToken, existingRefreshToken);
    }

    private AuthResponse buildAuthResponse(User user, String accessToken, String refreshToken) {
        AuthResponse response = new AuthResponse();
        response.setAccessToken(accessToken);
        response.setRefreshToken(refreshToken);
        response.setUserId(user.getUserId());
        response.setUserUid(user.getUserUid());
        response.setUserType(user.getUserType());
        response.setAuthProvider(user.getAuthProvider());
        return response;
    }

    public void register(LoginRequest request) {
        // Check if user already exists
        User existingUser = userMapper.findByUserId(request.getUsername());
        if (existingUser != null) {
            throw new RuntimeException("Email đã được sử dụng");
        }

        // Create new user
        User newUser = new User();
        newUser.setUserId(request.getUsername());
        newUser.setEmail(request.getUsername());
        newUser.setPwd(passwordEncoder.encode(request.getPassword()));
        newUser.setUserType("USER");
        newUser.setStatus("ACTIVE");
        newUser.setAuthProvider("LOCAL");

//        userMapper.insertUser(newUser);
    }
}
