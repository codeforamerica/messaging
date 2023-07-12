package org.codeforamerica.messaging.models;

public interface Messageable {
    String getSubject();
    String getEmailBody();
    String getSmsBody();
}
