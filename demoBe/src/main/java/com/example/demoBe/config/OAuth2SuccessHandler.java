package com.example.demoBe.config;

import com.example.demoBe.entity.User;
import com.example.demoBe.mapper.UserMapper;
import com.example.demoBe.util.JwtUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class OAuth2SuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private JwtUtil jwtUtil;

    private static final String SESSION_USER_KEY = "SSO_USER_UID";

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
            Authentication authentication) throws IOException {
        OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();

        String email = oAuth2User.getAttribute("email");
        String name = oAuth2User.getAttribute("name");

        // Find or create user
        User user = userMapper.findByUserId(email);
        if (user == null) {
            user = new User();
            user.setUserId(email);
            user.setAuthProvider("GOOGLE");
            user.setUserType("CUSTOMER");
            userMapper.insertUser(user);
        }

        // Create session
        HttpSession session = request.getSession();
        session.setAttribute(SESSION_USER_KEY, user.getUserUid());

        // Generate JWT
        String token = jwtUtil.generateToken(user);

        // Redirect to frontend with token
        String redirectUrl = "http://localhost:3000/oauth/callback?token=" + token;
        getRedirectStrategy().sendRedirect(request, response, redirectUrl);
    }
}
