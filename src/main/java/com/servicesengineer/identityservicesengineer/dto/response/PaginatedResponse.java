package com.servicesengineer.identityservicesengineer.dto.response;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.List;
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class PaginatedResponse<T> {
    int totalItems;
    int totalPages;
    int currentPage;
    int pageSize;
    boolean hasNextPage;
    boolean hasPreviousPage;
    List<T> elements;
}
