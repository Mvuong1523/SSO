package com.example.demoBe.dto;

import lombok.Data;

@Data
public class SsoTokenRequest {
    private String appId; // ID của app yêu cầu token
    private String redirectUrl; // URL để redirect sau khi có token
}
