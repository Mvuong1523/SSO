package com.example.demoBe.service;

import com.example.demoBe.entity.User;
import com.example.demoBe.mapper.UserMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class CustomUserDetailsService implements UserDetailsService {
    @Autowired
    private UserMapper userMapper;
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userMapper.findByUserId(username);
        if (user == null){
            throw  new UsernameNotFoundException("User not found: " + username);
        }

        return  org.springframework.security.core.userdetails.User
                .withUsername(user.getUserId())
                .password(user.getPwd())
                .build();
    }
}
