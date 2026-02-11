package com.example.demoBe.util;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Component;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.*;

@Component
public class CookieUtil {

    private final ObjectMapper objectMapper = new ObjectMapper();


    public String getCookieValue(HttpServletRequest request, String cookieName) {
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if (cookieName.equals(cookie.getName())) {
                    String value = cookie.getValue();
                    // URL decode nếu là cookie remember-me (chứa JSON đã encode)
                    if ("remember-me".equals(cookieName) && value != null && !value.isEmpty()) {
                        try {
                            return URLDecoder.decode(value, StandardCharsets.UTF_8);
                        } catch (Exception e) {
                            return value; // Fallback nếu decode lỗi
                        }
                    }
                    return value;
                }
            }
        }
        return "";
    }


    public String addAccountToList(String existingList, String newEmail) {
        Set<String> accounts = new HashSet<>();
        
        if (existingList != null && !existingList.isEmpty()) {
            accounts.addAll(Arrays.asList(existingList.split("\\|")));
        }
        
        accounts.add(newEmail);
        return String.join("|", accounts);
    }

   
    public String removeAccountFromList(String existingList, String emailToRemove) {
        if (existingList == null || existingList.isEmpty()) {
            return "";
        }
        
        Set<String> accounts = new HashSet<>(Arrays.asList(existingList.split("\\|")));
        accounts.remove(emailToRemove);
        return String.join("|", accounts);
    }


    public Map<String, String> parseRememberMeMap(String jsonString) {
        if (jsonString == null || jsonString.isEmpty()) {
            return new HashMap<>();
        }
        try {
            return objectMapper.readValue(jsonString, new TypeReference<Map<String, String>>() {});
        } catch (Exception e) {
            return new HashMap<>();
        }
    }

    
    public String mapToJson(Map<String, String> map) {
        try {
            return objectMapper.writeValueAsString(map);
        } catch (Exception e) {
            return "{}";
        }
    }

    
    public String addRememberMe(String existingJson, String email, String encryptedToken) {
        Map<String, String> map = parseRememberMeMap(existingJson);
        map.put(email, encryptedToken);
        return mapToJson(map);
    }

    public String getRememberMeToken(String jsonString, String email) {
        Map<String, String> map = parseRememberMeMap(jsonString);
        return map.getOrDefault(email, null);
    }


    public String removeRememberMeToken(String existingJson, String email) {
        Map<String, String> map = parseRememberMeMap(existingJson);
        map.remove(email);
        return mapToJson(map);
    }
}
