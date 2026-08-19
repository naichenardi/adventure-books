package com.adventurebooks.repository;

import com.adventurebooks.model.entity.Book;
import com.adventurebooks.model.enums.Difficulty;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface BookRepository extends JpaRepository<Book, Long> {

    @Query("SELECT b FROM Book b WHERE :difficulty IS NULL OR b.difficulty = :difficulty")
    List<Book> findByDifficulty(@Param("difficulty") Difficulty difficulty);

    @Query("SELECT b FROM Book b WHERE :query IS NULL OR :query = '' OR LOWER(b.title) LIKE LOWER(CONCAT('%', :query, '%'))")
    List<Book> searchByTitle(@Param("query") String query);
}
