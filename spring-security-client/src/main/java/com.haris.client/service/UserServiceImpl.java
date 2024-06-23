package com.haris.client.service;

import com.haris.client.entity.ResetPasswordToken;
import com.haris.client.entity.User;
import com.haris.client.entity.VerificationToken;
import com.haris.client.model.UserModel;
import com.haris.client.repository.ResetPasswordTokenRepository;
import com.haris.client.repository.UserRepository;
import com.haris.client.repository.VerificationTokenRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Calendar;
import java.util.Optional;
import java.util.UUID;

@Service
public class UserServiceImpl implements UserService{
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private VerificationTokenRepository verificationTokenRepository;
    @Autowired
    private ResetPasswordTokenRepository resetPasswordTokenRepository;
    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public User registerUser(UserModel userModel) {
        User user = new User();
        user.setEmail(userModel.getEmail());
        user.setFirstName(userModel.getFirstName());
        user.setLastName(userModel.getLastName());
        user.setRole("USER");
        user.setPassword(passwordEncoder.encode(userModel.getPassword()));
        userRepository.save(user);
        return user;
    }

    @Override
    public void saveVerificationTokenForUser(String token, User user) {
        VerificationToken verificationToken = new VerificationToken(user,token);
        verificationTokenRepository.save(verificationToken);
    }

    @Override
    public String validateVerificationToken(String token) {
        VerificationToken verificationToken = verificationTokenRepository.findByToken(token);
        if(verificationToken==null){
            return "Invalid token";
        }
        User user = verificationToken.getUser();
        Calendar calendar = Calendar.getInstance();
        if(verificationToken.getExpirationTime().getTime()-calendar.getTime().getTime()<=0){
            verificationTokenRepository.delete(verificationToken);
            return "Token is expired";
        }
        user.setEnabled(true);
        userRepository.save(user);
        return "valid";
    }

    @Override
    public VerificationToken generateNewVerificationToken(String oldToken) {
        VerificationToken verificationToken = verificationTokenRepository.findByToken(oldToken);
        verificationToken.setToken(UUID.randomUUID().toString());
        verificationTokenRepository.save(verificationToken);
        return verificationToken;
    }

    @Override
    public User findUserByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    @Override
    public void createResetPasswordToken(User user, String token) {
        ResetPasswordToken oldResetPasswordToken = resetPasswordTokenRepository.findByUser(user);
        if(oldResetPasswordToken!=null){
            resetPasswordTokenRepository.delete(oldResetPasswordToken);
        }
        ResetPasswordToken resetPasswordToken = new ResetPasswordToken(user,token);
        resetPasswordTokenRepository.save(resetPasswordToken);
    }

    @Override
    public String validatePasswordResetToken(String token) {
        ResetPasswordToken resetPasswordToken = resetPasswordTokenRepository.findByToken(token);
        if(resetPasswordToken==null){
            return "Invalid token";
        }
        Calendar calendar = Calendar.getInstance();
        if(resetPasswordToken.getExpirationTime().getTime()-calendar.getTime().getTime()<=0){
            resetPasswordTokenRepository.delete(resetPasswordToken);
            return "Token is expired";
        }
        return "valid";
    }

    @Override
    public Optional<User> getUserByPasswordResetToken(String token) {
        return Optional.ofNullable(resetPasswordTokenRepository.findByToken(token).getUser());
    }

    @Override
    public void changePassword(User user, String newPassword) {
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
    }

    @Override
    public boolean checkIfValidOldPassword(User user, String oldPassword) {
        return passwordEncoder.matches(oldPassword,user.getPassword());
    }
}
