package org.codeforamerica.messaging.models;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.codeforamerica.messaging.converters.PhoneNumberConverter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.type.SqlTypes;

import java.time.OffsetDateTime;
import java.util.Map;

import static org.codeforamerica.messaging.models.TemplateVariant.DEFAULT_LANGUAGE;
import static org.codeforamerica.messaging.models.TemplateVariant.DEFAULT_TREATMENT;
import static org.codeforamerica.messaging.utils.CSVReader.LANGUAGE_HEADER;
import static org.codeforamerica.messaging.utils.CSVReader.TREATMENT_HEADER;

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
    @Convert(converter = PhoneNumberConverter.class)
    PhoneNumber toPhone;
    @Email
    String toEmail;
    @ManyToOne
    MessageBatch messageBatch;

    @OneToOne(cascade = CascadeType.REMOVE)
    private SmsMessage smsMessage;
    private String rawSmsStatus;
    @Enumerated(EnumType.STRING)
    private MessageStatus smsStatus;
    private String smsErrorMessage;

    @OneToOne(cascade = CascadeType.REMOVE)
    private EmailMessage emailMessage;
    private String rawEmailStatus;
    @Enumerated(EnumType.STRING)
    private MessageStatus emailStatus;
    private String emailErrorMessage;

    @CreationTimestamp
    private OffsetDateTime creationTimestamp;
    @UpdateTimestamp
    private OffsetDateTime updateTimestamp;
    @Version
    private Long version;

    public String getTemplateName() {
        return templateVariant.getTemplateName();
    }

    public boolean needToSendEmail() {
        return toEmail != null && emailMessage == null && emailStatus == null;
    }

    public boolean needToSendSms() {
        return toPhone != null && smsMessage == null && smsStatus == null;
    }

    public String getLanguage() {
        if (this.getTemplateParams() != null && this.getTemplateParams().get(LANGUAGE_HEADER) != null) {
            return this.getTemplateParams().get(LANGUAGE_HEADER);
        }
        return DEFAULT_LANGUAGE;
    }

    public String getTreatment() {
        if (this.getTemplateParams() != null && this.getTemplateParams().get(TREATMENT_HEADER) != null) {
            return this.getTemplateParams().get(TREATMENT_HEADER);
        }
        return DEFAULT_TREATMENT;
    }

}
