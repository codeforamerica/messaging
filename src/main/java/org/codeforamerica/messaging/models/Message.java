package org.codeforamerica.messaging.models;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.codeforamerica.messaging.utils.RegexPatternStrings;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.type.SqlTypes;

import java.time.OffsetDateTime;
import java.util.Map;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Message {
    @Id
    @GeneratedValue(strategy= GenerationType.IDENTITY)
    private Long id;
    @ManyToOne
    @NotNull
    TemplateVariant templateVariant;
    @JdbcTypeCode(SqlTypes.JSON)
    private Map<String, String> templateParams;
    @Pattern(regexp = RegexPatternStrings.PHONE_NUMBER_REGEX)
    String toPhone;
    @Email
    String toEmail;
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
