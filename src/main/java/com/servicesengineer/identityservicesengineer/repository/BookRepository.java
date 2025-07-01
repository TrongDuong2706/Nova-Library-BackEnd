package com.servicesengineer.identityservicesengineer.repository;

import com.servicesengineer.identityservicesengineer.entity.Author;
import com.servicesengineer.identityservicesengineer.entity.Book;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface BookRepository extends JpaRepository<Book, String> {

    @Query(value = "SELECT b FROM Book b " +
            "JOIN b.author a " +
            "JOIN b.genre g " +
            "WHERE (:authorName IS NULL OR a.name LIKE %:authorName%) " +
            "AND (:genreName IS NULL OR g.name LIKE %:genreName%) " +
            "AND (:keyword IS NULL OR b.title LIKE %:keyword% OR b.description LIKE %:keyword%) " +
            "AND b.status = 1")
    Page<Book> findByAuthorNameAndGenreNameAndTitleAndDescription(
            @Param("authorName") String authorName,
            @Param("genreName") String genreName,
            @Param("keyword") String keyword,
             Pageable pageable);

    @Query(value = "SELECT b FROM Book b " +
            "JOIN b.author a " +
            "JOIN b.genre g " +
            "WHERE (:authorName IS NULL OR a.name LIKE %:authorName%) " +
            "AND (:genreName IS NULL OR g.name LIKE %:genreName%) " +
            "AND (:keyword IS NULL OR b.title LIKE %:keyword% OR b.description LIKE %:keyword%) " +
            "AND (:status IS NULL OR b.status = :status) " +
            "AND (:isbn IS NULL OR b.isbn LIKE %:isbn%)")
    Page<Book> findByFilters(
            @Param("authorName") String authorName,
            @Param("genreName") String genreName,
            @Param("keyword") String title,
            @Param("status") Integer status,
            @Param("isbn") String isbn,
            Pageable pageable);


    @Query("SELECT COUNT(b) FROM Book b")
    long countBooks();

    Page<Book> findByStock(int stock, Pageable pageable);

    @Query("SELECT b FROM Book b JOIN b.genre g WHERE LOWER(g.name) LIKE LOWER(CONCAT('%', :genreName, '%'))")
    Page<Book> findBooksByGenreName(@Param("genreName") String genreName, Pageable pageable);
    boolean existsByIsbn(String isbn);

    Optional<Book> findByIsbn(String isbn);

    Page<Book> findByTitleContainingIgnoreCase(String title, Pageable pageable);


}
