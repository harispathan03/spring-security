package com.haris.client.repository;

import com.haris.client.entity.ResetPasswordToken;
import com.haris.client.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ResetPasswordTokenRepository extends JpaRepository<ResetPasswordToken,Long> {
    ResetPasswordToken findByToken(String token);

    ResetPasswordToken findByUser(User user);
}
