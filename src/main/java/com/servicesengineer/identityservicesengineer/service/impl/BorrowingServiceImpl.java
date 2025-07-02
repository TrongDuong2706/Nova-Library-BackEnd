package com.servicesengineer.identityservicesengineer.service.impl;

import com.servicesengineer.identityservicesengineer.dto.request.BookRenewalRequest;
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
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

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
    @Transactional
    public BorrowingResponse createBorrowing(BorrowingRequest request) {
        User user = userRepository.findByStudentCode(request.getStudentCode())
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));

        if(user.getStatus().equals(UserStatus.DELETED)){
            throw new AppException(ErrorCode.DISABLE_ACCOUNT);
        }

        if (borrowingRepository.existsByUserAndStatusIn(user, List.of(BorrowingStatus.BORROWED, BorrowingStatus.OVERDUE))) {
            throw new AppException(ErrorCode.USER_HAS_UNRETURNED_BORROWING);
        }

        if (request.getBookIds().size() > 3) {
            throw new AppException(ErrorCode.EXCEED_BOOK_QUANTITY_BORROW);
        }

        LocalDate borrowDate = LocalDate.now();
        LocalDate dueDate = request.getDueDate();
        if (dueDate == null || dueDate.isBefore(borrowDate) || ChronoUnit.DAYS.between(borrowDate, dueDate) > 14) {
            throw new AppException(ErrorCode.INVALID_DUE_DATE);
        }

        Borrowing borrowing = Borrowing.builder()
                .borrowDate(borrowDate)
                .dueDate(dueDate)
                .user(user)
                .status(BorrowingStatus.BORROWED)
                .build();

        List<BorrowingRecordItem> items = new ArrayList<>();
        for (String bookId : request.getBookIds()) {
            Book book = bookRepository.findById(bookId)
                    .orElseThrow(() -> new AppException(ErrorCode.BOOK_NOT_EXISTED));

            if (book.getStock() <= 0) {
                throw new AppException(ErrorCode.BOOK_OUT_OF_STOCK);
            }

            book.setStock(book.getStock() - 1);
            // bookRepository.save(book) sẽ được thực hiện tự động bởi @Transactional

            items.add(BorrowingRecordItem.builder().book(book).borrowing(borrowing).build());

            // Xóa khỏi danh sách yêu thích nếu có
            FavoriteBook favoriteBook = favoriteBookRepository.findByUserAndBook(user, book);
            if (favoriteBook != null) {
                favoriteBookRepository.delete(favoriteBook);
            }
        }

        borrowing.setItems(items);
        Borrowing savedBorrowing = borrowingRepository.save(borrowing);

        return convertToBorrowingResponse(savedBorrowing);
    }

    @Override
    @Transactional
    public BorrowingResponse returnBorrow(String borrowId) {
        Borrowing borrowing = borrowingRepository.findById(borrowId)
                .orElseThrow(() -> new AppException(ErrorCode.BORROW_NOT_EXISTED));

        if (borrowing.getStatus() == BorrowingStatus.RETURNED) {
            throw new AppException(ErrorCode.BORROWING_ALREADY_RETURNED);
        }

        LocalDate today = LocalDate.now();
        borrowing.setReturnDate(today);

        if (today.isAfter(borrowing.getDueDate())) {
            long daysLate = ChronoUnit.DAYS.between(borrowing.getDueDate(), today);
            double finePerDay = 30000.0;
            borrowing.setFinalAmount(daysLate * finePerDay);
        } else {
            borrowing.setFinalAmount(0.0);
        }

        for (BorrowingRecordItem item : borrowing.getItems()) {
            Book book = item.getBook();
            book.setStock(book.getStock() + 1);
        }

        borrowing.setStatus(BorrowingStatus.RETURNED);
        Borrowing savedBorrowing = borrowingRepository.save(borrowing);

        return convertToBorrowingResponse(savedBorrowing);
    }

    // --- CÁC PHƯƠNG THỨC GET SỬ DỤNG HELPER METHODS ---

    @Override
    public PaginatedResponse<BorrowingResponse> getAllBorrow(int page, int size) {
        Page<Borrowing> borrowingsPage = borrowingRepository.findAll(PageRequest.of(page, size));
        return createPaginatedBorrowingResponse(borrowingsPage);
    }

    @Override
    public BorrowingResponse getOneBorrow(String borrowId) {
        Borrowing borrowing = borrowingRepository.findById(borrowId)
                .orElseThrow(() -> new AppException(ErrorCode.BORROW_NOT_EXISTED));
        return convertToBorrowingResponse(borrowing);
    }

    @Override
    public PaginatedResponse<BorrowingResponse> getAllMyBorrow(int page, int size) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        userRepository.findByUsername(username)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));

        Page<Borrowing> borrowingsPage = borrowingRepository.findByUserUsername(username, PageRequest.of(page, size));
        return createPaginatedBorrowingResponse(borrowingsPage);
    }

    @Override
    public PaginatedResponse<BorrowingResponse> getAllBorrowWithFilter(String id, String name, LocalDate borrowDate, int page, int size) {
        Page<Borrowing> borrowingsPage = borrowingRepository.findByBorrowIdAndNameAndBorrowDate(id, name, borrowDate, PageRequest.of(page, size));
        return createPaginatedBorrowingResponse(borrowingsPage);
    }

    @Override
    public PaginatedResponse<BorrowingResponse> getAllBorrowWithOverdueStatus(int page, int size) {
        Page<Borrowing> borrowingsPage = borrowingRepository.findByStatus(BorrowingStatus.OVERDUE, PageRequest.of(page, size));
        return createPaginatedBorrowingResponse(borrowingsPage);
    }

    @Override
    public PaginatedResponse<BorrowingResponse> getAllBorrowByUserId(String userId, int page, int size) {
        Page<Borrowing> borrowingsPage = borrowingRepository.findByUserId(userId, PageRequest.of(page, size));
        return createPaginatedBorrowingResponse(borrowingsPage);
    }

    // --- CÁC PHƯƠNG THỨC KHÁC ---

    @Override
    @Transactional
    public void bookRenewal(String borrowId, BookRenewalRequest bookRenewalRequest) {
        Borrowing borrowing = borrowingRepository.findById(borrowId)
                .orElseThrow(() -> new AppException(ErrorCode.BORROW_NOT_EXISTED));

        if (bookRenewalRequest.getNewDueDate() == null || bookRenewalRequest.getNewDueDate().isBefore(LocalDate.now())) {
            throw new AppException(ErrorCode.INVALID_DUE_DATE);
        }
        if (borrowing.getStatus() == BorrowingStatus.RETURNED) {
            throw new AppException(ErrorCode.BORROWING_ALREADY_RETURNED);
        }
        if (borrowing.getStatus() == BorrowingStatus.OVERDUE) {
            throw new AppException(ErrorCode.OVERDUE_ALREADY);
        }

        LocalDate borrowDate = borrowing.getBorrowDate();
        long totalDays = ChronoUnit.DAYS.between(borrowDate, bookRenewalRequest.getNewDueDate());
        if (totalDays > 30) {
            throw new AppException(ErrorCode.BORROW_RENEWAL_EXCEEDS_LIMIT);
        }

        borrowing.setDueDate(bookRenewalRequest.getNewDueDate());
        borrowingRepository.save(borrowing);
    }

    @Override
    @Scheduled(cron = "0 0 0 * * ?")
    @Transactional
    public void updateOverdueStatuses() {
        LocalDate today = LocalDate.now();
        List<Borrowing> overdueBorrowings = borrowingRepository.findByStatusAndDueDateBefore(BorrowingStatus.BORROWED, today);
        for (Borrowing borrowing : overdueBorrowings) {
            borrowing.setStatus(BorrowingStatus.OVERDUE);
        }
        borrowingRepository.saveAll(overdueBorrowings);
    }

    @Override
    public long countBorrow() {
        return borrowingRepository.countBorrowed();
    }

    @Override
    public long countOverdue() {
        return borrowingRepository.countOverdue();
    }

    // =================================================================
    //  HELPER METHODS (PHƯƠNG THỨC HỖ TRỢ)
    // =================================================================

    /**
     * Chuyển đổi một Page<Borrowing> thành PaginatedResponse<BorrowingResponse>.
     */
    private PaginatedResponse<BorrowingResponse> createPaginatedBorrowingResponse(Page<Borrowing> borrowingsPage) {
        List<BorrowingResponse> borrowingResponses = borrowingsPage.getContent().stream()
                .map(this::convertToBorrowingResponse)
                .toList();

        return PaginatedResponse.<BorrowingResponse>builder()
                .elements(borrowingResponses)
                .currentPage(borrowingsPage.getNumber())
                .totalPages(borrowingsPage.getTotalPages())
                .totalItems((int) borrowingsPage.getTotalElements())
                .pageSize(borrowingsPage.getSize())
                .hasNextPage(borrowingsPage.hasNext())
                .hasPreviousPage(borrowingsPage.hasPrevious())
                .build();
    }

    /**
     * Chuyển đổi một thực thể Borrowing thành BorrowingResponse.
     * Đây là nơi tập trung logic mapping chính.
     */
    private BorrowingResponse convertToBorrowingResponse(Borrowing borrowing) {
        User user = borrowing.getUser();
        UserResponse userResponse = UserResponse.builder()
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .studentCode(user.getStudentCode())
                .build();

        List<BookResponse> bookResponses = borrowing.getItems().stream()
                .map(item -> convertToBookResponse(item.getBook()))
                .toList();

        return BorrowingResponse.builder()
                .id(borrowing.getId())
                .borrowDate(borrowing.getBorrowDate())
                .dueDate(borrowing.getDueDate())
                .returnDate(borrowing.getReturnDate())
                .status(borrowing.getStatus())
                .finalAmount(borrowing.getFinalAmount())
                .userResponse(userResponse)
                .books(bookResponses)
                .build();
    }

    /**
     * Chuyển đổi một thực thể Book thành BookResponse.
     * Phương thức này đã được cập nhật để xử lý ManyToMany.
     */
    private BookResponse convertToBookResponse(Book book) {
        // --- SỬA ĐỔI Ở ĐÂY ---
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

        List<ImageResponse> imageResponses = (book.getImages() != null)
                ? book.getImages().stream()
                .map(img -> ImageResponse.builder().imageUrl(img.getUrl()).build())
                .toList()
                : List.of();

        return BookResponse.builder()
                .id(book.getId())
                .title(book.getTitle())
                .description(book.getDescription())
                .authors(authorResponses) // Thay vì author
                .genres(genreResponses)   // Thay vì genre
                .createdAt(book.getCreatedAt())
                .images(imageResponses)
                .publicationDate(book.getPublicationDate())
                .isbn(book.getIsbn())
                .stock(book.getStock()) // Thêm stock để dễ theo dõi
                .status(book.getStatus()) // Thêm status
                .build();
    }
}