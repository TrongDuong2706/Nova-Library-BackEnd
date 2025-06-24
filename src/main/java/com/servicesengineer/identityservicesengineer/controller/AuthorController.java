package com.servicesengineer.identityservicesengineer.controller;

import com.servicesengineer.identityservicesengineer.dto.ApiResponse;
import com.servicesengineer.identityservicesengineer.dto.request.AuthorRequest;
import com.servicesengineer.identityservicesengineer.dto.response.AuthorResponse;
import com.servicesengineer.identityservicesengineer.dto.response.PaginatedResponse;
import com.servicesengineer.identityservicesengineer.service.AuthorService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/author")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class AuthorController {
    AuthorService authorService;

    @PostMapping()
    public ApiResponse<AuthorResponse> createAuthor(@RequestBody AuthorRequest request){
        return ApiResponse.<AuthorResponse>builder()
                .message("Create Author Successful")
                .result(authorService.createAuthor(request))
                .build();
    }
    @GetMapping()
    public ApiResponse<PaginatedResponse<AuthorResponse>> getAllAuthor(  @RequestParam(defaultValue = "1") int page,
                                                                         @RequestParam(defaultValue = "10") int size){
        int adjustedPage = Math.max(page - 1, 0);
        return ApiResponse.<PaginatedResponse<AuthorResponse>>builder()
                .message("Get All Author Successful")
                .result(authorService.getAllAuthor(adjustedPage, size))
                .build();
    }
    @GetMapping("/{authorId}")
    public ApiResponse<AuthorResponse> getOneAuthor(@PathVariable String authorId){
        return ApiResponse.<AuthorResponse>builder()
                .message("Get One Author Successful")
                .result(authorService.getOneAuthor(authorId))
                .build();
    }
    @PutMapping("/{authorId}")
    public ApiResponse<AuthorResponse> updateAuthor(@PathVariable String authorId, @RequestBody AuthorRequest request){
        return ApiResponse.<AuthorResponse>builder()
                .message("Update Author Successful")
                .result(authorService.updateAuthor(authorId,request))
                .build();
    }
    @DeleteMapping("/{authorId}")
    public ApiResponse<String> deleteAuthor(@PathVariable String authorId){
        return ApiResponse.<String>builder()
                .message("Delete Successful")
                .result(authorService.deleteAuthor(authorId))
                .build();
    }
}
