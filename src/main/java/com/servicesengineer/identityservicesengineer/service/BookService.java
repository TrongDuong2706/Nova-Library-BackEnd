package com.servicesengineer.identityservicesengineer.service;

import com.servicesengineer.identityservicesengineer.dto.request.BookRequest;
import com.servicesengineer.identityservicesengineer.dto.response.AuthorResponse;
import com.servicesengineer.identityservicesengineer.dto.response.BookResponse;
import com.servicesengineer.identityservicesengineer.dto.response.PaginatedResponse;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface BookService {
    BookResponse createBook(BookRequest request, List<MultipartFile> files);

    PaginatedResponse<BookResponse> getAllBook(int page, int size);

    BookResponse getOneBook(String id);

    BookResponse updateBook(String id, BookRequest request, List<MultipartFile> files);

    PaginatedResponse<BookResponse> getAllBookWithFilter(String authorName, String genreName, String keyword, int page, int size);

    void softDelete(String id);

    long countActiveBooks();

    PaginatedResponse<BookResponse> getAllBookWithAdminFilter(String authorName, String genreName, String keyword, Integer status,String isbn, int page, int size);

    PaginatedResponse<BookResponse> getAllBookZeroStock(int page, int size);

    PaginatedResponse<BookResponse> getAllBookWithGenre(String genreName, int page, int size);
    PaginatedResponse<BookResponse> getAllBookByTitle(String title, int page, int size);
}
