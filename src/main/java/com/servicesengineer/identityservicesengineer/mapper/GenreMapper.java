package com.servicesengineer.identityservicesengineer.mapper;

import com.servicesengineer.identityservicesengineer.dto.request.GenreRequest;
import com.servicesengineer.identityservicesengineer.dto.response.GenreResponse;
import com.servicesengineer.identityservicesengineer.entity.Genre;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface GenreMapper {
    Genre toGenre(GenreRequest request);
    GenreResponse toGenreResponse(Genre genre);
}
