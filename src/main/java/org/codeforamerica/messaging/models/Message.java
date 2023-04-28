package org.codeforamerica.messaging.models;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.*;
import org.codeforamerica.messaging.utils.RegexPatternStrings;
import org.codeforamerica.messaging.validators.ValidMessage;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import lombok.Builder;
import java.time.OffsetDateTime;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder(toBuilder = true)
@ValidMessage
public class Message {
    @Id
    @GeneratedValue(strategy= GenerationType.IDENTITY)
    private Long id;
    @Pattern(regexp = RegexPatternStrings.PHONE_NUMBER_REGEX)
    String toPhone;
    @Email
    String toEmail;
    @NotBlank
    String body;
    String subject;
    @OneToOne(cascade = CascadeType.REMOVE)
    private SmsMessage smsMessage;
    @OneToOne(cascade = CascadeType.REMOVE)
    private EmailMessage emailMessage;
    @CreationTimestamp
    private OffsetDateTime createdAt;
    @UpdateTimestamp
    private OffsetDateTime updatedAt;
}
