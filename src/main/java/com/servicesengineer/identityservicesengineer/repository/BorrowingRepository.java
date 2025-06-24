package com.servicesengineer.identityservicesengineer.repository;

import com.servicesengineer.identityservicesengineer.entity.Borrowing;
import com.servicesengineer.identityservicesengineer.entity.BorrowingStatus;
import com.servicesengineer.identityservicesengineer.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface BorrowingRepository extends JpaRepository<Borrowing, String> {
    Page<Borrowing> findByUserUsername(String username, Pageable pageable);
    @Query("SELECT COUNT(b) FROM Borrowing b WHERE b.status = 'BORROWED'")
    long countBorrowed();

    @Query("SELECT COUNT(b) FROM Borrowing b WHERE b.status = 'OVERDUE'")
    long countOverdue();

    @Query("SELECT b FROM Borrowing b " +
            "JOIN b.user u " +
            "WHERE (:id IS NULL OR b.id = :id) " +
            "AND (:name IS NULL OR CONCAT(u.firstName, ' ', u.lastName) LIKE %:name%) " +
            "AND (:borrowDate IS NULL OR b.borrowDate = :borrowDate)")
    Page<Borrowing> findByBorrowIdAndNameAndBorrowDate(
            @Param("id") String id,
            @Param("name") String name,
            @Param("borrowDate") LocalDate borrowDate,
            Pageable pageable);
    boolean existsByUserAndStatusIn(User user, List<BorrowingStatus> statuses);

}
