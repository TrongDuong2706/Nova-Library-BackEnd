package com.servicesengineer.identityservicesengineer.controller;

import com.servicesengineer.identityservicesengineer.dto.ApiResponse;
import com.servicesengineer.identityservicesengineer.dto.response.MonthlyBorrowStatResponse;
import com.servicesengineer.identityservicesengineer.service.BorrowingRecordItemService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.Map;

@RestController
@RequestMapping("/borrowingRecordItem")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class BorrowingRecordItemController {
    BorrowingRecordItemService borrowingRecordItemService;
    @GetMapping()
    public ApiResponse<Map<String, Long>> getBookStat(){
        return ApiResponse.<Map<String, Long>>builder()
                .result(borrowingRecordItemService.getMonthlyBorrowStats())
                .build();
    }
}
