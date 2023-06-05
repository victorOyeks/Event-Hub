package com.decagon.eventhubbe.exception;

public class  AppUserNotFoundException extends RuntimeException{
    public AppUserNotFoundException(String email) {
        super("User with email : "+email+ " not found");
    }
}
