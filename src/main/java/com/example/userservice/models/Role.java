package com.example.userservice.models;


import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import jakarta.persistence.Entity;
import lombok.Data;

@Entity
@Data
@JsonDeserialize(as = Role.class)
public class Role extends  BaseModel {

    private String name;
}
