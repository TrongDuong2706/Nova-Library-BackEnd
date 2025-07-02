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
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class FavoriteBookServiceImpl implements FavoriteBookService {
    FavoriteBookRepository favoriteBookRepository;
    UserRepository userRepository;
    BookRepository bookRepository;

    @Override
    public SimpleBookResponse addFavorite(String bookId) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));
        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> new AppException(ErrorCode.BOOK_NOT_EXISTED));

        if (favoriteBookRepository.existsByUserAndBook(user, book)) {
            throw new AppException(ErrorCode.ALREADY_FAVORITED);
        }

        FavoriteBook favorite = FavoriteBook.builder()
                .user(user)
                .book(book)
                .build();
        favoriteBookRepository.save(favorite);

        // Gọi helper method để tạo response, giúp code gọn gàng hơn
        return convertToSimpleBookResponse(favorite);
    }

    @Override
    public PaginatedResponse<SimpleBookResponse> getAllFavorite(int page, int size) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));

        PageRequest pageRequest = PageRequest.of(page, size);
        Page<FavoriteBook> favoriteBooksPage = favoriteBookRepository.findByUser(user, pageRequest);

        List<SimpleBookResponse> content = favoriteBooksPage.getContent().stream()
                .map(this::convertToSimpleBookResponse) // Sử dụng method reference cho gọn
                .toList();

        return PaginatedResponse.<SimpleBookResponse>builder()
                .elements(content)
                .currentPage(favoriteBooksPage.getNumber())
                .pageSize(favoriteBooksPage.getSize())
                .totalItems((int) favoriteBooksPage.getTotalElements())
                .totalPages(favoriteBooksPage.getTotalPages())
                .build();
    }

    @Override
    public void removeFavorite(String bookId) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));

        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> new AppException(ErrorCode.BOOK_NOT_EXISTED));

        // Tìm FavoriteBook
        FavoriteBook favoriteBook = favoriteBookRepository.findByUserAndBook(user, book);

        // Kiểm tra xem nó có null hay không
        if (favoriteBook == null) {
            // Nếu không tìm thấy, ném ra ngoại lệ
            throw new AppException(ErrorCode.FAVORITE_NOT_FOUND);
        }

        // Nếu tìm thấy, tiến hành xóa
        favoriteBookRepository.delete(favoriteBook);
    }

    // --- HELPER METHOD ---
    /**
     * Chuyển đổi một thực thể FavoriteBook sang SimpleBookResponse.
     * Đây là nơi tập trung logic mapping, giúp các phương thức khác gọn gàng hơn.
     */
    private SimpleBookResponse convertToSimpleBookResponse(FavoriteBook favoriteBook) {
        Book book = favoriteBook.getBook();

        // Chuyển đổi Set<Author> thành Set<AuthorResponse>
        Set<AuthorResponse> authorResponses = book.getAuthors().stream()
                .map(author -> AuthorResponse.builder()
                        .id(author.getId())
                        .name(author.getName())
                        .bio(author.getBio())
                        .build())
                .collect(Collectors.toSet());

        // Chuyển đổi Set<Genre> thành Set<GenreResponse>
        Set<GenreResponse> genreResponses = book.getGenres().stream()
                .map(genre -> GenreResponse.builder()
                        .id(genre.getId())
                        .name(genre.getName())
                        .description(genre.getDescription())
                        .build())
                .collect(Collectors.toSet());

        // Chuyển đổi List<Image> thành List<ImageResponse>
        List<ImageResponse> imageResponses = book.getImages() != null ?
                book.getImages().stream()
                        .map(image -> ImageResponse.builder().imageUrl(image.getUrl()).build())
                        .toList() : List.of();

        return SimpleBookResponse.builder()
                .favoriteId(favoriteBook.getId())
                .bookId(book.getId())
                .title(book.getTitle())
                .description(book.getDescription())
                .authors(authorResponses) // Gán tập hợp tác giả
                .genres(genreResponses)   // Gán tập hợp thể loại
                .images(imageResponses)
                .build();
    }
}