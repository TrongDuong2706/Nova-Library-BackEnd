package com.servicesengineer.identityservicesengineer.service.impl;

import com.servicesengineer.identityservicesengineer.dto.request.BookRequest;
import com.servicesengineer.identityservicesengineer.dto.response.*;
import com.servicesengineer.identityservicesengineer.entity.Author;
import com.servicesengineer.identityservicesengineer.entity.Book;
import com.servicesengineer.identityservicesengineer.entity.Genre;
import com.servicesengineer.identityservicesengineer.entity.Image;
import com.servicesengineer.identityservicesengineer.exception.AppException;
import com.servicesengineer.identityservicesengineer.exception.ErrorCode;
import com.servicesengineer.identityservicesengineer.mapper.AuthorMapper;
import com.servicesengineer.identityservicesengineer.repository.AuthorRepository;
import com.servicesengineer.identityservicesengineer.repository.BookRepository;
import com.servicesengineer.identityservicesengineer.repository.GenreRepository;
import com.servicesengineer.identityservicesengineer.service.BookService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
@Service
@RequiredArgsConstructor
@Slf4j
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class BookServiceImpl implements BookService {
    BookRepository bookRepository;
    S3StorageService s3StorageService;
    AuthorRepository authorRepository;
    GenreRepository genreRepository;
   // AuthorMapper authorMapper;
    @Override
    @PreAuthorize("hasRole('ADMIN')")
    public BookResponse createBook(BookRequest request, List<MultipartFile> files) {
        // 1. Tìm Author và Genre
        Author author = authorRepository.findById(request.getAuthorId())
                .orElseThrow(() -> new AppException((ErrorCode.AUTHOR_NOT_EXISTED)));

        Genre genre = genreRepository.findById(request.getGenreId())
                .orElseThrow(() -> new AppException((ErrorCode.GENRE_NOT_EXISTED)));

        if(request.getStock() < 0){
            throw new AppException(ErrorCode.BOOK_QUANTITY_SMALLER_THAN_ZERO);
        }

        if (bookRepository.existsByIsbn(request.getIsbn())) {
            throw new AppException(ErrorCode.ISBN_VALIDATE);
        }


        // 2. Tạo Book (chưa gán ảnh)
        Book book = Book.builder()
                .title(request.getTitle())
                .description(request.getDescription())
                .author(author)
                .genre(genre)
                .createdAt(java.time.LocalDate.now())
                .stock(request.getStock())
                .status(request.getStatus())
                .isbn(request.getIsbn())
                .publicationDate(request.getPublicationDate())
                .build();

        // 3. Lưu Book để có ID
        book = bookRepository.save(book);
        // 4. Upload ảnh và tạo danh sách Image
        List<Image> images = new ArrayList<>();
        if (files != null && !files.isEmpty()) {
            for (MultipartFile file : files) {
                try {
                    String imageUrl = s3StorageService.uploadFile(file);
                    Image image = Image.builder()
                            .url(imageUrl)
                            .book(book)
                            .build();
                    images.add(image);
                } catch (IOException e) {
                    log.error("Error uploading image to S3", e);
                    throw new RuntimeException("Upload failed: " + file.getOriginalFilename());
                }
            }
        }

        // 5. Gán danh sách ảnh vào book
        book.setImages(images);

        // 6. Lưu lại book với ảnh
        book = bookRepository.save(book);

        // 7. Tạo response cho ảnh
        List<ImageResponse> imageResponses = images.stream()
                .map(img -> ImageResponse.builder().imageUrl(img.getUrl()).build())
                .toList();
        // 8. Tạo Response cho tác giả
        AuthorResponse authorResponse = AuthorResponse.builder()
                .id(author.getId())
                .bio(author.getBio())
                .name(author.getName())
                .build();
        //9. Tạo Response cho thể loại
        GenreResponse genreResponse = GenreResponse.builder()
                .id(genre.getId())
                .name(genre.getName())
                .description(genre.getDescription())
                .build();

        return BookResponse.builder()
                .id(book.getId())
                .title(book.getTitle())
                .description(book.getDescription())
                .author(authorResponse)
                .genre(genreResponse)
                .createdAt(book.getCreatedAt())
                .images(imageResponses)
                .stock(book.getStock())
                .status(book.getStatus())
                .publicationDate(book.getPublicationDate())
                .isbn(book.getIsbn())
                .build();
    }
//Get All Book
    @Override
    public PaginatedResponse<BookResponse> getAllBook(int page, int size){
        PageRequest pageRequest = PageRequest.of(page,size);
        Page<Book> books = bookRepository.findAll(pageRequest);

        var bookResponse = books.getContent().stream().map(
            book -> {
                List<ImageResponse> imageResponses = book.getImages() != null
                        ? book.getImages().stream()
                        .map(image -> ImageResponse.builder()
                                .imageUrl(image.getUrl())
                                .build()).toList() : List.of();
                //Get Author
                AuthorResponse authorResponse = AuthorResponse.builder()
                        .id(book.getAuthor().getId())
                        .name(book.getAuthor().getName())
                        .bio(book.getAuthor().getBio())
                        .build();
                //Get Genre
                GenreResponse genreResponse = GenreResponse.builder()
                        .id(book.getGenre().getId())
                        .name(book.getGenre().getName())
                        .description(book.getGenre().getDescription())
                        .build();

                return BookResponse.builder()
                        .id(book.getId())
                        .title(book.getTitle())
                        .description(book.getDescription())
                        .author(authorResponse)
                        .genre(genreResponse)
                        .stock(book.getStock())
                        .createdAt(book.getCreatedAt())
                        .images(imageResponses)
                        .status(book.getStatus())
                        .isbn(book.getIsbn())
                        .publicationDate(book.getPublicationDate())
                        .build();
            }

        ).toList();
        return PaginatedResponse.<BookResponse>builder()
                .elements(bookResponse)
                .currentPage(books.getNumber())
                .totalItems((int) books.getTotalElements())
                .totalPages(books.getTotalPages())
                .build();
    }

    @Override
    public BookResponse getOneBook(String id) {
        Book book = bookRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.BOOK_NOT_EXISTED));

        List<ImageResponse> imageResponses = book.getImages().stream()
                .map(image -> ImageResponse.builder()
                        .imageUrl(image.getUrl()) // sửa `getUrl()` thành `getImageUrl()` hoặc đúng field
                        .build())
                .toList();
        //Tạo AuthorResponse
        AuthorResponse authorResponse = AuthorResponse.builder()
                .id(book.getAuthor().getId())
                .bio(book.getAuthor().getBio())
                .name(book.getAuthor().getName())
                .build();
        //Tạo GenreResponse
        GenreResponse genreResponse = GenreResponse.builder()
                .id(book.getGenre().getId())
                .name(book.getGenre().getName())
                .description(book.getGenre().getDescription())
                .build();

        return BookResponse.builder()
                .id(book.getId())
                .title(book.getTitle())
                .stock(book.getStock())
                .status(book.getStatus())
                .description(book.getDescription())
                .author(authorResponse)
                .genre(genreResponse)
                .createdAt(book.getCreatedAt())
                .images(imageResponses)
                .isbn(book.getIsbn())
                .publicationDate(book.getPublicationDate())
                .build();
    }

    //Update Book function
    @Override
    @PreAuthorize("hasRole('ADMIN')")
    public BookResponse updateBook(String bookId, BookRequest request, List<MultipartFile> files) {
        // 1. Lấy Book
        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> new AppException(ErrorCode.BOOK_NOT_EXISTED));

        // 2. Lấy Author và Genre mới nếu cần
        Author author = authorRepository.findById(request.getAuthorId())
                .orElseThrow(() -> new AppException(ErrorCode.AUTHOR_NOT_EXISTED));
        Genre genre = genreRepository.findById(request.getGenreId())
                .orElseThrow(() -> new AppException(ErrorCode.GENRE_NOT_EXISTED));

        if(request.getStock() < 0){
            throw new AppException(ErrorCode.BOOK_QUANTITY_SMALLER_THAN_ZERO);
        }

        // Kiểm tra ISBN trùng lặp (trừ chính nó)
        bookRepository.findByIsbn(request.getIsbn()).ifPresent(existingBook -> {
            if (!existingBook.getId().equals(bookId)) {
                throw new AppException(ErrorCode.ISBN_VALIDATE);
            }
        });

        // 3. Cập nhật thông tin cơ bản
        book.setTitle(request.getTitle());
        book.setDescription(request.getDescription());
        book.setAuthor(author);
        book.setGenre(genre);
        book.setStock(request.getStock());
        book.setStatus(request.getStatus());
        book.setIsbn(request.getIsbn());
        book.setPublicationDate(request.getPublicationDate());

        // 4. Nếu có ảnh mới => Xóa ảnh cũ
        if (files != null && files.stream().anyMatch(file -> !file.isEmpty())) {
            // 🔥 Thực sự có ảnh mới ⇒ mới xóa ảnh cũ
            List<Image> oldImages = book.getImages();
            if (oldImages != null) {
                for (Image img : oldImages) {
                    try {
                        s3StorageService.deleteFile(img.getUrl());
                    } catch (Exception e) {
                        log.error("Error deleting image from S3: {}", img.getUrl(), e);
                    }
                }
                oldImages.clear();
            }

            // Upload ảnh mới
            List<Image> newImages = new ArrayList<>();
            for (MultipartFile file : files) {
                if (!file.isEmpty()) {
                    try {
                        String imageUrl = s3StorageService.uploadFile(file);
                        Image image = Image.builder()
                                .url(imageUrl)
                                .book(book)
                                .build();
                        newImages.add(image);
                    } catch (IOException e) {
                        log.error("Error uploading image to S3", e);
                        throw new RuntimeException("Upload failed: " + file.getOriginalFilename());
                    }
                }
            }
            book.getImages().addAll(newImages);
        }

        // 6. Lưu lại Book
        book = bookRepository.save(book);

        // 7. Trả về BookResponse
        List<ImageResponse> imageResponses = book.getImages().stream()
                .map(img -> ImageResponse.builder().imageUrl(img.getUrl()).build())
                .toList();
        // 8. Tạo Response cho tác giả
        AuthorResponse authorResponse = AuthorResponse.builder()
                .id(author.getId())
                .bio(author.getBio())
                .name(author.getName())
                .build();
        //9. Tạo Response cho thể loại
        GenreResponse genreResponse = GenreResponse.builder()
                .id(genre.getId())
                .name(genre.getName())
                .description(genre.getDescription())
                .build();

        return BookResponse.builder()
                .id(book.getId())
                .title(book.getTitle())
                .description(book.getDescription())
                .status(book.getStatus())
                .author(authorResponse)
                .genre(genreResponse)
                .createdAt(book.getCreatedAt())
                .stock(book.getStock())
                .images(imageResponses)
                .publicationDate(book.getPublicationDate())
                .isbn(book.getIsbn())
                .build();
    }

    @Override
    public PaginatedResponse<BookResponse> getAllBookWithFilter(String authorName, String genreName, String title, String description , int page, int size){
        PageRequest pageRequest = PageRequest.of(page, size);
        Page<Book> books = bookRepository.findByAuthorNameAndGenreNameAndTitleAndDescription(authorName, genreName, title, description, pageRequest);
        var bookResponse = books.getContent().stream().map(
                book -> {
                    List<ImageResponse> imageResponses = book.getImages() != null
                            ? book.getImages().stream()
                            .map(image -> ImageResponse.builder()
                                    .imageUrl(image.getUrl())
                                    .build()).toList() : List.of();
                    //Get Author
                    AuthorResponse authorResponse = AuthorResponse.builder()
                            .id(book.getAuthor().getId())
                            .name(book.getAuthor().getName())
                            .bio(book.getAuthor().getBio())
                            .build();
                    //Get Genre
                    GenreResponse genreResponse = GenreResponse.builder()
                            .id(book.getGenre().getId())
                            .name(book.getGenre().getName())
                            .description(book.getGenre().getDescription())
                            .build();

                    return BookResponse.builder()
                            .id(book.getId())
                            .title(book.getTitle())
                            .description(book.getDescription())
                            .author(authorResponse)
                            .genre(genreResponse)
                            .status(book.getStatus())
                            .createdAt(book.getCreatedAt())
                            .stock(book.getStock())
                            .images(imageResponses)
                            .isbn(book.getIsbn())
                            .publicationDate(book.getPublicationDate())
                            .build();
                }

        ).toList();
        return PaginatedResponse.<BookResponse>builder()
                .elements(bookResponse)
                .currentPage(books.getNumber())
                .totalItems((int) books.getTotalElements())
                .totalPages(books.getTotalPages())
                .build();
    }
    @Override
    @PreAuthorize("hasRole('ADMIN')")
    public void softDelete(String id){
        Book book = bookRepository.findById(id).orElseThrow(() -> new AppException(ErrorCode.BOOK_NOT_EXISTED));
        book.setStatus(0);
        bookRepository.save(book);
    }
    @Override
    public long countActiveBooks() {
        return bookRepository.countBooks();
    }

    @Override
    public PaginatedResponse<BookResponse> getAllBookWithAdminFilter(String authorName, String genreName, String title, Integer status, String isbn, int page, int size){
        PageRequest pageRequest = PageRequest.of(page, size);
        Page<Book> books = bookRepository.findByFilters(authorName, genreName, title, status,isbn ,pageRequest);
        var bookResponse = books.getContent().stream().map(
                book -> {
                    List<ImageResponse> imageResponses = book.getImages() != null
                            ? book.getImages().stream()
                            .map(image -> ImageResponse.builder()
                                    .imageUrl(image.getUrl())
                                    .build()).toList() : List.of();
                    //Get Author
                    AuthorResponse authorResponse = AuthorResponse.builder()
                            .id(book.getAuthor().getId())
                            .name(book.getAuthor().getName())
                            .bio(book.getAuthor().getBio())
                            .build();
                    //Get Genre
                    GenreResponse genreResponse = GenreResponse.builder()
                            .id(book.getGenre().getId())
                            .name(book.getGenre().getName())
                            .description(book.getGenre().getDescription())
                            .build();

                    return BookResponse.builder()
                            .id(book.getId())
                            .title(book.getTitle())
                            .description(book.getDescription())
                            .author(authorResponse)
                            .genre(genreResponse)
                            .status(book.getStatus())
                            .createdAt(book.getCreatedAt())
                            .stock(book.getStock())
                            .images(imageResponses)
                            .isbn(book.getIsbn())
                            .publicationDate(book.getPublicationDate())
                            .build();
                }

        ).toList();
        return PaginatedResponse.<BookResponse>builder()
                .elements(bookResponse)
                .currentPage(books.getNumber())
                .totalItems((int) books.getTotalElements())
                .totalPages(books.getTotalPages())
                .build();
    }

    @Override
    public PaginatedResponse<BookResponse> getAllBookZeroStock(int page, int size){
        PageRequest pageRequest = PageRequest.of(page,size);
        Page<Book> books = bookRepository.findByStock(0, pageRequest);
        var bookResponse = books.getContent().stream().map(
                book -> {
                    List<ImageResponse> imageResponses = book.getImages() != null
                            ? book.getImages().stream()
                            .map(image -> ImageResponse.builder()
                                    .imageUrl(image.getUrl())
                                    .build()).toList() : List.of();
                    //Get Author
                    AuthorResponse authorResponse = AuthorResponse.builder()
                            .id(book.getAuthor().getId())
                            .name(book.getAuthor().getName())
                            .bio(book.getAuthor().getBio())
                            .build();
                    //Get Genre
                    GenreResponse genreResponse = GenreResponse.builder()
                            .id(book.getGenre().getId())
                            .name(book.getGenre().getName())
                            .description(book.getGenre().getDescription())
                            .build();

                    return BookResponse.builder()
                            .id(book.getId())
                            .title(book.getTitle())
                            .description(book.getDescription())
                            .author(authorResponse)
                            .genre(genreResponse)
                            .stock(book.getStock())
                            .createdAt(book.getCreatedAt())
                            .images(imageResponses)
                            .status(book.getStatus())
                            .isbn(book.getIsbn())
                            .publicationDate(book.getPublicationDate())
                            .build();
                }

        ).toList();
        return PaginatedResponse.<BookResponse>builder()
                .elements(bookResponse)
                .currentPage(books.getNumber())
                .totalItems((int) books.getTotalElements())
                .totalPages(books.getTotalPages())
                .build();
    }

    @Override
    public PaginatedResponse<BookResponse> getAllBookWithGenre(String genreName, int page, int size) {
        PageRequest pageRequest = PageRequest.of(page, size);
        Page<Book> books = bookRepository.findBooksByGenreName(genreName, pageRequest);

        var bookResponse = books.getContent().stream().map(
                book -> {
                    List<ImageResponse> imageResponses = book.getImages() != null
                            ? book.getImages().stream()
                            .map(image -> ImageResponse.builder()
                                    .imageUrl(image.getUrl())
                                    .build())
                            .toList()
                            : List.of();

                    AuthorResponse authorResponse = AuthorResponse.builder()
                            .id(book.getAuthor().getId())
                            .name(book.getAuthor().getName())
                            .bio(book.getAuthor().getBio())
                            .build();

                    GenreResponse genreResponse = GenreResponse.builder()
                            .id(book.getGenre().getId())
                            .name(book.getGenre().getName())
                            .description(book.getGenre().getDescription())
                            .build();

                    return BookResponse.builder()
                            .id(book.getId())
                            .title(book.getTitle())
                            .description(book.getDescription())
                            .author(authorResponse)
                            .genre(genreResponse)
                            .stock(book.getStock())
                            .createdAt(book.getCreatedAt())
                            .images(imageResponses)
                            .status(book.getStatus())
                            .isbn(book.getIsbn())
                            .publicationDate(book.getPublicationDate())
                            .build();
                }
        ).toList();

        return PaginatedResponse.<BookResponse>builder()
                .elements(bookResponse)
                .currentPage(books.getNumber())
                .totalItems((int) books.getTotalElements())
                .totalPages(books.getTotalPages())
                .build();
    }


}
