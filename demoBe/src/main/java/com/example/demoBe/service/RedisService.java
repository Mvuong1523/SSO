package com.example.demoBe.service;

import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class RedisService {


    private final Map<String, String> inMemoryStore = new ConcurrentHashMap<>();


    public void saveRefreshToken(String token, Long userUid) {
        inMemoryStore.put(token, String.valueOf(userUid));
    }

    public boolean hasRefreshToken(String token) {
        return inMemoryStore.containsKey(token);
    }

    public Object getRefreshToken(String token) {
        return inMemoryStore.get(token);
    }

    public void deleteRefreshToken(String token) {
        inMemoryStore.remove(token);
    }
}
