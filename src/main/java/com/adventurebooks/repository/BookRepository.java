package com.adventurebooks.repository;

import com.adventurebooks.model.entity.Book;
import com.adventurebooks.model.enums.Difficulty;

import java.util.List;
import java.util.Optional;

public interface BookRepository {
    void saveAll(List<Book> books);

    List<Book> findAll();

    Optional<Book> findById(String id);

    List<Book> findByDifficulty(Difficulty difficulty);

    List<Book> searchByTitle(String query);
}
