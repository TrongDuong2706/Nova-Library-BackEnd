package com.servicesengineer.identityservicesengineer.service;

import com.servicesengineer.identityservicesengineer.dto.response.BookResponse;
import com.servicesengineer.identityservicesengineer.dto.response.PaginatedResponse;
import com.servicesengineer.identityservicesengineer.dto.response.SimpleBookResponse;

public interface FavoriteBookService {
    SimpleBookResponse addFavorite(String bookId);
    PaginatedResponse<SimpleBookResponse> getAllFavorite(int page, int size);
    void removeFavorite(String bookId);
}
