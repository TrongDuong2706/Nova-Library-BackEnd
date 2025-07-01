package com.servicesengineer.identityservicesengineer.controller;

import com.servicesengineer.identityservicesengineer.dto.ApiResponse;
import com.servicesengineer.identityservicesengineer.dto.request.BookRequest;
import com.servicesengineer.identityservicesengineer.dto.response.BookResponse;
import com.servicesengineer.identityservicesengineer.dto.response.PaginatedResponse;
import com.servicesengineer.identityservicesengineer.service.BookService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/books")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class BookController {
    BookService bookService;

    @PostMapping()
    public ApiResponse<BookResponse> createBook(@RequestPart("books") BookRequest request,
                                                @RequestPart(value = "images", required = false)  List<MultipartFile> files) {
        return ApiResponse.<BookResponse>builder()
                .message("Create book Successful")
                .result(bookService.createBook(request,files))
                .build();
    }

    @GetMapping()
    public ApiResponse<PaginatedResponse<BookResponse>> getAllBook(@RequestParam(defaultValue = "1") int page,
                                                                   @RequestParam(defaultValue = "10") int size) {
        int adjustedPage = Math.max(page - 1, 0);
        return ApiResponse.<PaginatedResponse<BookResponse>>builder()
                .message("Get All Book Successful")
                .result(bookService.getAllBook(adjustedPage, size))
                .build();
    }

    @GetMapping("/{bookId}")
    public ApiResponse<BookResponse> getOneBook(@PathVariable String bookId) {
        return ApiResponse.<BookResponse>builder()
                .message("Get One Book Successful")
                .result(bookService.getOneBook(bookId))
                .build();
    }
    @PutMapping("/{bookId}")
    public ApiResponse<BookResponse> updateBook(@PathVariable String bookId,
                                                @RequestPart("books") BookRequest request,
                                                @RequestPart("images") List<MultipartFile> files
                                                ){
        return ApiResponse.<BookResponse>builder()
                .message("Update Book Successful")
                .result(bookService.updateBook(bookId, request, files))
                .build();
    }
    @GetMapping("/filter")
    public ApiResponse<PaginatedResponse<BookResponse>> getAllBooksWithFilter(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String authorName,
            @RequestParam(required = false) String genreName,
            @RequestParam(required = false) String keyword
        ) {

        int adjustedPage = Math.max(page - 1, 0);
        var books = bookService.getAllBookWithFilter(authorName, genreName, keyword, adjustedPage, size);
        return ApiResponse.<PaginatedResponse<BookResponse>>builder()
                .result(books)
                .build();
    }
    @DeleteMapping("/{bookId}")
    public ApiResponse<Void> softDelete(@PathVariable String bookId){
        bookService.softDelete(bookId);
        return ApiResponse.<Void>builder()
                .message("Delete book successful")
                .result(null)
                .build();
    }
    @GetMapping("/countBook")
    public ApiResponse<Long> countBook(){
        return ApiResponse.<Long>builder()
                .message("Count book successful")
                .result(bookService.countActiveBooks())
                .build();
    }

    @GetMapping("/filterAdmin")
    public ApiResponse<PaginatedResponse<BookResponse>> getAllBooksWithFilterAdmin(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String authorName,
            @RequestParam(required = false) String genreName,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String isbn,
            @RequestParam(required = false) Integer status) {

        int adjustedPage = Math.max(page - 1, 0);
        var books = bookService.getAllBookWithAdminFilter(authorName, genreName, keyword, status,isbn, adjustedPage, size);
        return ApiResponse.<PaginatedResponse<BookResponse>>builder()
                .message("Get Books with filter successful")
                .result(books)
                .build();
    }
    @GetMapping("/getAllBookZeroStock")
    public ApiResponse<PaginatedResponse<BookResponse>> getAllBookWithZeroStock(@RequestParam(defaultValue = "1") int page,
                                                                                @RequestParam(defaultValue = "10") int size){
        int adjustedPage = Math.max(page - 1, 0);
        return ApiResponse.<PaginatedResponse<BookResponse>>builder()
                .message("Get All Book With Zero Stock Successful")
                .result(bookService.getAllBookZeroStock(adjustedPage,size))
                .build();
    }

    @GetMapping("/getBookWithGenre")
    public ApiResponse<PaginatedResponse<BookResponse>> getAllBookWithGenre(@RequestParam String genreName,
                                                                            @RequestParam(defaultValue = "1") int page,
                                                                            @RequestParam(defaultValue = "10") int size){
        int adjustedPage = Math.max(page - 1, 0);
        return ApiResponse.<PaginatedResponse<BookResponse>>builder()
                .message("Get All Book With Zero Stock Successful")
                .result(bookService.getAllBookWithGenre(genreName,adjustedPage,size))
                .build();

    }
    @GetMapping("/getBookByTitle")
    public ApiResponse<PaginatedResponse<BookResponse>> getAllBookByTitle(@RequestParam String title,
                                                                          @RequestParam(defaultValue = "1") int page,
                                                                          @RequestParam(defaultValue = "10") int size){
        int adjustedPage = Math.max(page - 1, 0);
        return ApiResponse.<PaginatedResponse<BookResponse>>builder()
                .message("Get All Book By tile successful")
                .result(bookService.getAllBookByTitle(title,adjustedPage,size))
                .build();
    }

}
