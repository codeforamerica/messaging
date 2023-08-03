package org.codeforamerica.messaging.utils;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;

import java.io.IOException;
import java.io.Reader;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;

public class CSVReader {
    public static final String EMAIL_HEADER = "email";
    public static final String PHONE_HEADER = "phone";
    public static final String LANGUAGE_HEADER = "language";
    public static final String TREATMENT_HEADER = "treatment";
    public static final String ERROR_HEADER = "ERROR";
    private final CSVParser parser;

    public CSVReader(Reader reader) throws IOException {
        var csvFormat = CSVFormat.Builder.create(CSVFormat.RFC4180)
                .setHeader()
                .setSkipHeaderRecord(true)
                .setIgnoreSurroundingSpaces(true)
                .build();
        this.parser =  csvFormat.parse(reader);
    }

    public boolean isValidHeader(Set<String> requiredHeaderNames) {
        return CollectionUtils.isEqualCollection(requiredHeaderNames, parser.getHeaderNames());
    }

    public List<String> getHeaderNames() {
        return parser.getHeaderNames();
    }

    public Stream<Map<String, String>> stream() {
        return parser.stream().map(CSVRecord::toMap);
    }
}
