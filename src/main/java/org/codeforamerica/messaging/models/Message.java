package org.codeforamerica.messaging.models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
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
public class Message {
    @Id
    @GeneratedValue(strategy= GenerationType.IDENTITY)
    private Long id;
    @ManyToOne(cascade = CascadeType.MERGE)
    @NotNull
    TemplateVariant templateVariant;
    @Pattern(regexp = RegexPatternStrings.PHONE_NUMBER_REGEX)
    String toPhone;
    @Email
    String toEmail;
    @NotBlank
    String body;
    String subject;
    @ManyToOne
    MessageBatch messageBatch;
    @OneToOne(cascade = CascadeType.REMOVE)
    private SmsMessage smsMessage;
    @OneToOne(cascade = CascadeType.REMOVE)
    private EmailMessage emailMessage;
    @CreationTimestamp
    private OffsetDateTime creationTimestamp;
    @UpdateTimestamp
    private OffsetDateTime updateTimestamp;

    public String getTemplateName() {
        return templateVariant.getTemplateName();
    }

    public boolean needToSendEmail() {
        return toEmail != null && emailMessage == null;
    }

    public boolean needToSendSms() {
        return toPhone != null && smsMessage == null;
    }

    public String getStatus() {
        return needToSendEmail() || needToSendSms() ? "pending" : "completed";
    }
}
