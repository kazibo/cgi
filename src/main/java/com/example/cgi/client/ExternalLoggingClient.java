package com.example.cgi.client;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
public class ExternalLoggingClient {

    private final RestClient restClient;
    private final String loggingUrl;

    public ExternalLoggingClient(
            RestClient.Builder builder,
            @Value("${external.logging.url}") String loggingUrl
    ) {
        this.restClient = builder.build();
        this.loggingUrl = loggingUrl;
    }

    public void logWithdrawal() {
        restClient.get()
                .uri(loggingUrl)
                .retrieve()
                .toBodilessEntity();
    }
}
