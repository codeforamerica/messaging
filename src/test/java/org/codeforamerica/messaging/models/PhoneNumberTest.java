package org.codeforamerica.messaging.models;

import org.codeforamerica.messaging.exceptions.PhoneNumberParsingException;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

class PhoneNumberTest {
    @ParameterizedTest
    @ValueSource(strings = { "1234567890", "11234567890", "+11234567890" })
    public void acceptsValidPhoneNumbers(String candidate) {
        assertDoesNotThrow(() -> PhoneNumber.valueOf(candidate));
    }

    @ParameterizedTest
    @ValueSource(strings = {"123456A7890", "123456789012"})
    public void rejectsInValidPhoneNumbers(String candidate) {
        assertThrows(PhoneNumberParsingException.class, () -> PhoneNumber.valueOf(candidate));
    }
}
