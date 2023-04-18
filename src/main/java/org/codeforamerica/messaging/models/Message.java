package org.codeforamerica.messaging.models;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Builder;
import lombok.Value;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import java.time.OffsetDateTime;

@Value
@Builder(toBuilder = true)
@Table(name = "messages")
public class Message {

    @Id
    Long id;
    @NotBlank
    @Pattern(regexp = "\\A1?\\d{10}\\z")
    String to;
    @NotBlank
    String body;
    @NotBlank
    @Pattern(regexp = "\\A1?\\d{10}\\z")
    String from;
    String status;
    String providerMessageId;
    OffsetDateTime providerCreatedAt;
    OffsetDateTime createdAt;
    OffsetDateTime updatedAt;
}
