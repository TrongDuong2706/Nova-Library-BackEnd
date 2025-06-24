package com.servicesengineer.identityservicesengineer.service;

import com.servicesengineer.identityservicesengineer.dto.request.GenreRequest;
import com.servicesengineer.identityservicesengineer.dto.response.GenreResponse;
import com.servicesengineer.identityservicesengineer.dto.response.PaginatedResponse;
import com.servicesengineer.identityservicesengineer.entity.Genre;

public interface GenreService {
    GenreResponse createGenre(GenreRequest request);
    PaginatedResponse<GenreResponse> getAllGenre(int page, int size);
    GenreResponse getOneGenre(String id);
    GenreResponse updateGenre(String id, GenreRequest request);
    String deleteGenre(String id);
}
