package com.haris.client.controller;

import com.haris.client.model.ResetPasswordModel;
import com.haris.client.entity.User;
import com.haris.client.entity.VerificationToken;
import com.haris.client.event.RegistrationCompleteEvent;
import com.haris.client.model.UserModel;
import com.haris.client.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;
import java.util.UUID;

@RestController
@Slf4j
public class RegistrationController{

    @Autowired
    private UserService userService;
    @Autowired
    private ApplicationEventPublisher publisher;

    @PostMapping("/register")
    public String registerUser(@RequestBody UserModel userModel, final HttpServletRequest request){
        log.info("Before user function");
        User user = userService.registerUser(userModel);
        log.info("After user function");
        publisher.publishEvent(new RegistrationCompleteEvent(
                user, applicationUrl(request)
        ));
        return "Success";
    }
    @GetMapping("/verifyRegistration")
    public String verifyRegistration(@RequestParam("token") String token){
        String result = userService.validateVerificationToken(token);
        if(result.equalsIgnoreCase("valid")){
            return "User Verified Successfully.";
        }
        return "Bad User";
    }

    @GetMapping("/resendVerificationToken")
    public String resendVerificationToken(@RequestParam("token") String oldToken, final HttpServletRequest request){
        VerificationToken verificationToken = userService.generateNewVerificationToken(oldToken);
        User user = verificationToken.getUser();
        resendVerificationTokenMail(oldToken,applicationUrl(request));
        return "Verification Token Sent";
    }
    @PostMapping("/resetPassword")
    public String resetPassword(@RequestBody ResetPasswordModel resetPassword, final HttpServletRequest request){
        User user = userService.findUserByEmail(resetPassword.getEmail());
        String url = "";
        if(user!=null){
            String token = UUID.randomUUID().toString();
            userService.createResetPasswordToken(user,token);
            url =  sendPasswordResetTokenMail(token,applicationUrl(request));
            return url;
        }
        return "user is not registered";
    }
    @PostMapping("/savePassword")
    public String savePassword(@RequestParam("token") String token,
                               @RequestBody ResetPasswordModel resetPasswordModel){
        String result = userService.validatePasswordResetToken(token);
        if(!result.equalsIgnoreCase("valid")){
            return "Invalid token";
        }
        Optional<User> user = userService.getUserByPasswordResetToken(token);
        if(user.isPresent()){
            userService.resetPassword(user.get(),resetPasswordModel.getNewPassword());
            return "Password reset successfully";
        }
        return "Invalid token";
    }

    private String sendPasswordResetTokenMail(String token, String applicationUrl) {
        //Send mail to user
        String url = applicationUrl + "/savePassword?token="+token;
        //resetPasswordToken()
        log.info("Click the link to reset password: "+url);
        return url;
    }

    private void resendVerificationTokenMail(String token, String applicationUrl) {
        //Send mail to user
        String url = applicationUrl + "/verifyRegistration?token="+token;
        //sendVerificationMethod()
        log.info("Click the link to verify your account: "+url);
    }

    private String applicationUrl(HttpServletRequest request) {
        return "http://"+request.getServerName()+":"+request.getServerPort()+request.getContextPath();
    }
}
