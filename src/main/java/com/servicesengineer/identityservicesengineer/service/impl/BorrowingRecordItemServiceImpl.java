package com.servicesengineer.identityservicesengineer.service.impl;

import com.servicesengineer.identityservicesengineer.dto.response.MonthlyBorrowStatResponse;
import com.servicesengineer.identityservicesengineer.repository.BorrowingRecordItemRepository;
import com.servicesengineer.identityservicesengineer.service.BorrowingRecordItemService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class BorrowingRecordItemServiceImpl implements BorrowingRecordItemService {
    BorrowingRecordItemRepository borrowingRecordItemRepository;
    @Override
    public Map<String, Long> getMonthlyBorrowStats() {
        List<Object[]> rawResults = borrowingRecordItemRepository.countBooksBorrowedByMonthRaw();
        return rawResults.stream()
                .collect(Collectors.toMap(
                        row -> (String) row[0],
                        row -> ((Number) row[1]).longValue()
                ));
    }

}
