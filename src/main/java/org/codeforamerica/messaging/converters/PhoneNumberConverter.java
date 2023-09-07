package org.codeforamerica.messaging.converters;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import org.codeforamerica.messaging.models.PhoneNumber;

@Converter
public class PhoneNumberConverter implements AttributeConverter<PhoneNumber, String> {
    @Override
    public String convertToDatabaseColumn(PhoneNumber attribute) {
        return attribute == null ? null : attribute.getNumber();
    }

    @Override
    public PhoneNumber convertToEntityAttribute(String dbData) {
        return dbData == null ? null : PhoneNumber.valueOf(dbData);
    }
}
