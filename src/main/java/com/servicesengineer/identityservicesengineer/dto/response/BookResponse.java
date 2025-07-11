package com.servicesengineer.identityservicesengineer.dto.response;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDate;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class BookResponse {
    String id;
    String title;
    String description;
    AuthorResponse author;
    GenreResponse genre;
    LocalDate createdAt;
    int stock;
    int status;
    String isbn;
    LocalDate publicationDate;
    List<ImageResponse> images;
}
