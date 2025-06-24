package com.servicesengineer.identityservicesengineer.mapper;

import com.servicesengineer.identityservicesengineer.dto.request.AuthorRequest;
import com.servicesengineer.identityservicesengineer.dto.response.AuthorResponse;
import com.servicesengineer.identityservicesengineer.entity.Author;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface AuthorMapper {
    Author toAuthor(AuthorRequest request);
    AuthorResponse toAuthorResponse(Author author);
}
