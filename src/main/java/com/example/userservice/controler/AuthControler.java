package com.example.userservice.controler;


import com.example.userservice.dto.*;

import com.example.userservice.exception.UserAlredayExitExecption;
import com.example.userservice.exception.WrongPasswordExecption;
import com.example.userservice.service.AuthService;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
public class AuthControler {
    private final AuthService authService;


    public AuthControler(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/sign_up")
    public ResponseEntity<SignResponseDTO> signUp(@RequestBody SignUpReqDTO Request){

        SignResponseDTO response = new SignResponseDTO();

        try {
            if ( authService.signUp(Request.email, Request.password)){
                response.setStatus(RequestStatus.SUCCESS);

            }else {
                response.setStatus(RequestStatus.FAILURE);
            }

            return ResponseEntity.status(HttpStatus.OK).body(response);

        } catch (Exception e) {
            response.setStatus(RequestStatus.FAILURE);

            return  new ResponseEntity<>(response, HttpStatus.CONFLICT);

        }

    }


    @PostMapping("/login")
    public ResponseEntity<LoginResDTO>  login(@RequestBody LoginReqDTO request){

        try {
            String token = authService.login(request.getEmail(), request.getPassword());
            LoginResDTO logDTO = new LoginResDTO();
            logDTO.setStatus(RequestStatus.SUCCESS);
            MultiValueMap <String, String> headers = new LinkedMultiValueMap<>();
            headers.add("AUTH_TOKEN", token);
            ResponseEntity<LoginResDTO> response = new ResponseEntity<>(logDTO,headers, HttpStatus.OK);

            return response;

        } catch (WrongPasswordExecption ex){
            LoginResDTO logDTO = new LoginResDTO();
            logDTO.setStatus(RequestStatus.FAILURE);
            return  new ResponseEntity<>(logDTO, HttpStatus.UNAUTHORIZED);
        } catch (UserAlredayExitExecption ex){
            LoginResDTO logDTO = new LoginResDTO();
            logDTO.setStatus(RequestStatus.FAILURE);
            return  new ResponseEntity<>(logDTO, HttpStatus.NOT_FOUND);
        }


    }
}

