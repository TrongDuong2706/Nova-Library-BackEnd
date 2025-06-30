package com.servicesengineer.identityservicesengineer.service.impl;

import com.servicesengineer.identityservicesengineer.dto.request.GenreRequest;
import com.servicesengineer.identityservicesengineer.dto.response.AuthorResponse;
import com.servicesengineer.identityservicesengineer.dto.response.GenreResponse;
import com.servicesengineer.identityservicesengineer.dto.response.PaginatedResponse;
import com.servicesengineer.identityservicesengineer.entity.Genre;
import com.servicesengineer.identityservicesengineer.exception.AppException;
import com.servicesengineer.identityservicesengineer.exception.ErrorCode;
import com.servicesengineer.identityservicesengineer.mapper.GenreMapper;
import com.servicesengineer.identityservicesengineer.repository.BorrowingRepository;
import com.servicesengineer.identityservicesengineer.repository.GenreRepository;
import com.servicesengineer.identityservicesengineer.service.GenreService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class GenreServiceImpl implements GenreService {


    GenreMapper genreMapper;
    GenreRepository genreRepository;

    @Override
    @PreAuthorize("hasRole('ADMIN')")
    public GenreResponse createGenre(GenreRequest request){
        Genre genre = genreMapper.toGenre(request);
        genreRepository.save(genre);

        return genreMapper.toGenreResponse(genre);
    }
    @Override
    public PaginatedResponse<GenreResponse> getAllGenre(int page, int size){
        PageRequest pageRequest = PageRequest.of(page,size);
        Page<Genre> genres = genreRepository.findAll(pageRequest);
        var genreResponse = genres.getContent().stream().map(genreMapper::toGenreResponse).toList();
        return PaginatedResponse.<GenreResponse>builder()
                .totalItems((int) genres.getTotalElements())
                .currentPage(genres.getNumber())
                .hasNextPage(genres.hasNext())
                .hasPreviousPage(genres.hasPrevious())
                .totalPages(genres.getTotalPages())
                .pageSize(genres.getSize())
                .elements(genreResponse)
                .build();
    }
    @Override
    public GenreResponse getOneGenre(String id){
        Genre genre = genreRepository.findById(id).orElseThrow(() -> new AppException(ErrorCode.GENRE_NOT_EXISTED));
        return genreMapper.toGenreResponse(genre);
    }

    @Override
    @PreAuthorize("hasRole('ADMIN')")
    public GenreResponse updateGenre(String id, GenreRequest request){
        var genre = genreRepository.findById(id).orElseThrow(() -> new AppException(ErrorCode.GENRE_NOT_EXISTED));
        genre.setDescription(request.getDescription());
        genre.setName(request.getName());
        genreRepository.save(genre);
        return genreMapper.toGenreResponse(genre);
    }
    @Override
    @PreAuthorize("hasRole('ADMIN')")
    public String deleteGenre(String id) {
        if (!genreRepository.existsById(id)) {
            throw new AppException(ErrorCode.GENRE_NOT_EXISTED);
        }
        genreRepository.deleteById(id);
        return "Delete Successful";
    }


}
