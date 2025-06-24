package com.servicesengineer.identityservicesengineer.service.impl;

import com.servicesengineer.identityservicesengineer.dto.response.*;
import com.servicesengineer.identityservicesengineer.entity.Book;
import com.servicesengineer.identityservicesengineer.entity.FavoriteBook;
import com.servicesengineer.identityservicesengineer.entity.User;
import com.servicesengineer.identityservicesengineer.exception.AppException;
import com.servicesengineer.identityservicesengineer.exception.ErrorCode;
import com.servicesengineer.identityservicesengineer.repository.BookRepository;
import com.servicesengineer.identityservicesengineer.repository.FavoriteBookRepository;
import com.servicesengineer.identityservicesengineer.repository.UserRepository;
import com.servicesengineer.identityservicesengineer.service.FavoriteBookService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class FavoriteBookServiceImpl implements FavoriteBookService {
    FavoriteBookRepository favoriteBookRepository;
    UserRepository userRepository;
    BookRepository bookRepository;
    @Override
    public SimpleBookResponse addFavorite(String bookId){
        var context = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByUsername(context)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));
        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> new AppException(ErrorCode.BOOK_NOT_EXISTED));

        if (favoriteBookRepository.existsByUserAndBook(user, book)) {
            throw new AppException(ErrorCode.ALREADY_FAVORITED);
        }

        AuthorResponse authorResponse = AuthorResponse.builder()
                .id(book.getAuthor().getId())
                .bio(book.getAuthor().getBio())
                .name(book.getAuthor().getName())
                .build();
        GenreResponse genreResponse = GenreResponse.builder()
                .id(book.getGenre().getId())
                .name(book.getGenre().getName())
                .description(book.getGenre().getDescription())
                .build();

        FavoriteBook favorite = FavoriteBook.builder()
                .user(user)
                .book(book)
                .build();
        favoriteBookRepository.save(favorite);
        List<ImageResponse> imageResponses = book.getImages().stream()
                .map(img -> ImageResponse.builder().imageUrl(img.getUrl()).build())
                .toList();
        return SimpleBookResponse.builder()
                .favoriteId(favorite.getId())
                .bookId(bookId)
                .title(book.getTitle())
                .author(authorResponse)
                .genre(genreResponse)
                .description(book.getDescription())
                .images(imageResponses)
                .build();
    }
    @Override
    public PaginatedResponse<SimpleBookResponse> getAllFavorite(int page, int size) {
        var username = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));

        PageRequest pageRequest = PageRequest.of(page, size);
        Page<FavoriteBook> favoriteBooks = favoriteBookRepository.findByUser(user, pageRequest);

        List<SimpleBookResponse> content = favoriteBooks.getContent().stream().map(fav -> {
            Book book = fav.getBook();
            return SimpleBookResponse.builder()
                    .favoriteId(fav.getId())
                    .bookId(book.getId())
                    .title(book.getTitle())
                    .description(book.getDescription())
                    .author(AuthorResponse.builder()
                            .id(book.getAuthor().getId())
                            .name(book.getAuthor().getName())
                            .bio(book.getAuthor().getBio())
                            .build())
                    .genre(GenreResponse.builder()
                            .id(book.getGenre().getId())
                            .name(book.getGenre().getName())
                            .build())
                    .images(book.getImages().stream().map(img ->
                            ImageResponse.builder().imageUrl(img.getUrl()).build()
                    ).toList())
                    .build();
        }).toList();

        return PaginatedResponse.<SimpleBookResponse>builder()
                .elements(content)
                .currentPage(favoriteBooks.getNumber())
                .pageSize(favoriteBooks.getSize())
                .totalItems((int) favoriteBooks.getTotalElements())
                .totalPages(favoriteBooks.getTotalPages())
                .build();
    }

    @Override
    public void removeFavorite(String bookId) {
        var username = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));

        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> new AppException(ErrorCode.BOOK_NOT_EXISTED));

        FavoriteBook favoriteBook = favoriteBookRepository.findByUserAndBook(user, book);
        favoriteBookRepository.delete(favoriteBook);
    }



}
