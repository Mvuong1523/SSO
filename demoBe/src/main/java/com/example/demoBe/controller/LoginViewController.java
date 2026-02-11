package com.example.demoBe.controller;

import com.example.demoBe.util.CookieUtil;
import com.example.demoBe.util.RememberMeUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.ui.Model;


import jakarta.servlet.http.HttpServletRequest;

@Controller
public class LoginViewController {
    @Autowired
    private CookieUtil cookieUtil;

    @Autowired
    private RememberMeUtil rememberMeUtil;


    @GetMapping("/login")
    public String loginPage(
        @RequestParam(required = false) String redirect_uri,
        @RequestParam(required = false) String email,
        HttpServletRequest request,
        Model model) {
        
        model.addAttribute("redirect_uri", redirect_uri != null ? redirect_uri : "");
        
        // Nếu có email param, load credentials
        if (email != null && !email.isEmpty()) {
            try {
                String rememberMeJson = cookieUtil.getCookieValue(request, "remember-me");
                String encryptedToken = cookieUtil.getRememberMeToken(rememberMeJson, email);
                
                if (encryptedToken != null) {
                    String decrypted = rememberMeUtil.decrypt(encryptedToken);
                    String[] parts = decrypted.split(":");
                    
                    if (parts.length == 2) {
                        model.addAttribute("savedEmail", parts[0]);
                        model.addAttribute("savedPassword", parts[1]);
                    }
                }
            } catch (Exception e) {
                // Ignore error
            }
        }
        
        return "login";
    }

    @GetMapping("/register")
    public String registerPage() {
        return "register";
    }
}
