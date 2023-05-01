package org.codeforamerica.messaging.models;

import com.fasterxml.jackson.annotation.JsonIgnore;
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
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class SmsMessage {

    @Id
    @GeneratedValue(strategy= GenerationType.IDENTITY)
    private Long id;
    @NotBlank
    @Pattern(regexp = RegexPatternStrings.PHONE_NUMBER_REGEX)
    private String toPhone;
    @NotBlank
    private String body;
    @NotBlank
    @Pattern(regexp = RegexPatternStrings.PHONE_NUMBER_REGEX)
    private String fromPhone;
    private String status;
    private String providerMessageId;
    private OffsetDateTime providerCreatedAt;
    @CreationTimestamp
    private OffsetDateTime createdAt;
    @UpdateTimestamp
    private OffsetDateTime updatedAt;

    @JsonIgnore
    @OneToOne(mappedBy = "smsMessage")
    private Message message;
}


