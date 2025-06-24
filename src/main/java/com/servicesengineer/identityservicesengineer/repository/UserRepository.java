package com.servicesengineer.identityservicesengineer.repository;

import com.servicesengineer.identityservicesengineer.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, String> {
    Optional<User> findByUsername(String username);
    Optional<User> findByStudentCode(String studentCode);
    @Query("SELECT u FROM User u WHERE " +
            "(:name IS NULL OR :name = '' OR CONCAT(u.firstName, ' ', u.lastName) LIKE %:name%) AND " +
            "(:studentCode IS NULL OR :studentCode = '' OR u.studentCode = :studentCode) AND " +
            "(:phoneNumber IS NULL OR :phoneNumber = '' OR u.phoneNumber = :phoneNumber)")
    Page<User> findByNameAndStudentCodeAndPhoneNumber(
            @Param("name") String name,
            @Param("studentCode") String studentCode,
            @Param("phoneNumber") String phoneNumber,
            Pageable pageable);
}
