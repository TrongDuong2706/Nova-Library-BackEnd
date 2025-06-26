package com.servicesengineer.identityservicesengineer.dto.response;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class MonthlyBorrowStatResponse {
    String monthYear;
    long totalBooksBorrowed;
}
