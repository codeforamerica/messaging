package org.codeforamerica.messaging.models;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.codeforamerica.messaging.validators.ValidMessageable;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.OffsetDateTime;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@ValidMessageable
public class Message implements Messageable {
    @Id
    @GeneratedValue(strategy= GenerationType.IDENTITY)
    private Long id;
    @ManyToOne
    @NotNull
    TemplateVariant templateVariant;
    @ManyToOne
    MessageBatch messageBatch;
    @OneToOne(cascade = CascadeType.ALL)
    private SmsMessage smsMessage;
    @OneToOne(cascade = CascadeType.ALL)
    private EmailMessage emailMessage;
    @CreationTimestamp
    private OffsetDateTime creationTimestamp;
    @UpdateTimestamp
    private OffsetDateTime updateTimestamp;

    public String getTemplateName() {
        return templateVariant.getTemplateName();
    }

    public String getToPhone() {
        return this.smsMessage == null ? null : this.smsMessage.getToPhone();
    }

    public String getToEmail() {
        return this.emailMessage == null ? null : this.emailMessage.getToEmail();
    }

    public String getSubject() {
        return this.emailMessage == null ? null : this.emailMessage.getSubject();
    }

    public String getEmailBody() {
        return this.emailMessage == null ? null : this.emailMessage.getBody();
    }

    public String getSmsBody() {
        return this.smsMessage == null ? null : this.smsMessage.getBody();
    }

    public boolean needToSendEmail() {
        return emailMessage != null && emailMessage.getStatus().equals("scheduled");
    }

    public boolean needToSendSms() {
        return smsMessage != null && smsMessage.getStatus().equals("scheduled");
    }

    public String getStatus() {
        return needToSendEmail() || needToSendSms() ? "pending" : "completed";
    }
}
