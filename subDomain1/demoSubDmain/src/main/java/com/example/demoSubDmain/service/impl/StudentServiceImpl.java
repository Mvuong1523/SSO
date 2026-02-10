package com.example.demoSubDmain.service.impl;

import com.example.demoSubDmain.dto.CreateStudentReq;
import com.example.demoSubDmain.dto.SearchStudentReq;
import com.example.demoSubDmain.dto.UpdateStudentReq;
import com.example.demoSubDmain.entity.Student;
import com.example.demoSubDmain.mapper.StudentMapper;
import com.example.demoSubDmain.service.StudentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

import com.example.demoSubDmain.dto.PageResponse;

@Service
public class StudentServiceImpl implements StudentService {

    private final StudentMapper studentMapper;

    public StudentServiceImpl(StudentMapper studentMapper) {
        this.studentMapper = studentMapper;
    }

    @Override
    public PageResponse<Student> search(SearchStudentReq req) {
        int offset = (req.getPage() - 1) * req.getSize();

        List<Student> students = studentMapper.findByCodeAndName(req, offset);
        int total = studentMapper.countByCodeAndName(req);

        return PageResponse.<Student>builder()
                .data(students)
                .total(total)
                .page(req.getPage())
                .size(req.getSize())
                .build();
    }

    @Override
    public void create(CreateStudentReq req) {
        // check code
        if (studentMapper.countByStudentCode(req.getStudentCode()) > 0) {
            throw new RuntimeException("ma hoc sinh da ton tai");
        }

        // check email
        if (studentMapper.countByEmail(req.getEmail()) > 0) {
            throw new RuntimeException("email da ton tai");
        }

        studentMapper.insert(req);
    }

    @Override
    public void update(String code, UpdateStudentReq req) {
        // check email co bi trung khong tru email hien tai
        if (studentMapper.coutByEmailNotCode(req.getEmail(), code) > 0) {
            throw new RuntimeException("email da ton tai");
        }
        studentMapper.update(code, req.getEmail(), req.getScore());
    }

    @Override
    public void deleteByCode(String code) {

        // checo studentCode co ton tai khong
        if (studentMapper.countByStudentCode(code) == 0) {
            throw new RuntimeException("ma hoc sinh khon ton tai");
        }
        studentMapper.deleteByCode(code);
    }
}
