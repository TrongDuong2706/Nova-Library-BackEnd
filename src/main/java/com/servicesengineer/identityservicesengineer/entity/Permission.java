package com.servicesengineer.identityservicesengineer.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.*;

@Entity
@Builder
@AllArgsConstructor
@Getter
@Setter
@NoArgsConstructor
public class Permission {
    @Id
    String name;

    String description;

}
