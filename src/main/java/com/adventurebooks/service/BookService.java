package com.adventurebooks.service;

import com.adventurebooks.model.entity.Book;
import com.adventurebooks.model.enums.Difficulty;
import com.adventurebooks.repository.BookRepository;
import com.adventurebooks.validation.BookValidationResult;
import com.adventurebooks.validation.BookValidationService;
import jakarta.annotation.PostConstruct;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
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

    public Optional<Book> getBookById(Long id) {
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

    public Book uploadBook(MultipartFile file) {
        byte[] bytes;
        try {
            bytes = file.getBytes();
        } catch (IOException e) {
            throw new IllegalArgumentException("Failed to read uploaded file", e);
        }

        ByteArrayResource resource = new ByteArrayResource(bytes) {
            @Override
            public String getFilename() {
                return file.getOriginalFilename();
            }
        };

        Book book = bookLoaderService.loadBook(resource);
        if (book == null) {
            throw new IllegalArgumentException("Uploaded file is empty or invalid");
        }

        BookValidationResult result = bookValidationService.validate(book);
        if (!result.valid()) {
            throw new IllegalArgumentException("Book validation failed: " + String.join(", ", result.errors()));
        }

        return bookRepository.save(book);
    }
}
