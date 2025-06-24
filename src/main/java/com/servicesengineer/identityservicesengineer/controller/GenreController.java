package com.servicesengineer.identityservicesengineer.controller;

import com.servicesengineer.identityservicesengineer.dto.ApiResponse;
import com.servicesengineer.identityservicesengineer.dto.request.GenreRequest;
import com.servicesengineer.identityservicesengineer.dto.response.AuthorResponse;
import com.servicesengineer.identityservicesengineer.dto.response.GenreResponse;
import com.servicesengineer.identityservicesengineer.dto.response.PaginatedResponse;
import com.servicesengineer.identityservicesengineer.service.GenreService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/genres")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class GenreController {
    GenreService genreService;

    @PostMapping()
    public ApiResponse<GenreResponse> createGenre(@RequestBody GenreRequest request){
        return ApiResponse.<GenreResponse>builder()
                .message("Create Genre Successful")
                .result(genreService.createGenre(request))
                .build();
    }
    @GetMapping()
    public ApiResponse<PaginatedResponse<GenreResponse>> getAllGenre(@RequestParam(defaultValue = "1") int page,
                                                                     @RequestParam(defaultValue = "10") int size){
        int adjustedPage = Math.max(page - 1, 0);
        return ApiResponse.<PaginatedResponse<GenreResponse>>builder()
                .message("Get All Genre Successful")
                .result(genreService.getAllGenre(adjustedPage, size))
                .build();
    }
    @GetMapping("/{genreId}")
    public ApiResponse<GenreResponse> getOneGenre(@PathVariable String genreId){
        return ApiResponse.<GenreResponse>builder()
                .result(genreService.getOneGenre(genreId))
                .build();
    }
    @PutMapping("/{genreId}")
    public ApiResponse<GenreResponse> updateGenre(@PathVariable String genreId, @RequestBody GenreRequest request){
        return ApiResponse.<GenreResponse>builder()
                .message("Update Genre Successful")
                .result(genreService.updateGenre(genreId, request))
                .build();
    }
    @DeleteMapping("/{genreId}")
    public ApiResponse<String> deleteGenre(@PathVariable String genreId){
        return ApiResponse.<String>builder()
                .result(genreService.deleteGenre(genreId))
                .build();
    }

}
