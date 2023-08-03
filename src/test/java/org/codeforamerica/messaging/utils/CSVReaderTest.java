package org.codeforamerica.messaging.utils;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.StringReader;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

class CSVReaderTest {

    String testCSV = """
            phone, email, language, color, score
            1234567890,bar@example.org, en, grey, 23
            8885551212,foo@example.com, es, black, 42
            """;

    @Test
    public void whenRequiredHeadersPresent_ThenSucceedsValidation() throws IOException {
        CSVReader reader = new CSVReader(new StringReader(testCSV));

        assertTrue(reader.isValidHeader(Set.of("phone", "email", "language", "color", "score")));
    }

    @Test
    public void whenRequiredHeadersAbsent_ThenFailsValidation() throws IOException {
        CSVReader reader = new CSVReader(new StringReader(testCSV));

        assertFalse(reader.isValidHeader(Set.of("phone", "language", "color", "score")));
    }

    @Test
    public void whenParsed_ThenReturnsCorrectCSV() throws IOException {
        CSVReader reader = new CSVReader(new StringReader(testCSV));

        List<String> result = reader.stream().map((r) -> r.get("phone")).collect(Collectors.toList());

        assertEquals(result, List.of("1234567890", "8885551212"));
    }
}
