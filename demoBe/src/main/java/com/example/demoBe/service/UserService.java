package com.example.demoBe.service;

import org.springframework.security.core.userdetails.UserDetailsService;

import com.example.demoBe.entity.User;

public interface UserService extends UserDetailsService {
    User findByUserId(String userId);
}
