package com.service.auth.services;



import com.example.common.dto.UserAuthDetails;
import com.service.auth.entity.User;
import com.service.auth.repository.UserRepository;
import com.service.auth.security.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    @Autowired
    private UserRepository repository;
    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtUtil jwtUtil;

    public String saveUser(User user) {
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        repository.save(user);
        return "user added to the system";
    }

    public String generateToken(Authentication authentication) {
        return jwtUtil.generateToken(authentication);
    }

    public UserAuthDetails validateToken(String token) {
      return   jwtUtil.validateAndExtractDetails(token);
    }


}