package com.adventurebooks.service;

import com.adventurebooks.model.dto.BookDto;
import com.adventurebooks.model.dto.ConsequenceDto;
import com.adventurebooks.model.dto.OptionDto;
import com.adventurebooks.model.dto.SectionDto;
import com.adventurebooks.model.entity.Book;
import com.adventurebooks.model.entity.Consequence;
import com.adventurebooks.model.entity.Option;
import com.adventurebooks.model.entity.Section;
import com.adventurebooks.model.enums.ConsequenceType;
import com.adventurebooks.model.enums.Difficulty;
import com.adventurebooks.model.enums.SectionType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import tools.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.io.InputStream;
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
        when(objectMapper.readValue(any(InputStream.class), eq(BookDto.class)))
                .thenReturn(
                        new BookDto("The Prisoner", "Daniel El Fuego", Difficulty.HARD, List.of()),
                        new BookDto("Dragon Quest", "Anya Stone", Difficulty.HARD, List.of())
                );

        List<Book> books = service.loadBooks();

        assertEquals(2, books.size());
        assertEquals("The Prisoner", books.getFirst().getTitle());
        assertEquals("Dragon Quest", books.get(1).getTitle());
    }

    @Test
    void loadBookMapsDtoToEntityGraph() {
        ByteArrayResource resource = createResource("dragon-quest.json");
        when(objectMapper.readValue(any(InputStream.class), eq(BookDto.class)))
                .thenReturn(new BookDto(
                        "Dragon Quest",
                        "Anya Stone",
                        Difficulty.HARD,
                        List.of(
                                new SectionDto(
                                        "1",
                                        "Start",
                                        SectionType.BEGIN,
                                        List.of(
                                                new OptionDto("Explore the ruined gate", "2", null),
                                                new OptionDto(
                                                        "Fight",
                                                        "3",
                                                        new ConsequenceDto(ConsequenceType.LOSE_HEALTH, "5", "Ouch")
                                                )
                                        )
                                ),
                                new SectionDto("2", "End", SectionType.END, null)
                        )
                ));

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
        Consequence consequence = option.getConsequence();
        assertNotNull(consequence);
        assertEquals(ConsequenceType.LOSE_HEALTH, consequence.getType());
        assertEquals(Integer.valueOf(5), consequence.getValue());
        assertEquals("Ouch", consequence.getText());
    }

    @Test
    void loadBookThrowsWhenMapperFails() {
        ByteArrayResource resource = createResource("broken.json");
        when(objectMapper.readValue(any(InputStream.class), eq(BookDto.class)))
                .thenThrow(new RuntimeException("boom"));

        IllegalStateException exception = assertThrows(
                IllegalStateException.class,
                () -> service.loadBook(resource)
        );

        assertTrue(exception.getMessage().contains("Unable to parse book resource"));
    }

    @Test
    void loadBooksThrowsWhenResolverFails() throws IOException {
        when(resourcePatternResolver.getResources("classpath*:books/*.json"))
                .thenThrow(new IOException("resolver failure"));

        IllegalStateException exception = assertThrows(IllegalStateException.class, service::loadBooks);

        assertTrue(exception.getMessage().contains("Unable to load adventure books from path: books"));
    }

    private ByteArrayResource createResource(String filename) {
        return new ByteArrayResource("{}".getBytes(StandardCharsets.UTF_8)) {
            @Override
            public String getFilename() {
                return filename;
            }
        };
    }
}
