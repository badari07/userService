package com.example.userservice.service;


import com.example.userservice.exception.UserAlredayExitExecption;
import com.example.userservice.exception.WrongPasswordExecption;
import com.example.userservice.models.Role;
import com.example.userservice.models.Session;
import com.example.userservice.models.SessionStatus;
import com.example.userservice.models.User;
import com.example.userservice.repository.SessionRepository;
import com.example.userservice.repository.UserRepoistory;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class AuthService {
    private UserRepoistory userRepository;
    private BCryptPasswordEncoder passwordEncoder;
    private JwtService jwtService;
    private SessionRepository sessionRepository;

    public AuthService(UserRepoistory userRepository, BCryptPasswordEncoder passwordEncoder, 
                      JwtService jwtService, SessionRepository sessionRepository) {
        this.passwordEncoder = passwordEncoder;
        this.userRepository = userRepository;
        this.jwtService = jwtService;
        this.sessionRepository = sessionRepository;
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

    public String login(String email, String password) {
        Optional<User> user = userRepository.findByEmail(email);
        if (user.isEmpty()) {
            throw new UserAlredayExitExecption("User not found with email: " + email);
        }

        boolean matches = passwordEncoder.matches(password, user.get().getPassword());
        if (!matches) {
            throw new WrongPasswordExecption("Wrong password for email: " + email);
        }

        // Generate JWT token with roles
        User authenticatedUser = user.get();
        Set<Role> userRoles = authenticatedUser.getRoles();
        List<String> roleNames = userRoles.stream()
                .map(Role::getName)
                .filter(name -> name != null)
                .collect(Collectors.toList());
        
        String token = jwtService.generateToken(email, authenticatedUser.getId(), roleNames);
        
        // Get token expiration date
        Date expirationDate = jwtService.extractExpiration(token);
        
        // Save session to database
        Session session = new Session();
        session.setToken(token);
        session.setUser(authenticatedUser);
        session.setExpiringAt(expirationDate);
        session.setStatus(SessionStatus.Active);
        
        sessionRepository.save(session);
        
        return token;
    }
}
