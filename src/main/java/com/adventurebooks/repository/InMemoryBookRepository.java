package com.adventurebooks.repository;

import com.adventurebooks.model.entity.Book;
import com.adventurebooks.model.enums.Difficulty;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

@Repository
public class InMemoryBookRepository implements BookRepository {

    private final Map<Long, Book> booksById = new ConcurrentHashMap<>();
    private final AtomicLong idSequence = new AtomicLong(1);

    @Override
    public void saveAll(List<Book> books) {
        if (books == null) {
            return;
        }

        for (Book book : books) {
            if (book != null && book.getTitle() != null) {
                book.setId(idSequence.getAndIncrement());
                booksById.put(book.getId(), book);
            }
        }
    }

    @Override
    public Book save(Book book) {
        if (book.getId() == null) {
            book.setId(idSequence.getAndIncrement());
        }
        booksById.put(book.getId(), book);
        return book;
    }

    @Override
    public List<Book> findAll() {
        return booksById.values().stream()
                .toList();
    }

    @Override
    public Optional<Book> findById(Long id) {
        return Optional.ofNullable(booksById.get(id));
    }

    @Override
    public List<Book> findByDifficulty(Difficulty difficulty) {
        if (difficulty == null) {
            return findAll();
        }

        return booksById.values().stream()
                .filter(book -> book.getDifficulty() == difficulty)
                .toList();
    }

    @Override
    public List<Book> searchByTitle(String query) {
        if (query == null || query.isBlank()) {
            return findAll();
        }

        String normalized = query.trim().toLowerCase();
        return booksById.values().stream()
                .filter(book -> book.getTitle() != null && book.getTitle().toLowerCase().contains(normalized))
                .toList();
    }
}
