package com.example.demoBe.dto;

import lombok.Data;

@Data
public class LoginRequest {
    private String email;
    private String pwd;
}
