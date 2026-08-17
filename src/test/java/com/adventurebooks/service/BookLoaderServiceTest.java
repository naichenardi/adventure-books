package com.adventurebooks.service;

import com.adventurebooks.generated.model.*;
import com.adventurebooks.model.entity.Book;
import com.adventurebooks.model.entity.Consequence;
import com.adventurebooks.model.entity.Option;
import com.adventurebooks.model.entity.Section;
import com.adventurebooks.model.enums.ConsequenceType;
import com.adventurebooks.model.enums.Difficulty;
import com.adventurebooks.model.enums.SectionType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.support.ResourcePatternResolver;
import tools.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BookLoaderServiceTest {

    @Mock
    private ObjectMapper objectMapper;

    @Mock
    private ResourcePatternResolver resourcePatternResolver;

    private BookLoaderService service;

    @BeforeEach
    void setUp() {
        service = new BookLoaderService(objectMapper, resourcePatternResolver, "books");
    }

    @Test
    void loadBooksLoadsAllBookFilesFromResolver() throws IOException {
        ByteArrayResource firstResource = createResource("first.json");
        ByteArrayResource secondResource = createResource("second.json");
        when(resourcePatternResolver.getResources("classpath*:books/*.json"))
                .thenReturn(new ByteArrayResource[]{firstResource, secondResource});
        when(objectMapper.readValue(any(byte[].class), eq(BookDto.class)))
                .thenReturn(
                        bookDtoOf("The Prisoner", "Daniel El Fuego", DifficultyDto.HARD, List.of()),
                        bookDtoOf("Dragon Quest", "Anya Stone", DifficultyDto.HARD, List.of())
                );

        List<Book> books = service.loadBooks();

        assertEquals(2, books.size());
        assertEquals("The Prisoner", books.getFirst().getTitle());
        assertEquals("Dragon Quest", books.get(1).getTitle());
    }

    @Test
    void loadBookMapsDtoToEntityGraph() {
        ConsequenceDto consequence = new ConsequenceDto(ConsequenceTypeDto.LOSE_HEALTH);
        consequence.setValue("5");
        consequence.setText("Ouch");

        OptionDto optionA = new OptionDto("Explore the ruined gate", "2");
        OptionDto optionB = new OptionDto("Fight", "3");
        optionB.setConsequence(consequence);

        SectionDto sectionStart = new SectionDto("1", "Start", SectionTypeDto.BEGIN);
        sectionStart.setOptions(List.of(optionA, optionB));
        SectionDto sectionEnd = new SectionDto("2", "End", SectionTypeDto.END);

        ByteArrayResource resource = createResource("dragon-quest.json", "{\"title\":\"Dragon Quest\"}");
        when(objectMapper.readValue(any(byte[].class), eq(BookDto.class)))
                .thenReturn(bookDtoOf("Dragon Quest", "Anya Stone", DifficultyDto.HARD,
                        List.of(sectionStart, sectionEnd)));

        Book book = service.loadBook(resource);

        assertNotNull(book);
        assertEquals("Dragon Quest", book.getTitle());
        assertEquals("Anya Stone", book.getAuthor());
        assertEquals(Difficulty.HARD, book.getDifficulty());
        assertEquals(2, book.getSections().size());

        Section firstSection = book.getSections().getFirst();
        assertEquals("1", firstSection.getId());
        assertEquals(SectionType.BEGIN, firstSection.getType());
        assertEquals(2, firstSection.getOptions().size());

        Option firstOption = firstSection.getOptions().getFirst();
        assertEquals("Explore the ruined gate", firstOption.getDescription());
        assertEquals("2", firstOption.getGotoId());
        assertNull(firstOption.getConsequence());

        Option option = firstSection.getOptions().get(1);
        Consequence domainConsequence = option.getConsequence();
        assertNotNull(domainConsequence);
        assertEquals(ConsequenceType.LOSE_HEALTH, domainConsequence.getType());
        assertEquals(Integer.valueOf(5), domainConsequence.getValue());
        assertEquals("Ouch", domainConsequence.getText());
    }

    @Test
    void loadBookThrowsWhenJsonIsEmpty() {
        ByteArrayResource resource = createResource("empty.json", "   ");

        IllegalStateException exception = assertThrows(
                IllegalStateException.class,
                () -> service.loadBook(resource)
        );

        assertTrue(exception.getMessage().contains("Book resource is empty: empty.json"));
    }

    @Test
    void loadBookThrowsWhenJsonFormatIsInvalid() {
        ByteArrayResource resource = createResource("broken.json", "{");
        when(objectMapper.readValue(any(byte[].class), eq(BookDto.class)))
                .thenThrow(new RuntimeException("invalid json"));

        IllegalStateException exception = assertThrows(
                IllegalStateException.class,
                () -> service.loadBook(resource)
        );

        assertTrue(exception.getMessage().contains("Invalid JSON format in book resource: broken.json"));
    }

    @Test
    void loadBooksThrowsWhenResolverFails() throws IOException {
        when(resourcePatternResolver.getResources("classpath*:books/*.json"))
                .thenThrow(new IOException("resolver failure"));

        IllegalStateException exception = assertThrows(IllegalStateException.class, service::loadBooks);

        assertTrue(exception.getMessage().contains("Unable to load adventure books from path: books"));
    }

    private BookDto bookDtoOf(String title, String author, DifficultyDto difficulty, List<SectionDto> sections) {
        BookDto dto = new BookDto(title);
        dto.setAuthor(author);
        dto.setDifficulty(difficulty);
        dto.setSections(sections);
        return dto;
    }

    private ByteArrayResource createResource(String filename) {
        return createResource(filename, "{}");
    }

    private ByteArrayResource createResource(String filename, String content) {
        return new ByteArrayResource(content.getBytes(StandardCharsets.UTF_8)) {
            @Override
            public String getFilename() {
                return filename;
            }
        };
    }
}