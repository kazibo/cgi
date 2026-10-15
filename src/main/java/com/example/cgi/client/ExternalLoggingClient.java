package com.example.cgi.client;

import com.example.cgi.dto.DepositWithdrawRequest;
import com.example.cgi.exception.ExternalLoggingException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

@Component
public class ExternalLoggingClient {

    private final RestClient restClient;
    private final String loggingUrl;

    public ExternalLoggingClient(
            RestClient loggingRestClient,
            @Value("${external.logging.url}") String loggingUrl
    ) {
        this.restClient = loggingRestClient;
        this.loggingUrl = loggingUrl;
    }

    public void logWithdrawal(DepositWithdrawRequest request) {
        try {
            restClient.get()
                    .uri(loggingUrl)
                    .retrieve()
                    .toBodilessEntity();
        } catch (RestClientException e) {
            throw new ExternalLoggingException("External logging service is unavailable", e);
        }
    }
}
