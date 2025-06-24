package com.servicesengineer.identityservicesengineer.controller;

import com.servicesengineer.identityservicesengineer.dto.ApiResponse;
import com.servicesengineer.identityservicesengineer.dto.response.PaginatedResponse;
import com.servicesengineer.identityservicesengineer.dto.response.SimpleBookResponse;
import com.servicesengineer.identityservicesengineer.service.FavoriteBookService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/favorite")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class FavoriteController {
    FavoriteBookService favoriteBookService;

    @PostMapping("/{bookId}")
    public ApiResponse<SimpleBookResponse> addFavorite(@PathVariable String bookId){
        return ApiResponse.<SimpleBookResponse>builder()
                .message("Add Favourite Successful")
                .result(favoriteBookService.addFavorite(bookId))
                .build();
    }

    @GetMapping()
    public ApiResponse<PaginatedResponse<SimpleBookResponse>> getAllFavorite(@RequestParam(defaultValue = "1") int page,
                                                                             @RequestParam(defaultValue = "10") int size){
        int adjustedPage = Math.max(page - 1, 0);
        return ApiResponse.<PaginatedResponse<SimpleBookResponse>>builder()
                .message("GetAllFavorite")
                .result(favoriteBookService.getAllFavorite(adjustedPage, size))
                .build();
    }
    @DeleteMapping("/{bookId}")
    public ApiResponse<Void> deleteBookFavorite(@PathVariable String bookId){
        favoriteBookService.removeFavorite(bookId);  // Gọi service, không cần gán kết quả
        return ApiResponse.<Void>builder()
                .message("Delete Favorite Book Successful")
                .result(null)
                .build();
    }
}
