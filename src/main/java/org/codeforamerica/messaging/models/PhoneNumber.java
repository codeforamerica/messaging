package org.codeforamerica.messaging.models;

import lombok.Value;
import org.codeforamerica.messaging.exceptions.PhoneNumberParsingException;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Value
public class PhoneNumber {
    private static final Pattern phoneNumberPattern = Pattern.compile("^\\+?1?(?<normalizedPhone>\\d{10})$");

    String number;

    public PhoneNumber(String number) {
        this.number = validateAndNormalize(number);
    }

    private static String validateAndNormalize(String number) {
        Matcher matcher = phoneNumberPattern.matcher(number);
        if (!matcher.matches()) {
            throw new PhoneNumberParsingException("The phone number is not valid: " + number);
        }
        return matcher.group("normalizedPhone");
    }

    public static PhoneNumber valueOf(String number) {
        return new PhoneNumber(number);
    }

    public String toString() {
        return number;
    }
}
