package com.example.demoSubDmain.controller;

import com.example.demoSubDmain.common.ApiResponse;
import com.example.demoSubDmain.dto.CreateStudentReq;
import com.example.demoSubDmain.dto.PageResponse;
import com.example.demoSubDmain.dto.SearchStudentReq;
import com.example.demoSubDmain.dto.UpdateStudentReq;
import com.example.demoSubDmain.entity.Student;
import com.example.demoSubDmain.service.StudentService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/students")
public class StudentController {
    @Autowired
    private StudentService studentService;

    @PostMapping("/search")
    public ApiResponse<PageResponse<Student>> search(@RequestBody SearchStudentReq req) {
        return new ApiResponse<>(1000, null, studentService.search(req));
    }

    @PostMapping
    public ApiResponse<String> create(@RequestBody @Valid CreateStudentReq req) {
        studentService.create(req);
        return new ApiResponse<>(1000, "Create success", null);
    }

    @PutMapping("/{studentCode}")
    public ApiResponse<String> update(@PathVariable String studentCode, @RequestBody UpdateStudentReq req) {
        studentService.update(studentCode, req);
        return new ApiResponse<>(1000, "Update success", null);
    }

    @DeleteMapping("/{code}")
    public ApiResponse<String> deleteByCode(@PathVariable String code) {
        studentService.deleteByCode(code);
        return new ApiResponse<>(1000, "Delete success", null);
    }
}
