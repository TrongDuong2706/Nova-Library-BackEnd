package com.servicesengineer.identityservicesengineer.service;

import com.servicesengineer.identityservicesengineer.dto.request.AuthorRequest;
import com.servicesengineer.identityservicesengineer.dto.response.AuthorResponse;
import com.servicesengineer.identityservicesengineer.dto.response.PaginatedResponse;

import java.util.List;

public interface AuthorService {
    AuthorResponse createAuthor(AuthorRequest request);
    PaginatedResponse<AuthorResponse> getAllAuthor(int page, int size);
    AuthorResponse getOneAuthor(String id);
    AuthorResponse updateAuthor(String id, AuthorRequest request);
    String deleteAuthor(String id);
    PaginatedResponse<AuthorResponse> getAllAuthorByName(String keyword, int page, int size);
}
