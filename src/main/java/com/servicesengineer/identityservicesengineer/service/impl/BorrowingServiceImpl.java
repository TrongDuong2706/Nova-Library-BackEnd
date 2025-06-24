package com.servicesengineer.identityservicesengineer.service.impl;

import com.servicesengineer.identityservicesengineer.dto.request.BorrowingRequest;
import com.servicesengineer.identityservicesengineer.dto.response.*;
import com.servicesengineer.identityservicesengineer.entity.*;
import com.servicesengineer.identityservicesengineer.exception.AppException;
import com.servicesengineer.identityservicesengineer.exception.ErrorCode;
import com.servicesengineer.identityservicesengineer.repository.BookRepository;
import com.servicesengineer.identityservicesengineer.repository.BorrowingRepository;
import com.servicesengineer.identityservicesengineer.repository.FavoriteBookRepository;
import com.servicesengineer.identityservicesengineer.repository.UserRepository;
import com.servicesengineer.identityservicesengineer.service.BorrowingService;
import jakarta.transaction.Transactional;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
@Slf4j
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class BorrowingServiceImpl implements BorrowingService {
    UserRepository userRepository;
    BookRepository bookRepository;
    BorrowingRepository borrowingRepository;
    FavoriteBookRepository favoriteBookRepository;

    @Override
    public BorrowingResponse createBorrowing(BorrowingRequest request) {
        // 1. Lấy User
        User user = userRepository.findByStudentCode(request.getStudentCode())
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));

        boolean hasUnreturnedBorrow = borrowingRepository.existsByUserAndStatusIn(
                user,
                List.of(BorrowingStatus.BORROWED, BorrowingStatus.OVERDUE)
        );

        if (hasUnreturnedBorrow) {
            throw new AppException(ErrorCode.USER_HAS_UNRETURNED_BORROWING);
        }

        // 2. Tạo phiếu mượn mới
        Borrowing borrowing = Borrowing.builder()
                .borrowDate(LocalDate.now())
                .dueDate(request.getDueDate())
                .user(user)
                .status(BorrowingStatus.BORROWED)
                .build();

        // 3. Tạo danh sách bản ghi BorrowingRecordItem
        List<BorrowingRecordItem> items = new ArrayList<>();
        for (String bookId : request.getBookIds()) {
            Book book = bookRepository.findById(bookId)
                    .orElseThrow(() -> new AppException(ErrorCode.BOOK_NOT_EXISTED));

            if (Objects.isNull(book.getStock()) || book.getStock() <= 0) {
                throw new AppException(ErrorCode.BOOK_OUT_OF_STOCK);
            }

            // Trừ stock
            book.setStock(book.getStock() - 1);
            bookRepository.save(book); // Lưu thay đổi stock

            BorrowingRecordItem item = BorrowingRecordItem.builder()
                    .book(book)
                    .borrowing(borrowing)
                    .build();

            items.add(item);
            FavoriteBook favoriteBook = favoriteBookRepository.findByUserAndBook(user, book);
            if (favoriteBook != null) {
                favoriteBookRepository.delete(favoriteBook);
            }
        }

        borrowing.setItems(items); // Gán danh sách item vào phiếu mượn


        // 4. Lưu vào DB
        borrowing = borrowingRepository.save(borrowing);

        // 5. Tạo danh sách BookResponse từ các item
        List<BookResponse> bookResponses = borrowing.getItems().stream()
                .map(item -> {
                    Book book = item.getBook();
                    List<ImageResponse> images = book.getImages() != null
                            ? book.getImages().stream()
                            .map(img -> ImageResponse.builder().imageUrl(img.getUrl()).build())
                            .toList() : List.of();
                    AuthorResponse authorResponse = AuthorResponse.builder()
                            .id(item.getBook().getAuthor().getId())
                            .name(item.getBook().getAuthor().getName())
                            .bio(item.getBook().getAuthor().getBio())
                            .build();
                    GenreResponse genreResponse = GenreResponse.builder()
                            .id(item.getBook().getGenre().getId())
                            .name(item.getBook().getGenre().getName())
                            .description(item.getBook().getGenre().getDescription())
                            .build();

                    return BookResponse.builder()
                            .id(book.getId())
                            .title(book.getTitle())
                            .description(book.getDescription())
                            .author(authorResponse)
                            .genre(genreResponse)
                            .createdAt(book.getCreatedAt())
                            .images(images)
                            .publicationDate(book.getPublicationDate())
                            .isbn(book.getIsbn())
                            .build();
                })
                .toList();
        //Tạo UserResponse
        UserResponse userResponse = UserResponse.builder()
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .studentCode(user.getStudentCode())
                .build();

        // 6. Trả về kết quả
        return BorrowingResponse.builder()
                .id(borrowing.getId())
                .borrowDate(borrowing.getBorrowDate())
                .dueDate(borrowing.getDueDate())
                .status(borrowing.getStatus())
                .returnDate(borrowing.getReturnDate())
                .finalAmount(borrowing.getFinalAmount())
                .userResponse(userResponse)
                .books(bookResponses)
                .build();
    }

    //Hàm trả sách
    @Override
    @Transactional
    public BorrowingResponse returnBorrow(String borrowId){
        Borrowing borrowing = borrowingRepository.findById(borrowId).orElseThrow(() -> new AppException(ErrorCode.BORROW_NOT_EXISTED));
        // 2. Nếu đã trả rồi thì không cho trả lại nữa
        if (borrowing.getStatus() == BorrowingStatus.RETURNED) {
            throw new AppException(ErrorCode.BORROWING_ALREADY_RETURNED);
        }
        //Gán ngày trả là hôm nay
        LocalDate today = LocalDate.now();
        borrowing.setReturnDate(today);
        if (today.isAfter(borrowing.getDueDate())) {
            long daysLate = ChronoUnit.DAYS.between(borrowing.getDueDate(), today);
            double finePerDay = 30000.0;
            borrowing.setFinalAmount(daysLate * finePerDay);
        } else {
            borrowing.setFinalAmount(0.0);
        }
        // ✅ 5. Cộng lại stock cho từng sách
        for (BorrowingRecordItem item : borrowing.getItems()) {
            Book book = item.getBook();
            Integer currentStock = book.getStock();
            if (currentStock == null) {
                currentStock = 0;
            }
            book.setStock(currentStock + 1);
            bookRepository.save(book); // Cập nhật stock
        }
        // 5. Cập nhật trạng thái
        borrowing.setStatus(BorrowingStatus.RETURNED);
        // 6. Lưu lại
        borrowing = borrowingRepository.save(borrowing);

        List<BookResponse> bookResponses = borrowing.getItems().stream().map(
                item -> {
                    Book book = item.getBook();
                    List<ImageResponse> images = book.getImages().stream().map(
                            img -> ImageResponse.builder()
                                    .imageUrl(img.getUrl())
                                    .build()

                    ).toList();
                    AuthorResponse authorResponse = AuthorResponse.builder()
                            .id(item.getBook().getAuthor().getId())
                            .name(item.getBook().getAuthor().getName())
                            .bio(item.getBook().getAuthor().getBio())
                            .build();
                    GenreResponse genreResponse = GenreResponse.builder()
                            .id(item.getBook().getGenre().getId())
                            .name(item.getBook().getGenre().getName())
                            .description(item.getBook().getGenre().getDescription())
                            .build();


                    return BookResponse.builder()
                            .id(book.getId())
                            .title(book.getTitle())
                            .description(book.getDescription())
                            .author(authorResponse)
                            .genre(genreResponse)
                            .createdAt(book.getCreatedAt())
                            .images(images)
                            .isbn(book.getIsbn())
                            .publicationDate(book.getPublicationDate())
                            .build();
                }
        ).toList();

        UserResponse userResponse = UserResponse.builder()
                .firstName(borrowing.getUser().getFirstName())
                .lastName(borrowing.getUser().getLastName())
                .studentCode(borrowing.getUser().getStudentCode())
                .build();

        return BorrowingResponse.builder()
                .id(borrowing.getId())
                .borrowDate(borrowing.getBorrowDate())
                .dueDate(borrowing.getDueDate())
                .returnDate(borrowing.getReturnDate())
                .finalAmount(borrowing.getFinalAmount())
                .status(borrowing.getStatus())
                .userResponse(userResponse)
                .books(bookResponses)
                .build();
    }
    //Get All Borrow
    @Override
    public PaginatedResponse<BorrowingResponse> getAllBorrow(int page, int size) {
        PageRequest pageRequest = PageRequest.of(page, size);
        Page<Borrowing> borrowings = borrowingRepository.findAll(pageRequest);

        List<BorrowingResponse> borrowResponses = borrowings.getContent().stream().map(borrowing -> {
            List<BookResponse> bookResponses = borrowing.getItems().stream().map(item -> {
                Book book = item.getBook();
                List<ImageResponse> images = book.getImages() != null
                        ? book.getImages().stream()
                        .map(img -> ImageResponse.builder()
                                .imageUrl(img.getUrl())
                                .build())
                        .toList()
                        : List.of();
                AuthorResponse authorResponse = AuthorResponse.builder()
                        .id(item.getBook().getAuthor().getId())
                        .name(item.getBook().getAuthor().getName())
                        .bio(item.getBook().getAuthor().getBio())
                        .build();
                GenreResponse genreResponse = GenreResponse.builder()
                        .id(item.getBook().getGenre().getId())
                        .name(item.getBook().getGenre().getName())
                        .description(item.getBook().getGenre().getDescription())
                        .build();

                return BookResponse.builder()
                        .id(book.getId())
                        .title(book.getTitle())
                        .description(book.getDescription())
                        .author(authorResponse)
                        .genre(genreResponse)
                        .createdAt(book.getCreatedAt())
                        .images(images)
                        .publicationDate(book.getPublicationDate())
                        .isbn(book.getIsbn())
                        .build();
            }).toList();
            UserResponse userResponse = UserResponse.builder()
                    .firstName(borrowing.getUser().getFirstName())
                    .lastName(borrowing.getUser().getLastName())
                    .studentCode(borrowing.getUser().getStudentCode())
                    .build();

            return BorrowingResponse.builder()
                    .id(borrowing.getId())
                    .borrowDate(borrowing.getBorrowDate())
                    .dueDate(borrowing.getDueDate())
                    .returnDate(borrowing.getReturnDate())
                    .finalAmount(borrowing.getFinalAmount())
                    .status(borrowing.getStatus())
                    .userResponse(userResponse)
                    .books(bookResponses)
                    .build();
        }).toList();

        return PaginatedResponse.<BorrowingResponse>builder()
                .totalItems((int) borrowings.getTotalElements())
                .totalPages(borrowings.getTotalPages())
                .currentPage(borrowings.getNumber())
                .pageSize(borrowings.getSize())
                .hasNextPage(borrowings.hasNext())
                .hasPreviousPage(borrowings.hasPrevious())
                .elements(borrowResponses)
                .build();
    }

    @Override
    public BorrowingResponse getOneBorrow(String borrowId) {
        Borrowing borrowing = borrowingRepository.findById(borrowId)
                .orElseThrow(() -> new AppException(ErrorCode.BORROW_NOT_EXISTED));

        // Lấy danh sách sách đã mượn
        List<BookResponse> bookResponses = borrowing.getItems().stream().map(item -> {
            Book book = item.getBook();
            List<ImageResponse> images = book.getImages() != null
                    ? book.getImages().stream()
                    .map(img -> ImageResponse.builder()
                            .imageUrl(img.getUrl())
                            .build())
                    .toList()
                    : List.of();

            AuthorResponse authorResponse = AuthorResponse.builder()
                    .id(item.getBook().getAuthor().getId())
                    .name(item.getBook().getAuthor().getName())
                    .bio(item.getBook().getAuthor().getBio())
                    .build();
            GenreResponse genreResponse = GenreResponse.builder()
                    .id(item.getBook().getGenre().getId())
                    .name(item.getBook().getGenre().getName())
                    .description(item.getBook().getGenre().getDescription())
                    .build();

            return BookResponse.builder()
                    .id(book.getId())
                    .title(book.getTitle())
                    .description(book.getDescription())
                    .author(authorResponse)
                    .genre(genreResponse)
                    .createdAt(book.getCreatedAt())
                    .images(images)
                    .isbn(book.getIsbn())
                    .publicationDate(book.getPublicationDate())
                    .build();
        }).toList();

        UserResponse userResponse = UserResponse.builder()
                .firstName(borrowing.getUser().getFirstName())
                .lastName(borrowing.getUser().getLastName())
                .studentCode(borrowing.getUser().getStudentCode())
                .build();

        return BorrowingResponse.builder()
                .id(borrowing.getId())
                .borrowDate(borrowing.getBorrowDate())
                .dueDate(borrowing.getDueDate())
                .returnDate(borrowing.getReturnDate())
                .finalAmount(borrowing.getFinalAmount())
                .status(borrowing.getStatus())
                .userResponse(userResponse)
                .books(bookResponses)
                .build();
    }

    @Override
    public PaginatedResponse<BorrowingResponse> getAllMyBorrow(int page, int size) {
        // Step 1: Get the current authenticated user's username
        var context = SecurityContextHolder.getContext();
        String username = context.getAuthentication().getName();

        // Step 2: Get the user entity using the username
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));

        // Step 3: Create a PageRequest with pagination information
        PageRequest pageRequest = PageRequest.of(page, size);

        // Step 4: Fetch borrowings associated with the user
        Page<Borrowing> borrowings = borrowingRepository.findByUserUsername(username, pageRequest);

        // Step 5: Map the borrowings to BorrowingResponse DTOs
        List<BorrowingResponse> borrowResponses = borrowings.getContent().stream().map(borrowing -> {
            List<BookResponse> bookResponses = borrowing.getItems().stream().map(item -> {
                Book book = item.getBook();
                List<ImageResponse> images = book.getImages() != null
                        ? book.getImages().stream()
                        .map(img -> ImageResponse.builder()
                                .imageUrl(img.getUrl())
                                .build())
                        .toList()
                        : List.of();

                AuthorResponse authorResponse = AuthorResponse.builder()
                        .id(item.getBook().getAuthor().getId())
                        .name(item.getBook().getAuthor().getName())
                        .bio(item.getBook().getAuthor().getBio())
                        .build();

                GenreResponse genreResponse = GenreResponse.builder()
                        .id(item.getBook().getGenre().getId())
                        .name(item.getBook().getGenre().getName())
                        .description(item.getBook().getGenre().getDescription())
                        .build();

                return BookResponse.builder()
                        .id(book.getId())
                        .title(book.getTitle())
                        .description(book.getDescription())
                        .author(authorResponse)
                        .genre(genreResponse)
                        .createdAt(book.getCreatedAt())
                        .images(images)
                        .isbn(book.getIsbn())
                        .publicationDate(book.getPublicationDate())
                        .build();
            }).toList();

            UserResponse userResponse = UserResponse.builder()
                    .firstName(borrowing.getUser().getFirstName())
                    .lastName(borrowing.getUser().getLastName())
                    .studentCode(borrowing.getUser().getStudentCode())
                    .build();

            return BorrowingResponse.builder()
                    .id(borrowing.getId())
                    .borrowDate(borrowing.getBorrowDate())
                    .dueDate(borrowing.getDueDate())
                    .returnDate(borrowing.getReturnDate())
                    .finalAmount(borrowing.getFinalAmount())
                    .status(borrowing.getStatus())
                    .userResponse(userResponse)
                    .books(bookResponses)
                    .build();
        }).toList();

        // Step 6: Return the PaginatedResponse
        return PaginatedResponse.<BorrowingResponse>builder()
                .totalItems((int) borrowings.getTotalElements())
                .totalPages(borrowings.getTotalPages())
                .currentPage(borrowings.getNumber())
                .pageSize(borrowings.getSize())
                .hasNextPage(borrowings.hasNext())
                .hasPreviousPage(borrowings.hasPrevious())
                .elements(borrowResponses)
                .build();
    }

    @Override
    public long countBorrow(){
        return borrowingRepository.countBorrowed();
    }
    @Override
    public long countOverdue(){
        return borrowingRepository.countOverdue();
    }
    @Override
    public PaginatedResponse<BorrowingResponse> getAllBorrowWithFilter(String id, String name, LocalDate borrowDate, int page, int size) {
        PageRequest pageRequest = PageRequest.of(page, size);
        // Gọi repo tìm kiếm
        Page<Borrowing> borrowings = borrowingRepository.findByBorrowIdAndNameAndBorrowDate(id, name, borrowDate, pageRequest);

        List<BorrowingResponse> borrowResponses = borrowings.getContent().stream().map(borrowing -> {
            List<BookResponse> bookResponses = borrowing.getItems().stream().map(item -> {
                Book book = item.getBook();
                List<ImageResponse> images = book.getImages() != null
                        ? book.getImages().stream()
                        .map(img -> ImageResponse.builder()
                                .imageUrl(img.getUrl())
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
                        .createdAt(book.getCreatedAt())
                        .images(images)
                        .isbn(book.getIsbn())
                        .publicationDate(book.getPublicationDate())
                        .build();
            }).toList();

            UserResponse userResponse = UserResponse.builder()
                    .firstName(borrowing.getUser().getFirstName())
                    .lastName(borrowing.getUser().getLastName())
                    .studentCode(borrowing.getUser().getStudentCode())
                    .build();

            return BorrowingResponse.builder()
                    .id(borrowing.getId())
                    .borrowDate(borrowing.getBorrowDate())
                    .dueDate(borrowing.getDueDate())
                    .returnDate(borrowing.getReturnDate())
                    .finalAmount(borrowing.getFinalAmount())
                    .status(borrowing.getStatus())
                    .userResponse(userResponse)
                    .books(bookResponses)
                    .build();
        }).toList();

        return PaginatedResponse.<BorrowingResponse>builder()
                .totalItems((int) borrowings.getTotalElements())
                .totalPages(borrowings.getTotalPages())
                .currentPage(borrowings.getNumber())
                .pageSize(borrowings.getSize())
                .hasNextPage(borrowings.hasNext())
                .hasPreviousPage(borrowings.hasPrevious())
                .elements(borrowResponses)
                .build();
    }



}
