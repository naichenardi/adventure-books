package com.adventurebooks.exception;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class GlobalExceptionHandlerTest {

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(new TestController())
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    void handlesIllegalArgumentExceptionAsBadRequest() throws Exception {
        mockMvc.perform(get("/test/bad-request"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Bad Request"))
                .andExpect(jsonPath("$.message").value("Invalid input."))
                .andExpect(jsonPath("$.path").value("/test/bad-request"));
    }

    @Test
    void handlesIllegalStateExceptionAsInternalServerError() throws Exception {
        mockMvc.perform(get("/test/state-error"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.status").value(500))
                .andExpect(jsonPath("$.error").value("Internal Server Error"))
                .andExpect(jsonPath("$.message").value("Application is in an invalid state."))
                .andExpect(jsonPath("$.path").value("/test/state-error"));
    }

    @Test
    void handlesUnexpectedExceptionAsInternalServerError() throws Exception {
        mockMvc.perform(get("/test/unexpected-error"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.status").value(500))
                .andExpect(jsonPath("$.error").value("Internal Server Error"))
                .andExpect(jsonPath("$.message").value("Unexpected failure."))
                .andExpect(jsonPath("$.path").value("/test/unexpected-error"));
    }

    @RestController
    static class TestController {

        @GetMapping("/test/bad-request")
        String badRequest() {
            throw new IllegalArgumentException("Invalid input.");
        }

        @GetMapping("/test/state-error")
        String stateError() {
            throw new IllegalStateException("Application is in an invalid state.");
        }

        @GetMapping("/test/unexpected-error")
        String unexpectedError() {
            throw new RuntimeException("Unexpected failure.");
        }
    }
}
