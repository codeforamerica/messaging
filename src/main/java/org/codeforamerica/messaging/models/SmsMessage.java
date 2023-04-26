package org.codeforamerica.messaging.models;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.codeforamerica.messaging.utils.RegexPatternStrings;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.OffsetDateTime;

@Entity
@Table(name="sms_messages")
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder(toBuilder = true)
public class SmsMessage {

    @Id
    @GeneratedValue(strategy= GenerationType.IDENTITY)
    private Long id;
    @NotBlank
    @Pattern(regexp = RegexPatternStrings.PHONE_NUMBER_REGEX)
    private String toNumber;
    @NotBlank
    private String body;
    @NotBlank
    @Pattern(regexp = RegexPatternStrings.PHONE_NUMBER_REGEX)
    private String fromNumber;
    private String status;
    private String providerMessageId;
    private OffsetDateTime providerCreatedAt;
    @CreationTimestamp
    private OffsetDateTime createdAt;
    @UpdateTimestamp
    private OffsetDateTime updatedAt;
}


