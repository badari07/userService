package com.example.userservice.models;

import jakarta.persistence.*;
import lombok.Data;


@MappedSuperclass
@Data
public class BaseModel {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

}
