package com.example.demoBe.mapper;

import com.example.demoBe.entity.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface UserMapper {
    User findByUserId(@Param("userId") String userId);

    User findByUserUid(@Param("userUid") Long userUid);

    void insertUser(User user);

}
