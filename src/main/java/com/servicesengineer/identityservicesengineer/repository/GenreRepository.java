package com.servicesengineer.identityservicesengineer.repository;

import com.servicesengineer.identityservicesengineer.entity.Genre;
import org.springframework.data.jpa.repository.JpaRepository;

public interface GenreRepository extends JpaRepository<Genre, String> {
}
