package org.codeforamerica.messaging.models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
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

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class SmsMessage {

    @Id
    @GeneratedValue(strategy= GenerationType.IDENTITY)
    private Long id;
    @Convert(converter = PhoneNumberConverter.class)
    PhoneNumber toPhone;
    @Convert(converter = PhoneNumberConverter.class)
    PhoneNumber fromPhone;
    @NotBlank
    private String body;
    private String providerMessageId;
    private OffsetDateTime providerCreatedAt;
    @CreationTimestamp
    private OffsetDateTime creationTimestamp;
    @UpdateTimestamp
    private OffsetDateTime updateTimestamp;
    @Version
    private Long version;

    @JsonIgnore
    @OneToOne(mappedBy = "smsMessage")
    private Message message;
    @JdbcTypeCode(SqlTypes.JSON)
    private Map<String, String> providerError;
}
