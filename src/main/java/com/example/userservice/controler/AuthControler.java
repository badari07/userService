package com.example.userservice.controler;


import com.example.userservice.dto.*;

import com.example.userservice.exception.UserAlredayExitExecption;
import com.example.userservice.exception.WrongPasswordExecption;
import com.example.userservice.service.AuthService;
import com.example.userservice.service.JwtService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Date;
import java.util.List;

@RestController
@RequestMapping("/auth")
public class AuthControler {
    private final AuthService authService;
    private final JwtService jwtService;


    public AuthControler(AuthService authService, JwtService jwtService) {
        this.authService = authService;
        this.jwtService = jwtService;
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

    @PostMapping("/validate")
    public ResponseEntity<ValidateTokenResDTO> validateToken(@RequestBody ValidateTokenReqDTO request) {
        ValidateTokenResDTO response = new ValidateTokenResDTO();

        try {
            String token = request.getToken();
            
            if (token == null || token.trim().isEmpty()) {
                response.setStatus(RequestStatus.FAILURE);
                response.setIsValid(false);
                response.setMessage("Token is required");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
            }

            // Validate token
            Boolean isValid = jwtService.validateToken(token);
            
            if (isValid) {
                // Extract token information
                String email = jwtService.extractEmail(token);
                String userId = jwtService.extractUserId(token);
                List<String> roles = jwtService.extractRoles(token);
                Date expiration = jwtService.extractExpiration(token);
                
                response.setStatus(RequestStatus.SUCCESS);
                response.setIsValid(true);
                response.setEmail(email);
                response.setUserId(userId);
                response.setRoles(roles);
                response.setExpirationTime(expiration.getTime());
                response.setMessage("Token is valid");
                
                return ResponseEntity.status(HttpStatus.OK).body(response);
            } else {
                response.setStatus(RequestStatus.FAILURE);
                response.setIsValid(false);
                response.setMessage("Token is invalid or expired");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
            }
            
        } catch (Exception e) {
            response.setStatus(RequestStatus.FAILURE);
            response.setIsValid(false);
            response.setMessage("Error validating token: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
    }
}

