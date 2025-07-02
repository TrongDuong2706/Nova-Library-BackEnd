package com.servicesengineer.identityservicesengineer.dto.response;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class BookResponse {
    String id;
    String title;
    String description;
    Set<AuthorResponse> authors; // Giả sử bạn có AuthorResponse DTO
    Set<GenreResponse> genres;
    LocalDate createdAt;
    int stock;
    int status;
    String isbn;
    LocalDate publicationDate;
    List<ImageResponse> images;
}
