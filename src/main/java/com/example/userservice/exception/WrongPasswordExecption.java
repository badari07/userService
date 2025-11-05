package com.example.userservice.exception;

public class WrongPasswordExecption extends RuntimeException{
    public WrongPasswordExecption(String message) {
        super(message);
    }
}
