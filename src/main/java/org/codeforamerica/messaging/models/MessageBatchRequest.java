package org.codeforamerica.messaging.models;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Value;
import org.springframework.web.multipart.MultipartFile;

import java.time.OffsetDateTime;

@Value
@AllArgsConstructor
@Builder
public class MessageBatchRequest {
    @NotBlank
    String templateName;
    MultipartFile recipients;
    @Future
    OffsetDateTime sendAt;
}
