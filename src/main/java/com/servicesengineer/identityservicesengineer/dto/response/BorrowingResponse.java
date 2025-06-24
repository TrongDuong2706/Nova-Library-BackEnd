package com.servicesengineer.identityservicesengineer.dto.response;

import com.servicesengineer.identityservicesengineer.entity.BorrowingStatus;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDate;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class BorrowingResponse {
    String id;
     LocalDate borrowDate;
     LocalDate dueDate;
     LocalDate returnDate;
     Double finalAmount;
     BorrowingStatus status;
     UserResponse userResponse;
    List<BookResponse> books;
}
