package com.servicesengineer.identityservicesengineer.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Entity
@Table(name = "authors")
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
@Setter
public class Author {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;
    @Column(name = "name")
    private String name;

    @Column(name = "bio")
    private String bio;
    @ManyToMany(mappedBy = "authors" )
    private Set<Book> books = new HashSet<>();

}