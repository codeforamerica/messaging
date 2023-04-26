package org.codeforamerica.messaging.providers.mailgun;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.codeforamerica.messaging.models.EmailMessage;
import org.codeforamerica.messaging.repositories.EmailMessageRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.Map;

@RestController
@RequestMapping("/mailgun_callbacks")
@Slf4j
public class MailgunCallbackController {

    private final EmailMessageRepository emailMessageRepository;

    public MailgunCallbackController(EmailMessageRepository emailMessageRepository) {
        this.emailMessageRepository = emailMessageRepository;
    }

    @PostMapping(path = "/status")
    public ResponseEntity<Object> updateStatus(@RequestParam Map<String, String> mailgunCallback) {
        log.info("Received mailgun callback: " + mailgunCallback);
        EmailMessage emailMessage = emailMessageRepository.findFirstByProviderMessageId(mailgunCallback.get("id"));

        String json = mailgunCallback.get("event-data");
        ObjectMapper mapper = new ObjectMapper();
        Map<String, String> map = null;
        try {

            // convert JSON string to Map
//            Map<String, String> map = mapper.readValue(json, Map.class);

            // it works
            map = mapper.readValue(json, new TypeReference<>() {
            });

            System.out.println(map);

        } catch (IOException e) {
            e.printStackTrace();
        }

        EmailMessage updatedEmailMessage = emailMessage.toBuilder()
                .status(map.get("event"))
                .build();
        emailMessageRepository.save(updatedEmailMessage);
        return ResponseEntity.ok().build();
    }
}

//{
//        “signature”:
//        {
//        "timestamp": "1529006854",
//        "token": "a8ce0edb2dd8301dee6c2405235584e45aa91d1e9f979f3de0",
//        "signature": "d2271d12299f6592d9d44cd9d250f0704e4674c30d79d07c47a66f95ce71cf55"
//        }
//        “event-data”:
//        {
//        "event": "opened",
//        "timestamp": 1529006854.329574,
//        "id": "DACSsAdVSeGpLid7TN03WA",
//        // ...
//        }
//        }