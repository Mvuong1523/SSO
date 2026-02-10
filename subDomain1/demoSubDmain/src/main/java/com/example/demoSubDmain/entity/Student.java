package com.example.demoSubDmain.entity;

import lombok.Data;
import java.time.LocalDate;

public class Student {
    private Long id;
    private String studentCode;
    private String name;
    private LocalDate birthDate;
    private String email;
    private String gender;
    private Double score;

    public Student() {
    }

    public Student(Long id, String studentCode, String name, LocalDate birthDate, String email, String gender,
            Double score) {
        this.id = id;
        this.studentCode = studentCode;
        this.name = name;
        this.birthDate = birthDate;
        this.email = email;
        this.gender = gender;
        this.score = score;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getStudentCode() {
        return studentCode;
    }

    public void setStudentCode(String studentCode) {
        this.studentCode = studentCode;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public LocalDate getBirthDate() {
        return birthDate;
    }

    public void setBirthDate(LocalDate birthDate) {
        this.birthDate = birthDate;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public Double getScore() {
        return score;
    }

    public void setScore(Double score) {
        this.score = score;
    }
}
