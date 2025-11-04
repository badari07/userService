package com.example.userservice.service;


import com.example.userservice.exception.UserAlredayExitExecption;
import com.example.userservice.models.User;
import com.example.userservice.repository.UserRepoistory;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

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
        return  "token";

    }
}
