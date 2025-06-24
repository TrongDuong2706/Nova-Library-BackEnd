package com.servicesengineer.identityservicesengineer.repository;

import com.servicesengineer.identityservicesengineer.entity.Author;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AuthorRepository extends JpaRepository<Author, String> {
}
