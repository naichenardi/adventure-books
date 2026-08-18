package com.adventurebooks.controller;

import com.adventurebooks.generated.api.BookApi;
import com.adventurebooks.generated.model.BookDto;
import com.adventurebooks.generated.model.BookValidationResultDto;
import com.adventurebooks.generated.model.DifficultyDto;
import com.adventurebooks.mapper.BookMapper;
import com.adventurebooks.model.entity.Book;
import com.adventurebooks.model.enums.Difficulty;
import com.adventurebooks.service.BookService;
import com.adventurebooks.validation.BookValidationResult;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.stream.Collectors;

@Controller
public class BookController implements BookApi {

    private final BookService bookService;
    private final BookMapper bookMapper;

    public BookController(BookService bookService, BookMapper bookMapper) {
        this.bookService = bookService;
        this.bookMapper = bookMapper;
    }

    @Override
    public ResponseEntity<List<BookDto>> listBooks(String query, DifficultyDto difficulty) {
        List<Book> books;
        if (query != null && !query.isBlank()) {
            books = bookService.searchBooks(query);
        } else {
            books = bookService.getAllBooks();
        }

        if (difficulty != null) {
            Difficulty domainDifficulty = Difficulty.valueOf(difficulty.getValue());
            books = books.stream()
                    .filter(book -> book.getDifficulty() == domainDifficulty)
                    .collect(Collectors.toList());
        }
        if (books.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }
        return ResponseEntity.ok(books.stream().map(bookMapper::toDto).collect(Collectors.toList()));
    }

    @Override
    public ResponseEntity<BookDto> getBookById(Long id) {
        return ResponseEntity.ok(bookMapper.toDto(findBookOrThrow(id)));
    }

    @Override
    public ResponseEntity<BookValidationResultDto> validateBook(Long id) {
        Book book = findBookOrThrow(id);
        BookValidationResult result = bookService.validateBook(book);
        BookValidationResultDto payload = new BookValidationResultDto(result.valid());
        payload.setErrors(result.errors());
        return ResponseEntity.ok(payload);
    }

    @Override
    public ResponseEntity<BookDto> uploadBook(MultipartFile file) {
        return ResponseEntity.status(HttpStatus.CREATED).body(bookMapper.toDto(bookService.uploadBook(file)));
    }

    private Book findBookOrThrow(Long id) {
        return bookService.getBookById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Book not found: " + id));
    }
}
