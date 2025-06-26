package com.servicesengineer.identityservicesengineer.service;

import com.servicesengineer.identityservicesengineer.entity.Borrowing;
import com.servicesengineer.identityservicesengineer.entity.BorrowingStatus;
import com.servicesengineer.identityservicesengineer.repository.BorrowingRepository;
import jakarta.transaction.Transactional;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class MailService {
    JavaMailSender mailSender;
    BorrowingRepository borrowingRepository;

    @Scheduled(cron = "0 0 0 * * ?") // Chạy lúc 0h mỗi ngày
    @Transactional
    public void sendReminderEmails() {
        LocalDate today = LocalDate.now();
        LocalDate dueTomorrow = today.plusDays(1);

        List<Borrowing> borrowingsDueTomorrow = borrowingRepository
                .findByStatusAndDueDate(BorrowingStatus.BORROWED, dueTomorrow);

        for (Borrowing borrowing : borrowingsDueTomorrow) {
            sendReminderEmail(borrowing);
        }

        System.out.println("Đã gửi mail nhắc nhở cho các đơn mượn sắp đến hạn.");
    }

    private void sendReminderEmail(Borrowing borrowing) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom("trongduongleftleg@gmail.com");
        message.setTo(borrowing.getUser().getEmail());
        message.setSubject("Nhắc nhở: Đơn mượn sắp đến hạn");
        message.setText("Chào " + borrowing.getUser().getFirstName() + " "+borrowing.getUser().getLastName()
                + ",\n\nĐơn mượn sách của bạn sẽ đến hạn vào ngày: " + borrowing.getDueDate()
                + ".\nVui lòng trả sách đúng hạn để tránh bị quá hạn.\n\nXin cảm ơn!");

        mailSender.send(message);
        System.out.println("Đã gửi mail tới: " + borrowing.getUser().getEmail());
    }
}
