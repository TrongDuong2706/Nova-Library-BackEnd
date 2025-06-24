package com.servicesengineer.identityservicesengineer.repository;

import com.servicesengineer.identityservicesengineer.entity.Book;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface BookRepository extends JpaRepository<Book, String> {

    @Query(value = "SELECT b FROM Book b " +
            "JOIN b.author a " +
            "JOIN b.genre g " +
            "WHERE (:authorName IS NULL OR a.name LIKE %:authorName%) " +
            "AND (:genreName IS NULL OR g.name LIKE %:genreName%) " +
            "AND (:title IS NULL OR b.title LIKE %:title%) " +
            "AND (:description IS NULL OR b.description LIKE %:description%) " +
            "AND b.status = 1")
    Page<Book> findByAuthorNameAndGenreNameAndTitleAndDescription(
            @Param("authorName") String authorName,
            @Param("genreName") String genreName,
            @Param("title") String title,
            @Param("description") String description, Pageable pageable);
    @Query("SELECT COUNT(b) FROM Book b")
    long countBooks();

}
