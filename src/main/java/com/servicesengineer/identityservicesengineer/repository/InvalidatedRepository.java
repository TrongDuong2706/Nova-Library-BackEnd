package com.servicesengineer.identityservicesengineer.repository;

import com.servicesengineer.identityservicesengineer.entity.InvalidatedToken;
import org.springframework.data.jpa.repository.JpaRepository;

public interface InvalidatedRepository extends JpaRepository<InvalidatedToken, String> {
}
