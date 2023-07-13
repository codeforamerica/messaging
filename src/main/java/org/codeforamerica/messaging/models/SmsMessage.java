package org.codeforamerica.messaging.models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.*;
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
    private OffsetDateTime creationTimestamp;
    @UpdateTimestamp
    private OffsetDateTime updateTimestamp;

    @JsonIgnore
    @ToString.Exclude
    @OneToOne(mappedBy = "smsMessage")
    private Message message;
}


