package com.example.userservice.controler;


import com.example.userservice.dto.LoginResDTOP;
import com.example.userservice.dto.SignResponseDTO;
import com.example.userservice.dto.SignUpReqDTO;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
public class AuthControler {


    @PostMapping("/sign_up")
    public SignResponseDTO signUp(SignUpReqDTO signUpReqDTO){

        return null;

    }


    @PostMapping("/login")
    public ResponseEntity<SignResponseDTO>  login(LoginResDTOP loginResDTOP){
        return null;

    }
}

