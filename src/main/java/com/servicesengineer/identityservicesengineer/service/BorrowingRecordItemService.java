package com.servicesengineer.identityservicesengineer.service;

import com.servicesengineer.identityservicesengineer.dto.response.MonthlyBorrowStatResponse;

import java.util.List;
import java.util.Map;

public interface BorrowingRecordItemService {
    Map<String, Long> getMonthlyBorrowStats();
}
