package org.codeforamerica.messaging.utils;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;

import java.io.IOException;
import java.io.Reader;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

public class CSVReader {
    private final CSVParser parser;

    public CSVReader(Reader reader) throws IOException {
        var csvFormat = CSVFormat.Builder.create(CSVFormat.RFC4180)
                .setHeader()
                .setSkipHeaderRecord(true)
                .setIgnoreSurroundingSpaces(true)
                .build();
        this.parser =  csvFormat.parse(reader);
    }

    public boolean validateHeader(List<String> requiredHeaderNames) {
        return CollectionUtils.isEqualCollection(requiredHeaderNames, parser.getHeaderNames());
    }

    public Stream<Map<String, String>> stream() {
        return parser.stream().map(CSVRecord::toMap);
    }
}
