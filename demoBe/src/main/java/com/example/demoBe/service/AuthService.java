package com.example.demoBe.service;

import com.example.demoBe.dto.AuthResponse;
import com.example.demoBe.dto.LoginRequest;
import com.example.demoBe.dto.RegisterRequest;
import com.example.demoBe.entity.User;

public interface AuthService {

    AuthResponse registerLocal(RegisterRequest req);

    AuthResponse loginLocal(LoginRequest req);

    AuthResponse loginGoogle(String idToken);

    AuthResponse generateAuthResponse(User user);
}
