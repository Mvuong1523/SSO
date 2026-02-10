package com.example.demoSubDmain.service;

import com.example.demoSubDmain.dto.CreateStudentReq;
import com.example.demoSubDmain.dto.SearchStudentReq;
import com.example.demoSubDmain.dto.UpdateStudentReq;
import com.example.demoSubDmain.entity.Student;

import java.util.List;

import com.example.demoSubDmain.dto.PageResponse;

public interface StudentService {
    PageResponse<Student> search(SearchStudentReq req);

    void create(CreateStudentReq req);

    void update(String code, UpdateStudentReq req);

    void deleteByCode(String code);
}
