package com.servicesengineer.identityservicesengineer.repository;

import com.servicesengineer.identityservicesengineer.dto.response.MonthlyBorrowStatResponse;
import com.servicesengineer.identityservicesengineer.entity.BorrowingRecordItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface BorrowingRecordItemRepository extends JpaRepository<BorrowingRecordItem, String> {
    @Query(value = """
    SELECT 
      DATE_FORMAT(borrowing.borrow_date, '%Y-%m') AS monthYear,
      COUNT(borrowing_record_items.id) AS totalBooksBorrowed
    FROM 
      borrowing
    JOIN 
      borrowing_record_items ON borrowing.id = borrowing_record_items.borrowing_id
    GROUP BY 
      DATE_FORMAT(borrowing.borrow_date, '%Y-%m')
    ORDER BY 
      DATE_FORMAT(borrowing.borrow_date, '%Y-%m')
""", nativeQuery = true)
    List<Object[]> countBooksBorrowedByMonthRaw();



}
