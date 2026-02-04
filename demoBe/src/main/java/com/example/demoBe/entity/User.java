package com.example.demoBe.entity;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class User {
    private Long userUid;
    private String userId;
    private String pwd;
    private String pwdExpr;
    private String status;
    private String userType;
    private String authProvider;
    private LocalDateTime createdDate;
    private String createdBy;
    private LocalDateTime updatedDate;
    private String updatedBy;
    private String email;
}
