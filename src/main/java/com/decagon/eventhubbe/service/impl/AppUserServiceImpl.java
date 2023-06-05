package com.decagon.eventhubbe.service.impl;

import com.decagon.eventhubbe.domain.entities.AppUser;
import com.decagon.eventhubbe.domain.entities.JwtToken;
import com.decagon.eventhubbe.domain.repository.AppUserRepository;
import com.decagon.eventhubbe.domain.repository.JwtTokenRepository;
import com.decagon.eventhubbe.dto.request.LoginRequest;
import com.decagon.eventhubbe.dto.request.RegistrationRequest;
import com.decagon.eventhubbe.dto.response.LoginResponse;
import com.decagon.eventhubbe.dto.response.RegistrationResponse;
import com.decagon.eventhubbe.events.register.RegistrationEvent;
import com.decagon.eventhubbe.exception.AppUserAlreadyExistException;
import com.decagon.eventhubbe.exception.AppUserNotFoundException;
import com.decagon.eventhubbe.exception.InvalidCredentialsException;
import com.decagon.eventhubbe.exception.UserDisabledException;
import com.decagon.eventhubbe.security.JwtService;
import com.decagon.eventhubbe.service.AppUserService;
import com.decagon.eventhubbe.utils.EmailUtils;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Date;


@Service
@RequiredArgsConstructor
public class AppUserServiceImpl implements AppUserService {

    private final AppUserRepository appUserRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenRepository jwtTokenRepository;
    private final ModelMapper modelMapper;
    private final JwtService jwtService;
    private final ApplicationEventPublisher publisher;
    @Value("${jwt.expiration}")
    private long expiration;

    @Override
    public RegistrationResponse register(RegistrationRequest registrationRequest,
                                         HttpServletRequest request) {
        validateUserExistence(registrationRequest.getEmail());
        AppUser appUser = registrationRequestToAppUser(registrationRequest);
        appUser.setPassword(passwordEncoder.encode(appUser.getPassword()));
        appUser.setEnabled(false);
        AppUser savedUser = appUserRepository.insert(appUser);
        publisher.publishEvent(new RegistrationEvent(appUser, EmailUtils.applicationUrl(request)));
        return RegistrationResponse.builder()
                .firstName(savedUser.getFirstName())
                .lastName(savedUser.getLastName())
                .message("Registration Successful")
                .build();
    }
    @Override
    public LoginResponse authenticate(LoginRequest loginRequest){
        AppUser appUser = getUserByEmail(loginRequest.getEmail());
        if(appUser.getEnabled().equals(false)){
            throw new UserDisabledException("Account is Disabled");
        }
        if(!passwordEncoder.matches(loginRequest.getPassword(), appUser.getPassword())){
            throw new InvalidCredentialsException("Passwords do not match");
        }
        Authentication authentication = new UsernamePasswordAuthenticationToken(
                appUser.getEmail(),appUser.getPassword());
        String accessToken = jwtService.generateToken(authentication);
        String refreshToken = jwtService.generateRefreshToken(authentication);
        JwtToken jwtToken = JwtToken.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .appUser(appUser)
                .generatedAt(new Date(System.currentTimeMillis()))
                .expiresAt(new Date(System.currentTimeMillis()+expiration))
                .build();
        JwtToken savedToken = jwtTokenRepository.save(jwtToken);
        SecurityContextHolder.getContext().setAuthentication(authentication);
        return LoginResponse.builder()
                .accessToken(savedToken.getAccessToken())
                .refreshToken(savedToken.getRefreshToken())
                .userFullName(appUser.getFirstName()+" "+appUser.getLastName())
                .message("Login Successful")
                .build();
    }


    private AppUser registrationRequestToAppUser (RegistrationRequest registrationRequest) {
        return modelMapper.map(registrationRequest, AppUser.class);
    }
    private void validateUserExistence(String email){
        if(appUserRepository.existsByEmail(email)){
            throw new AppUserAlreadyExistException(email);
        }
    }
    public AppUser getUserByEmail(String email){
        return appUserRepository.findByEmail(email)
                .orElseThrow(()-> new AppUserNotFoundException(email));
    }

}
