package com.example.userservice.models;


import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Entity
@Table(name = "roles")
@Data
@EqualsAndHashCode(callSuper = false)
@JsonDeserialize(as = Role.class)
public class Role extends  BaseModel {

    private String name;
}
