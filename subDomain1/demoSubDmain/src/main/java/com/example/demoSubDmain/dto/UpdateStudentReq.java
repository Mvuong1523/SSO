package com.example.demoSubDmain.dto;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Email;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateStudentReq {
    @Email(message = "Email invalid")
    private String email;

    @DecimalMin(value = "0.0", inclusive = true, message = "Score must be >= 0")
    @DecimalMax(value = "10.0", inclusive = true, message = "Score must be <= 10")
    private Double score;
}
