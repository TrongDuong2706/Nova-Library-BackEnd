package com.servicesengineer.identityservicesengineer.service;

import com.servicesengineer.identityservicesengineer.dto.request.BookRenewalRequest;
import com.servicesengineer.identityservicesengineer.dto.request.BorrowingRequest;
import com.servicesengineer.identityservicesengineer.dto.response.BorrowingResponse;
import com.servicesengineer.identityservicesengineer.dto.response.PaginatedResponse;
import com.servicesengineer.identityservicesengineer.entity.Borrowing;

import java.time.LocalDate;

public interface BorrowingService {
    BorrowingResponse createBorrowing(BorrowingRequest borrowingRequest);
    BorrowingResponse returnBorrow(String borrowId);
    PaginatedResponse<BorrowingResponse> getAllBorrow(int page, int size);
    BorrowingResponse getOneBorrow (String borrowId);
    PaginatedResponse<BorrowingResponse> getAllMyBorrow(int page, int size);
    long countBorrow();
    long countOverdue();
    PaginatedResponse<BorrowingResponse> getAllBorrowWithFilter(String id, String name, LocalDate borrowDate, int page, int size);
    void bookRenewal(String borrowId, BookRenewalRequest bookRenewalRequest);
    void updateOverdueStatuses();
    PaginatedResponse<BorrowingResponse> getAllBorrowWithOverdueStatus(int page, int size);
    PaginatedResponse<BorrowingResponse> getAllBorrowByUserId(String userId, int page, int size);
}
