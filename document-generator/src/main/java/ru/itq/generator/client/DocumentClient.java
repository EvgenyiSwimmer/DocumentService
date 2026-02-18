package ru.itq.generator.client;

import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.Map;

@Component
public class DocumentClient {

    private final RestClient client;

    public DocumentClient(RestClient.Builder builder) {
        this.client = builder.build();
    }

    public void create(String baseUrl, String author, String title) {
        client.post()
                .uri(baseUrl + "/api/v1/documents")
                .contentType(MediaType.APPLICATION_JSON)
                .body(Map.of("author", author, "title", title))
                .retrieve()
                .toBodilessEntity();
    }
}

