package com.example.demoBe.mapper;

import com.example.demoBe.entity.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface UserMapper {
    User findByUserId(@Param("userId") String userId);

    User findByUserUid(@Param("userUid") Long userUid);

    User findByEmail(@Param("email") String email);

    void insertUser(User user);

    void updateUser(User user);

    void updatePassword(@Param("userUid") Long userUid, @Param("pwd") String pwd);
}
