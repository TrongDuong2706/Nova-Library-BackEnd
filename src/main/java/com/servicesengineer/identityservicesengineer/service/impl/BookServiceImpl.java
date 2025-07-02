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
import jakarta.transaction.Transactional;
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
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class BookServiceImpl implements BookService {
    BookRepository bookRepository;
    S3StorageService s3StorageService;
    AuthorRepository authorRepository;
    GenreRepository genreRepository;

    @Override
    @PreAuthorize("hasRole('ADMIN')")
    @Transactional // Đảm bảo tất cả các thao tác đều thành công hoặc rollback
    public BookResponse createBook(BookRequest request, List<MultipartFile> files) {
        if (bookRepository.existsByIsbn(request.getIsbn())) {
            throw new AppException(ErrorCode.ISBN_VALIDATE);
        }

        if(request.getStock() < 0){
            throw new AppException(ErrorCode.BOOK_QUANTITY_SMALLER_THAN_ZERO);
        }

        // 1. Tìm tập hợp các Author và Genre từ IDs
        Set<Author> authors = new HashSet<>(authorRepository.findAllById(request.getAuthorIds()));
        if (authors.size() != request.getAuthorIds().size()) {
            throw new AppException(ErrorCode.AUTHOR_NOT_EXISTED); // Một hoặc nhiều ID tác giả không tồn tại
        }

        Set<Genre> genres = new HashSet<>(genreRepository.findAllById(request.getGenreIds()));
        if (genres.size() != request.getGenreIds().size()) {
            throw new AppException(ErrorCode.GENRE_NOT_EXISTED); // Một hoặc nhiều ID thể loại không tồn tại
        }

        // 2. Tạo Book
        Book book = Book.builder()
                .title(request.getTitle())
                .description(request.getDescription())
                .authors(authors) // Gán tập hợp tác giả
                .genres(genres)   // Gán tập hợp thể loại
                .createdAt(java.time.LocalDate.now())
                .stock(request.getStock())
                .status(request.getStatus())
                .isbn(request.getIsbn())
                .publicationDate(request.getPublicationDate())
                .build();

        // 3. Lưu Book để có ID (quan trọng cho quan hệ với Image)
        book = bookRepository.save(book);

        // 4. Upload ảnh và gán vào sách
        if (files != null && !files.isEmpty()) {
            List<Image> images = new ArrayList<>();
            for (MultipartFile file : files) {
                try {
                    String imageUrl = s3StorageService.uploadFile(file);
                    Image image = Image.builder()
                            .url(imageUrl)
                            .book(book) // Gán sách vừa được lưu
                            .build();
                    images.add(image);
                } catch (IOException e) {
                    log.error("Error uploading image to S3", e);
                    // Cân nhắc xóa book đã tạo hoặc có cơ chế xử lý lỗi upload
                    throw new RuntimeException("Upload failed: " + file.getOriginalFilename());
                }
            }
            book.setImages(images);
            book = bookRepository.save(book); // Lưu lại sách với thông tin ảnh
        }

        return convertToBookResponse(book);
    }

    @Override
    @Transactional // Dùng transactional để đảm bảo các lazy-loading hoạt động đúng
    public BookResponse updateBook(String bookId, BookRequest request, List<MultipartFile> files) {
        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> new AppException(ErrorCode.BOOK_NOT_EXISTED));

        // Kiểm tra ISBN trùng lặp (trừ chính nó)
        bookRepository.findByIsbn(request.getIsbn()).ifPresent(existingBook -> {
            if (!existingBook.getId().equals(bookId)) {
                throw new AppException(ErrorCode.ISBN_VALIDATE);
            }
        });

        if(request.getStock() < 0){
            throw new AppException(ErrorCode.BOOK_QUANTITY_SMALLER_THAN_ZERO);
        }

        // 1. Tìm tập hợp Author và Genre mới
        Set<Author> authors = new HashSet<>(authorRepository.findAllById(request.getAuthorIds()));
        if (authors.size() != request.getAuthorIds().size()) {
            throw new AppException(ErrorCode.AUTHOR_NOT_EXISTED);
        }

        Set<Genre> genres = new HashSet<>(genreRepository.findAllById(request.getGenreIds()));
        if (genres.size() != request.getGenreIds().size()) {
            throw new AppException(ErrorCode.GENRE_NOT_EXISTED);
        }

        // 2. Cập nhật thông tin
        book.setTitle(request.getTitle());
        book.setDescription(request.getDescription());
        book.setAuthors(authors);
        book.setGenres(genres);
        book.setStock(request.getStock());
        book.setStatus(request.getStatus());
        book.setIsbn(request.getIsbn());
        book.setPublicationDate(request.getPublicationDate());

        // 3. Xử lý ảnh (nếu có file mới)
        if (files != null && files.stream().anyMatch(file -> !file.isEmpty())) {
            // Xóa ảnh cũ trên S3 và trong DB
            if (book.getImages() != null) {
                book.getImages().forEach(img -> {
                    try {
                        s3StorageService.deleteFile(img.getUrl());
                    } catch (Exception e) {
                        log.error("Error deleting old image from S3: {}", img.getUrl(), e);
                    }
                });
                book.getImages().clear(); // Xóa khỏi collection
            }

            // Upload ảnh mới
            List<Image> newImages = new ArrayList<>();
            for (MultipartFile file : files) {
                if (!file.isEmpty()) {
                    try {
                        String imageUrl = s3StorageService.uploadFile(file);
                        newImages.add(Image.builder().url(imageUrl).book(book).build());
                    } catch (IOException e) {
                        log.error("Error uploading new image to S3", e);
                        throw new RuntimeException("Upload failed: " + file.getOriginalFilename());
                    }
                }
            }
            book.setImages(newImages);
        }

        Book updatedBook = bookRepository.save(book);
        return convertToBookResponse(updatedBook);
    }

    // --- CÁC PHƯƠNG THỨC GET ĐƯỢC ĐƠN GIẢN HÓA ---

    @Override
    public PaginatedResponse<BookResponse> getAllBook(int page, int size) {
        Page<Book> booksPage = bookRepository.findAll(PageRequest.of(page, size));
        return createPaginatedResponse(booksPage);
    }

    @Override
    public BookResponse getOneBook(String id) {
        Book book = bookRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.BOOK_NOT_EXISTED));
        return convertToBookResponse(book);
    }

    @Override
    public PaginatedResponse<BookResponse> getAllBookWithFilter(String authorName, String genreName, String keyword, int page, int size) {
        Page<Book> booksPage = bookRepository.findByAuthorNameAndGenreNameAndTitleAndDescription(authorName, genreName, keyword, PageRequest.of(page, size));
        return createPaginatedResponse(booksPage);
    }

    @Override
    public PaginatedResponse<BookResponse> getAllBookWithAdminFilter(String authorName, String genreName, String keyword, Integer status, String isbn, int page, int size) {
        Page<Book> booksPage = bookRepository.findByFilters(authorName, genreName, keyword, status, isbn, PageRequest.of(page, size));
        return createPaginatedResponse(booksPage);
    }

    @Override
    public PaginatedResponse<BookResponse> getAllBookZeroStock(int page, int size) {
        Page<Book> booksPage = bookRepository.findByStock(0, PageRequest.of(page, size));
        return createPaginatedResponse(booksPage);
    }


    @Override
    public PaginatedResponse<BookResponse> getAllBookWithGenre(String genreName, int page, int size) {
        Page<Book> booksPage = bookRepository.findBooksByGenreName(genreName, PageRequest.of(page, size));
        return createPaginatedResponse(booksPage);
    }

    @Override
    public PaginatedResponse<BookResponse> getAllBookByTitle(String title, int page, int size) {
        Page<Book> booksPage = bookRepository.findByTitleContainingIgnoreCase(title, PageRequest.of(page, size));
        return createPaginatedResponse(booksPage);
    }

    // --- CÁC PHƯƠNG THỨC KHÁC KHÔNG THAY ĐỔI NHIỀU ---

    @Override
    @PreAuthorize("hasRole('ADMIN')")
    public void softDelete(String id) {
        Book book = bookRepository.findById(id).orElseThrow(() -> new AppException(ErrorCode.BOOK_NOT_EXISTED));
        book.setStatus(0); // 0 là trạng thái không hoạt động
        bookRepository.save(book);
    }

    @Override
    public long countActiveBooks() {
        return bookRepository.countBooks(); // Cần đảm bảo query này đúng ý đồ (ví dụ: chỉ đếm sách có status = 1)
    }

    // --- HELPER METHODS (PHƯƠNG THỨC HỖ TRỢ) ---

    /**
     * Phương thức private để chuyển đổi một đối tượng Page<Book> thành PaginatedResponse<BookResponse>.
     * Giúp tái sử dụng code và làm sạch các phương thức public.
     */
    private PaginatedResponse<BookResponse> createPaginatedResponse(Page<Book> booksPage) {
        List<BookResponse> bookResponses = booksPage.getContent().stream()
                .map(this::convertToBookResponse)
                .toList();

        return PaginatedResponse.<BookResponse>builder()
                .elements(bookResponses)
                .currentPage(booksPage.getNumber())
                .totalItems((int) booksPage.getTotalElements())
                .totalPages(booksPage.getTotalPages())
                .build();
    }

    /**
     * Phương thức private để chuyển đổi một thực thể Book sang BookResponse.
     * Đây là nơi tập trung logic mapping, giúp các phương thức khác gọn gàng hơn.
     */
    private BookResponse convertToBookResponse(Book book) {
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

        return BookResponse.builder()
                .id(book.getId())
                .title(book.getTitle())
                .description(book.getDescription())
                .authors(authorResponses) // Dùng 'authors' (số nhiều)
                .genres(genreResponses)   // Dùng 'genres' (số nhiều)
                .stock(book.getStock())
                .status(book.getStatus())
                .createdAt(book.getCreatedAt())
                .images(imageResponses)
                .isbn(book.getIsbn())
                .publicationDate(book.getPublicationDate())
                .build();
    }


}
