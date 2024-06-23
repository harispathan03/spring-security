package com.haris.client.config;

import org.springframework.context.annotation.Bean;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.stereotype.Component;

@EnableWebSecurity
@Component
public class WebSecurityConfig {

    private static final String[] WHITE_LIST_URLS = {"/hello","/register", "/verifyRegistration","/resendVerificationToken", "/resetPassword", "/savePassword", "/changePassword"};
    @Bean
    public PasswordEncoder passwordEncoder(){
        return new BCryptPasswordEncoder(11);
    }

    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity httpSecurity) throws Exception {
        httpSecurity
                .csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests((authorize) -> authorize.requestMatchers(WHITE_LIST_URLS).permitAll())
                .authorizeHttpRequests((authorize) -> authorize.requestMatchers("/api/**").authenticated())
//                .oauth2Login(oauth2login -> oauth2login.loginPage("/oauth/authorization/api-client-oidc"))
                .oauth2Client(Customizer.withDefaults())
                ;
        return httpSecurity.build();
    }
}
