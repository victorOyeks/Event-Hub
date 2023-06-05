package com.decagon.eventhubbe.controller;

import com.decagon.eventhubbe.dto.request.LoginRequest;
import com.decagon.eventhubbe.dto.request.RegistrationRequest;
import com.decagon.eventhubbe.dto.response.APIResponse;
import com.decagon.eventhubbe.dto.response.LoginResponse;
import com.decagon.eventhubbe.dto.response.RegistrationResponse;
import com.decagon.eventhubbe.service.AppUserService;
import com.decagon.eventhubbe.service.ConfirmationTokenService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AppUserService appUserService;
    private final ConfirmationTokenService confirmationTokenService;

    @PostMapping("/register")
    @CrossOrigin(origins = "http://localhost:5173")
    public ResponseEntity<APIResponse<RegistrationResponse>> register (@RequestBody RegistrationRequest registrationRequest, HttpServletRequest request) {
        APIResponse<RegistrationResponse> apiResponse = new APIResponse<>(appUserService.register(registrationRequest, request));
        return new ResponseEntity<>(apiResponse, HttpStatus.CREATED);
    }
    @PostMapping("/authenticate")
    @CrossOrigin(origins = "http://localhost:5173")
    public ResponseEntity<APIResponse<LoginResponse>> authenticate(@RequestBody LoginRequest loginRequest){
        APIResponse<LoginResponse> apiResponse = new APIResponse<>(appUserService.authenticate(loginRequest));
        return new ResponseEntity<>(apiResponse,HttpStatus.OK);
    }
    @GetMapping("/verify-email")
    @CrossOrigin(origins = "http://localhost:5173")
    public ResponseEntity<APIResponse<String>> verifyUser(@RequestParam("token") String token, HttpServletRequest request){
        APIResponse<String> apiResponse = new APIResponse<>(confirmationTokenService.verifyUser(token,request));
        return new ResponseEntity<>(apiResponse,HttpStatus.OK);
    }
    @GetMapping("/new-verification-link")
    @CrossOrigin(origins = "http://localhost:5173")
    public ResponseEntity<APIResponse<String>> resendAuthentication(@RequestParam ("email") String email, HttpServletRequest request){
        APIResponse<String> apiResponse = new APIResponse<>(confirmationTokenService.sendNewVerificationLink(email,request));
        return new ResponseEntity<>(apiResponse, HttpStatus.OK);
    }
    @GetMapping("/verify-password-token")
    @CrossOrigin(origins = "http://localhost:5173")
    public ResponseEntity<APIResponse<String>> forgotPassword(@RequestParam ("token") String token){
        APIResponse<String> apiResponse = new APIResponse<>(confirmationTokenService.forgotPassword(token));
        return new ResponseEntity<>(apiResponse,HttpStatus.OK);
    }
}
