package com.servicesengineer.identityservicesengineer.repository;

import com.servicesengineer.identityservicesengineer.entity.Book;
import com.servicesengineer.identityservicesengineer.entity.FavoriteBook;
import com.servicesengineer.identityservicesengineer.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FavoriteBookRepository extends JpaRepository<FavoriteBook, String> {
    boolean existsByUserAndBook(User user, Book book);
    Page<FavoriteBook> findByUser(User user, Pageable pageable);
    FavoriteBook findByUserAndBook(User user, Book book);

}
