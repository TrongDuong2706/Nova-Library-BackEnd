package com.servicesengineer.identityservicesengineer.service.impl;

import com.servicesengineer.identityservicesengineer.dto.request.AuthorRequest;
import com.servicesengineer.identityservicesengineer.dto.response.AuthorResponse;
import com.servicesengineer.identityservicesengineer.dto.response.PaginatedResponse;
import com.servicesengineer.identityservicesengineer.entity.Author;
import com.servicesengineer.identityservicesengineer.exception.AppException;
import com.servicesengineer.identityservicesengineer.exception.ErrorCode;
import com.servicesengineer.identityservicesengineer.mapper.AuthorMapper;
import com.servicesengineer.identityservicesengineer.repository.AuthorRepository;
import com.servicesengineer.identityservicesengineer.service.AuthorService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class AuthorServiceImpl implements AuthorService {

    AuthorRepository authorRepository;
    AuthorMapper authorMapper;
    @Override
    public AuthorResponse createAuthor(AuthorRequest request){
        Author author = authorMapper.toAuthor(request);
        authorRepository.save(author);
        return authorMapper.toAuthorResponse(author);
    }
    @Override
    public PaginatedResponse<AuthorResponse> getAllAuthor(int page, int size) {
        PageRequest pageRequest = PageRequest.of(page, size);
        Page<Author> authors = authorRepository.findAll(pageRequest);

        List<AuthorResponse> authorResponses = authors
                .getContent()
                .stream()
                .map(authorMapper::toAuthorResponse)
                .toList();

        return PaginatedResponse.<AuthorResponse>builder()
                .currentPage(authors.getNumber())
                .totalPages(authors.getTotalPages())
                .totalItems((int) authors.getTotalElements())
                .hasNextPage(authors.hasNext())
                .hasPreviousPage(authors.hasPrevious())
                .pageSize(authors.getSize())
                .elements(authorResponses)
                .build();
    }

    @Override
    public AuthorResponse getOneAuthor(String id){
        var author = authorRepository.findById(id).orElseThrow(() -> new AppException(ErrorCode.AUTHOR_NOT_EXISTED));
        return authorMapper.toAuthorResponse(author);
    }
    @Override
    public AuthorResponse updateAuthor(String id, AuthorRequest request){
        var author = authorRepository.findById(id).orElseThrow(() -> new AppException(ErrorCode.AUTHOR_NOT_EXISTED));
        author.setBio(request.getBio());
        author.setName(request.getName());
        authorRepository.save(author);
        return authorMapper.toAuthorResponse(author);
    }
    @Override
    public String deleteAuthor(String id) {
        if (!authorRepository.existsById(id)) {
            throw new AppException(ErrorCode.GENRE_NOT_EXISTED);
        }
        authorRepository.deleteById(id);
        return "Delete Successful";
    }
}
