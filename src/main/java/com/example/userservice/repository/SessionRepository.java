package com.example.userservice.repository;

import com.example.userservice.models.Session;
import com.example.userservice.models.SessionStatus;
import com.example.userservice.models.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Date;
import java.util.List;
import java.util.Optional;

public interface SessionRepository extends JpaRepository<Session, String> {
    
    Optional<Session> findByToken(String token);
    
    List<Session> findByUser(User user);
    
    List<Session> findByUserAndStatus(User user, SessionStatus status);
    
    List<Session> findByStatus(SessionStatus status);
    
    void deleteByExpiringAtBefore(Date date);
    
    void deleteByUserAndStatus(User user, SessionStatus status);
}

