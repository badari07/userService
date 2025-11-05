package com.example.userservice.service;


import com.example.userservice.exception.UserAlredayExitExecption;
import com.example.userservice.exception.WrongPasswordExecption;
import com.example.userservice.models.User;
import com.example.userservice.repository.UserRepoistory;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class AuthService {
    private UserRepoistory userRepository;
    private BCryptPasswordEncoder passwordEncoder;

    public AuthService(UserRepoistory userRepository, BCryptPasswordEncoder passwordEncoder) {
        this.passwordEncoder = passwordEncoder;
        this.userRepository = userRepository;
    }


    public boolean signUp( String email, String password){
        if(userRepository.findByEmail(email).isPresent()){
               throw new UserAlredayExitExecption("User already exists with email: " + email);
        }

        User user = new User();
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode(password));
        userRepository.save(user);

 return  true;
    }

    public String login( String email, String password){

        Optional<User> user = userRepository.findByEmail(email);
        if(user.isEmpty()){
            throw new UserAlredayExitExecption("User not found with email: " + email);
        }
       boolean matches = passwordEncoder.matches(password, user.get().getPassword());
         if(!matches){
                throw new WrongPasswordExecption("Wrong password for email: " + email);
         }else {
             return "token";
         }


    }
}
