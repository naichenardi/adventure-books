package com.adventurebooks;

import com.adventurebooks.service.BookLoaderService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;

import java.util.List;

@SpringBootTest(classes = {AdventureBooksApplication.class, AdventureBooksApplicationTests.TestConfig.class})
class AdventureBooksApplicationTests {

    @Test
    void contextLoads() {
    }

    @TestConfiguration
    static class TestConfig {

        @Bean
        @Primary
        BookLoaderService bookLoaderService() {
            BookLoaderService loaderService = Mockito.mock(BookLoaderService.class);
            Mockito.when(loaderService.loadBooks()).thenReturn(List.of());
            return loaderService;
        }
    }

}
