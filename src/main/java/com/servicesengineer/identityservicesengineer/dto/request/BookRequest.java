package com.servicesengineer.identityservicesengineer.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDate;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class BookRequest {
    String title;
    String description;
    String authorId;
    String genreId;
    int stock;
    int status;
    LocalDate publicationDate;
    @NotBlank( message = "ISBN không được để trống")
    @Pattern(regexp = "\\d{13}", message = "ISBN phải gồm 13 chữ số")
    String isbn;
}
