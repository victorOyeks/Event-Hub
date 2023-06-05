package com.decagon.eventhubbe.service;

import com.decagon.eventhubbe.dto.request.LoginRequest;
import com.decagon.eventhubbe.dto.request.RegistrationRequest;
import com.decagon.eventhubbe.dto.response.LoginResponse;
import com.decagon.eventhubbe.dto.response.RegistrationResponse;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Service;

@Service
public interface AppUserService {
    RegistrationResponse register(RegistrationRequest registrationRequest,
                                  HttpServletRequest request);

    LoginResponse authenticate(LoginRequest loginRequest);
}