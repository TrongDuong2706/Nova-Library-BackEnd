package com.servicesengineer.identityservicesengineer.controller;

import com.servicesengineer.identityservicesengineer.dto.ApiResponse;
import com.servicesengineer.identityservicesengineer.dto.request.BookRenewalRequest;
import com.servicesengineer.identityservicesengineer.dto.request.BorrowingRequest;
import com.servicesengineer.identityservicesengineer.dto.response.BorrowingResponse;
import com.servicesengineer.identityservicesengineer.dto.response.PaginatedResponse;
import com.servicesengineer.identityservicesengineer.service.BorrowingService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequestMapping("/borrowings")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class BorrowingController {

    BorrowingService borrowingService;

    @PostMapping()
    public ApiResponse<BorrowingResponse> createBorrowing(@RequestBody BorrowingRequest borrowingRequest) {
        return ApiResponse.<BorrowingResponse>builder()
                .message("Create Borrow Successful")
                .result(borrowingService.createBorrowing(borrowingRequest))
                .build();
    }

    @PutMapping("/{borrowId}")
    public ApiResponse<BorrowingResponse> returnBorrow(@PathVariable String borrowId) {
        return ApiResponse.<BorrowingResponse>builder()
                .message("Return Borrow Successful")
                .result(borrowingService.returnBorrow(borrowId))
                .build();
    }

    @GetMapping()
    public ApiResponse<PaginatedResponse<BorrowingResponse>> getAllBorrow(@RequestParam(defaultValue = "1") int page,
                                                                          @RequestParam(defaultValue = "10") int size) {
        int adjustedPage = Math.max(page - 1, 0);
        return ApiResponse.<PaginatedResponse<BorrowingResponse>>builder()
                .message("Get All Borrow Successful")
                .result(borrowingService.getAllBorrow(adjustedPage, size))
                .build();
    }

    @GetMapping("/{borrowId}")
    public ApiResponse<BorrowingResponse> getOneBorrow(@PathVariable String borrowId) {
        return ApiResponse.<BorrowingResponse>builder()
                .message("Get One Borrow Successful")
                .result(borrowingService.getOneBorrow(borrowId))
                .build();
    }

    @GetMapping("/getMyBorrow")
    public ApiResponse<PaginatedResponse<BorrowingResponse>> getMyBorrow(@RequestParam(defaultValue = "1") int page,
                                                                         @RequestParam(defaultValue = "10") int size) {
        int adjustedPage = Math.max(page - 1, 0);

        return ApiResponse.<PaginatedResponse<BorrowingResponse>>builder()
                .message("Get My Borrow Successful")
                .result(borrowingService.getAllMyBorrow(adjustedPage, size))
                .build();
    }

    @GetMapping("/countBorrow")
    public ApiResponse<Long> getCountBorrow() {
        return ApiResponse.<Long>builder()
                .message("Get Count Borrow Successful")
                .result(borrowingService.countBorrow())
                .build();
    }

    @GetMapping("/countOverdue")
    public ApiResponse<Long> getCountOverdue() {
        return ApiResponse.<Long>builder()
                .message("Get Count Overdue Successful")
                .result(borrowingService.countOverdue())
                .build();
    }

    @GetMapping("/filter")
    public ApiResponse<PaginatedResponse<BorrowingResponse>> getAllBorrowWithFilter(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String id,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) LocalDate borrowDate) {
        int adjustedPage = Math.max(page - 1, 0);
        return ApiResponse.<PaginatedResponse<BorrowingResponse>>builder()
                .message("Get All Borrow filter successful")
                .result(borrowingService.getAllBorrowWithFilter(id, name, borrowDate, adjustedPage, size))
                .build();
    }
    @PutMapping("/extends/{borrowId}")
    public ApiResponse<Void> bookRenewal(@PathVariable String borrowId, @RequestBody BookRenewalRequest request){
        borrowingService.bookRenewal(borrowId, request);
        return ApiResponse.<Void>builder()
                .message("Book Renewal successful")
                .result(null)
                .build();
    }
    @PostMapping("/update-overdue-now")
    public ApiResponse<Void> updateOverdueNow() {
        borrowingService.updateOverdueStatuses();
        return ApiResponse.<Void>builder()
                .message("Update status successful")
                .result(null)
                .build();
    }
    @GetMapping("/getOverDueStatus")
    public ApiResponse<PaginatedResponse<BorrowingResponse>> getAllBorrowOverDueStatus(@RequestParam(defaultValue = "1") int page,
                                                                                       @RequestParam(defaultValue = "10") int size){
        int adjustedPage = Math.max(page - 1, 0);
        return ApiResponse.<PaginatedResponse<BorrowingResponse>>builder()
                .message("Get all overdue borrow successful")
                .result(borrowingService.getAllBorrowWithOverdueStatus(adjustedPage,size))
                .build();
    }


}
