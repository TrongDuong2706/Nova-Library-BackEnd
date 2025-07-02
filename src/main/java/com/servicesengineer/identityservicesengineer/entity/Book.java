package com.servicesengineer.identityservicesengineer.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Entity
@Table(name = "books")
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
@Setter
public class Book {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;
    @Column(name = "title")
    private String title;

    @Column(name = "description")
    private String description;

    @Column(name = "isbn", nullable = false, unique = true)
    private String isbn;

    @Column(name = "publication_date")
    private LocalDate publicationDate;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "book_authors", // Tên bảng trung gian
            joinColumns = @JoinColumn(name = "book_id"), // Khóa ngoại trỏ tới Book
            inverseJoinColumns = @JoinColumn(name = "author_id") // Khóa ngoại trỏ tới Author
    )
    private Set<Author> authors = new HashSet<>();

    // --- THAY ĐỔI Ở ĐÂY: Từ ManyToOne thành ManyToMany ---
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "book_genres", // Tên bảng trung gian
            joinColumns = @JoinColumn(name = "book_id"), // Khóa ngoại trỏ tới Book
            inverseJoinColumns = @JoinColumn(name = "genre_id") // Khóa ngoại trỏ tới Genre
    )
    private Set<Genre> genres = new HashSet<>();

    @Column(name ="stock")
    private int stock;

    @Column(name = "status")
    private int status;

    @OneToMany(mappedBy = "book", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<FavoriteBook> favoritedByUsers = new HashSet<>();

    private LocalDate createdAt;

    @OneToMany(mappedBy = "book", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Image> images;
}
