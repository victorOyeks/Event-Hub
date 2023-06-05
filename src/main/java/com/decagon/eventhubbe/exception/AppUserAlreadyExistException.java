package com.decagon.eventhubbe.exception;

public class AppUserAlreadyExistException extends RuntimeException{
    public AppUserAlreadyExistException(String email) {
        super("User with email : "+email+ " already registered");
    }
}
