package com.adventurebooks.controller;

import com.adventurebooks.generated.api.BookApi;
import com.adventurebooks.generated.model.*;
import com.adventurebooks.service.BookService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.server.ResponseStatusException;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Controller
public class BookController implements BookApi {

    private final BookService bookService;

    public BookController(BookService bookService) {
        this.bookService = bookService;
    }

    @Override
    public ResponseEntity<List<BookDto>> listBooks(String query, DifficultyDto difficulty) {
        List<com.adventurebooks.model.entity.Book> books;
        if (query != null && !query.isBlank()) {
            books = bookService.searchBooks(query);
        } else {
            books = bookService.getAllBooks();
        }

        if (difficulty != null) {
            com.adventurebooks.model.enums.Difficulty domainDifficulty =
                    com.adventurebooks.model.enums.Difficulty.valueOf(difficulty.getValue());
            books = books.stream()
                    .filter(book -> book.getDifficulty() == domainDifficulty)
                    .collect(Collectors.toList());
        }

        List<BookDto> payload = books.stream()
                .map(this::toApiBook)
                .collect(Collectors.toList());
        return ResponseEntity.ok(payload);
    }

    @Override
    public ResponseEntity<BookDto> getBookById(String id) {
        com.adventurebooks.model.entity.Book book = findBookOrThrow(id);
        return ResponseEntity.ok(toApiBook(book));
    }

    @Override
    public ResponseEntity<BookValidationResultDto> validateBook(String id) {
        com.adventurebooks.model.entity.Book book = findBookOrThrow(id);
        com.adventurebooks.validation.BookValidationResult result = bookService.validateBook(book);
        BookValidationResultDto payload = new BookValidationResultDto(result.valid());
        payload.setErrors(result.errors());
        return ResponseEntity.ok(payload);
    }

    private com.adventurebooks.model.entity.Book findBookOrThrow(String id) {
        return bookService.getBookById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Book not found: " + id));
    }

    private BookDto toApiBook(com.adventurebooks.model.entity.Book domainBook) {
        BookDto apiBook = new BookDto(domainBook.getTitle());
        apiBook.setId(domainBook.getTitle());
        apiBook.setAuthor(domainBook.getAuthor());

        if (domainBook.getDifficulty() != null) {
            apiBook.setDifficulty(DifficultyDto.fromValue(domainBook.getDifficulty().name()));
        }

        List<SectionDto> sections =
                domainBook.getSections() == null
                        ? Collections.emptyList()
                        : domainBook.getSections().stream().map(this::toApiSection).collect(Collectors.toList());
        apiBook.setSections(sections);
        return apiBook;
    }

    private SectionDto toApiSection(com.adventurebooks.model.entity.Section domainSection) {
        SectionDto apiSection = new SectionDto(
                domainSection.getId(),
                domainSection.getText(),
                SectionTypeDto.fromValue(domainSection.getType().name())
        );

        List<OptionDto> options =
                domainSection.getOptions() == null
                        ? Collections.emptyList()
                        : domainSection.getOptions().stream().map(this::toApiOption).collect(Collectors.toList());
        apiSection.setOptions(options);
        return apiSection;
    }

    private OptionDto toApiOption(com.adventurebooks.model.entity.Option domainOption) {
        OptionDto apiOption = new OptionDto(domainOption.getDescription(), domainOption.getGotoId());
        apiOption.setConsequence(toApiConsequence(domainOption.getConsequence()));
        return apiOption;
    }

    private ConsequenceDto toApiConsequence(com.adventurebooks.model.entity.Consequence domainConsequence) {
        if (domainConsequence == null || domainConsequence.getType() == null) {
            return null;
        }

        ConsequenceDto apiConsequence = new ConsequenceDto(
                ConsequenceTypeDto.fromValue(domainConsequence.getType().name())
        );
        if (domainConsequence.getValue() != null) {
            apiConsequence.setValue(String.valueOf(domainConsequence.getValue()));
        }
        apiConsequence.setText(domainConsequence.getText());
        return apiConsequence;
    }
}