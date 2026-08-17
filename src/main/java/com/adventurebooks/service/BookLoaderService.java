package com.adventurebooks.service;

import com.adventurebooks.model.dto.BookDto;
import com.adventurebooks.model.dto.SectionDto;
import com.adventurebooks.model.dto.OptionDto;
import com.adventurebooks.model.dto.ConsequenceDto;
import com.adventurebooks.model.entity.Book;
import com.adventurebooks.model.entity.Option;
import com.adventurebooks.model.entity.Section;
import com.adventurebooks.model.entity.Consequence;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.stereotype.Service;
import tools.jackson.databind.ObjectMapper;

import java.util.ArrayList;
import java.util.List;

@Service
public class BookLoaderService {

    private final ObjectMapper objectMapper;
    private final String booksPath;
    private final PathMatchingResourcePatternResolver resourcePatternResolver;

    public BookLoaderService(ObjectMapper objectMapper, ApplicationContext applicationContext,
                            @Value("${adventure-books.books-path:books}") String booksPath) {
        this.objectMapper = objectMapper;
        this.booksPath = booksPath;
        this.resourcePatternResolver = new PathMatchingResourcePatternResolver(applicationContext);
    }

    public List<Book> loadBooks() {
        try {
            Resource[] resources = resourcePatternResolver.getResources("classpath*:" + booksPath + "/*.json");
            List<Book> books = new ArrayList<>();

            for (Resource resource : resources) {
                if (resource != null && resource.exists()) {
                    books.add(loadBook(resource));
                }
            }

            return books;
        } catch (Exception e) {
            throw new IllegalStateException("Unable to load adventure books from path: " + booksPath, e);
        }
    }

    public Book loadBook(Resource resource) {
        try {
            BookDto bookDto = objectMapper.readValue(resource.getInputStream(), BookDto.class);
            return mapToBook(bookDto);
        } catch (Exception e) {
            throw new IllegalStateException("Unable to parse book resource: " + resource.getFilename(), e);
        }
    }

    private Book mapToBook(BookDto bookDto) {
        List<Section> sections = new ArrayList<>();
        if (bookDto.sections() != null) {
            for (SectionDto sectionDto : bookDto.sections()) {
                sections.add(mapToSection(sectionDto));
            }
        }

        return new Book(bookDto.title(), bookDto.author(), bookDto.difficulty(), sections);
    }

    private Section mapToSection(SectionDto sectionDto) {
        List<Option> options = new ArrayList<>();
        if (sectionDto.options() != null) {
            for (OptionDto optionDto : sectionDto.options()) {
                options.add(mapToOption(optionDto));
            }
        }

        return new Section(sectionDto.id(), sectionDto.text(), sectionDto.type(), options);
    }

    private Option mapToOption(OptionDto optionDto) {
        Consequence consequence = null;
        if (optionDto.consequence() != null) {
            ConsequenceDto consequenceDto = optionDto.consequence();
            Integer value = null;
            if (consequenceDto.value() != null && !consequenceDto.value().isBlank()) {
                value = Integer.parseInt(consequenceDto.value());
            }
            consequence = new Consequence(consequenceDto.type(), value, consequenceDto.text());
        }

        return new Option(optionDto.description(), optionDto.gotoId(), consequence);
    }
}
