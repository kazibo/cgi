package com.example.cgi;

import com.example.cgi.client.ExternalLoggingClient;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

import static org.springframework.test.web.client.match.MockRestRequestMatchers.*;
import static org.springframework.test.web.client.response.MockRestResponseCreators.*;

class ExternalLoggingClientTest {

    @Test
    void logWithdrawalSendsGetRequest() {
        RestClient.Builder builder = RestClient.builder();

        MockRestServiceServer server =
                MockRestServiceServer.bindTo(builder).build();

        ExternalLoggingClient client =
                new ExternalLoggingClient(builder, "https://example.test/log");

        server.expect(requestTo("https://example.test/log"))
                .andExpect(method(org.springframework.http.HttpMethod.GET))
                .andRespond(withSuccess());

        client.logWithdrawal();

        server.verify();
    }

    @Test
    void logWithdrawalThrowsWhenExternalServiceFails() {
        RestClient.Builder builder = RestClient.builder();

        MockRestServiceServer server =
                MockRestServiceServer.bindTo(builder).build();

        ExternalLoggingClient client =
                new ExternalLoggingClient(builder, "https://example.test/log");

        server.expect(requestTo("https://example.test/log"))
                .andRespond(withServerError());

        org.junit.jupiter.api.Assertions.assertThrows(
                org.springframework.web.client.RestClientResponseException.class,
                client::logWithdrawal
        );

        server.verify();
    }
}