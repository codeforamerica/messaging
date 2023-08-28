package org.codeforamerica.messaging.models;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.codeforamerica.messaging.converters.PhoneNumberConverter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.OffsetDateTime;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class SmsSubscription {
    @Id
    @GeneratedValue(strategy= GenerationType.IDENTITY)
    private Long id;
    @NotNull
    @Convert(converter = PhoneNumberConverter.class)
    PhoneNumber phoneNumber;
    private boolean unsubscribed;
    private boolean sourceInternal;
    @CreationTimestamp
    private OffsetDateTime creationTimestamp;
    @UpdateTimestamp
    private OffsetDateTime updateTimestamp;
}
