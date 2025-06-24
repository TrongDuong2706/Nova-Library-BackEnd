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
public class SimpleBookResponse {
    String favoriteId;
    String title;
    String bookId;
    String description;
    AuthorResponse author;
    GenreResponse genre;
    List<ImageResponse> images;
}
