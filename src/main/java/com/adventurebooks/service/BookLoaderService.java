package com.adventurebooks.service;

import com.adventurebooks.generated.model.BookDto;
import com.adventurebooks.generated.model.ConsequenceDto;
import com.adventurebooks.generated.model.OptionDto;
import com.adventurebooks.generated.model.SectionDto;
import com.adventurebooks.model.entity.Book;
import com.adventurebooks.model.entity.Consequence;
import com.adventurebooks.model.entity.Option;
import com.adventurebooks.model.entity.Section;
import com.adventurebooks.model.enums.ConsequenceType;
import com.adventurebooks.model.enums.Difficulty;
import com.adventurebooks.model.enums.SectionType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.stereotype.Service;
import tools.jackson.databind.ObjectMapper;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

@Service
public class BookLoaderService {

    private static final Logger log = LoggerFactory.getLogger(BookLoaderService.class);
    private final ObjectMapper objectMapper;
    private final String booksPath;
    private final ResourcePatternResolver resourcePatternResolver;

    public BookLoaderService(
            ObjectMapper objectMapper,
            ResourcePatternResolver resourcePatternResolver,
            @Value("${adventure-books.books-path:books}") String booksPath
    ) {
        this.objectMapper = objectMapper;
        this.booksPath = booksPath;
        this.resourcePatternResolver = resourcePatternResolver;
    }

    public List<Book> loadBooks() {
        try {
            Resource[] resources = resourcePatternResolver.getResources("classpath*:" + booksPath + "/*.json");
            List<Book> books = new ArrayList<>();

            for (Resource resource : resources) {
                if (resource != null && resource.exists()) {
                    try {
                        Book book = loadBook(resource);
                        books.add(book);
                    } catch (IllegalStateException | IllegalArgumentException e) {
                        log.warn("X => Skipping book resource: {}", e.getMessage());
                    }
                }
            }

            return books;
        } catch (Exception e) {
            throw new IllegalStateException("Unable to load adventure books from path: " + booksPath, e);
        }
    }

    public Book loadBook(Resource resource) {
        try {
            byte[] content = resource.getInputStream().readAllBytes();
            String json = new String(content, StandardCharsets.UTF_8);
            String normalizedJson = json.replace("\uFEFF", "").trim();
            if (normalizedJson.isEmpty()) {
                throw new IllegalStateException("Book resource is empty: " + resource.getFilename());
            }

            BookDto bookDto;
            try {
                bookDto = objectMapper.readValue(content, BookDto.class);
                log.info("=> Book loaded: {}", resource.getFilename());
            } catch (Exception exception) {
                throw new IllegalStateException("Invalid JSON format in book resource: " + resource.getFilename(), exception);
            }

            return mapToBook(bookDto);
        } catch (IllegalStateException | IllegalArgumentException exception) {
            // Client-caused problems (bad enum values, bad numeric fields, empty/invalid JSON) are
            // rethrown as-is so callers like BookService.uploadBook can surface them as 400s, rather
            // than being masked here as a generic 500.
            throw exception;
        } catch (Exception e) {
            throw new IllegalStateException("Unable to parse book resource: " + resource.getFilename(), e);
        }
    }

    private Book mapToBook(BookDto bookDto) {
        List<Section> sections = new ArrayList<>();
        if (bookDto.getSections() != null) {
            for (SectionDto sectionDto : bookDto.getSections()) {
                sections.add(mapToSection(sectionDto));
            }
        }

        Difficulty difficulty = bookDto.getDifficulty() != null
                ? Difficulty.valueOf(bookDto.getDifficulty().getValue())
                : null;

        return new Book(bookDto.getTitle(), bookDto.getAuthor(), difficulty, sections);
    }

    private Section mapToSection(SectionDto sectionDto) {
        List<Option> options = new ArrayList<>();
        if (sectionDto.getOptions() != null) {
            for (OptionDto optionDto : sectionDto.getOptions()) {
                options.add(mapToOption(optionDto));
            }
        }

        SectionType type = SectionType.valueOf(sectionDto.getType().getValue());
        return new Section(sectionDto.getId(), sectionDto.getText(), type, options);
    }

    private Option mapToOption(OptionDto optionDto) {
        Consequence consequence = null;
        if (optionDto.getConsequence() != null) {
            ConsequenceDto consequenceDto = optionDto.getConsequence();
            Integer value = null;
            if (consequenceDto.getValue() != null && !consequenceDto.getValue().isBlank()) {
                try {
                    value = Integer.parseInt(consequenceDto.getValue());
                } catch (NumberFormatException e) {
                    throw new IllegalArgumentException(
                            "Invalid consequence value '" + consequenceDto.getValue() + "': must be a whole number.", e);
                }
            }
            ConsequenceType type = ConsequenceType.valueOf(consequenceDto.getType().getValue());
            consequence = new Consequence(type, value, consequenceDto.getText());
        }

        return new Option(optionDto.getDescription(), optionDto.getGotoId(), consequence);
    }
}
