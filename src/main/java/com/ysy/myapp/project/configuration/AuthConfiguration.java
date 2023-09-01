package com.ysy.myapp.project.configuration;

import com.ysy.myapp.project.util.HashUtil;
import com.ysy.myapp.project.util.JwtUtil;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AuthConfiguration {
    @Bean
    public HashUtil hashUtil() {
        return new HashUtil();
    }
    @Bean
    public JwtUtil jwtUtil() {
        return new JwtUtil();
    }
}
