package com.servicesengineer.identityservicesengineer.dto.request;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDate;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class BorrowingRequest {
    LocalDate dueDate;
    String studentCode;
    List<String> bookIds;
}
