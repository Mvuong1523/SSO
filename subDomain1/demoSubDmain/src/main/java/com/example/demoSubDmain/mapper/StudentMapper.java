package com.example.demoSubDmain.mapper;

import com.example.demoSubDmain.dto.CreateStudentReq;
import com.example.demoSubDmain.dto.SearchStudentReq;
import com.example.demoSubDmain.dto.UpdateStudentReq;
import com.example.demoSubDmain.entity.Student;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface StudentMapper {

    List<Student> findByCodeAndName(@Param("req") SearchStudentReq req, @Param("offset") int offset);

    int countByCodeAndName(SearchStudentReq req); // Đếm tổng số bản ghi thỏa mãn điều kiện

    int insert(CreateStudentReq student);

    int update(@Param("studentCode") String studentCode, 
               @Param("email") String email, 
               @Param("score") Double score);

    int deleteByCode(@Param("code") String code);

    int countByStudentCode(@Param("studentCode") String studentCode);

    int countByEmail(@Param("email") String email);

    int coutByEmailNotCode(@Param("email") String email,
            @Param("studentCode") String studentCode);
}
