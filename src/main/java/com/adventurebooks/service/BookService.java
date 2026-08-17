package com.adventurebooks.service;

import com.adventurebooks.model.entity.Book;
import com.adventurebooks.model.enums.Difficulty;
import com.adventurebooks.repository.BookRepository;
import com.adventurebooks.validation.BookValidationResult;
import com.adventurebooks.validation.BookValidationService;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class BookService {

    private final BookRepository bookRepository;
    private final BookLoaderService bookLoaderService;
    private final BookValidationService bookValidationService;

    public BookService(BookRepository bookRepository,
                       BookLoaderService bookLoaderService,
                       BookValidationService bookValidationService) {
        this.bookRepository = bookRepository;
        this.bookLoaderService = bookLoaderService;
        this.bookValidationService = bookValidationService;
    }

    @PostConstruct
    public void init() {
        if (bookRepository.findAll().isEmpty()) {
            bookRepository.saveAll(bookLoaderService.loadBooks());
        }
    }

    public List<Book> getAllBooks() {
        return bookRepository.findAll();
    }

    public Optional<Book> getBookById(String id) {
        return bookRepository.findById(id);
    }

    public List<Book> searchBooks(String query) {
        return bookRepository.searchByTitle(query);
    }

    public List<Book> filterBooksByDifficulty(Difficulty difficulty) {
        return bookRepository.findByDifficulty(difficulty);
    }

    public BookValidationResult validateBook(Book book) {
        return bookValidationService.validate(book);
    }
}
