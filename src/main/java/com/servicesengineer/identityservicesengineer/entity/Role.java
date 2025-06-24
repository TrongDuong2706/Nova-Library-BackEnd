package com.servicesengineer.identityservicesengineer.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToMany;
import lombok.*;

import java.util.Set;

@Entity
@Builder
@AllArgsConstructor
@Getter
@Setter
@NoArgsConstructor
public class Role {
    @Id
    String name;

    String description;

    @ManyToMany
    Set<Permission> permissions;

}
