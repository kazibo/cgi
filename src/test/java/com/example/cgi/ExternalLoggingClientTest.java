package com.example.cgi;

import com.example.cgi.client.ExternalLoggingClient;
import com.example.cgi.dto.DepositWithdrawRequest;
import com.example.cgi.exception.ExternalLoggingException;
import com.example.cgi.model.Currency;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.*;
import static org.springframework.test.web.client.response.MockRestResponseCreators.*;

class ExternalLoggingClientTest {

    private static final String URL = "https://example.test/log";

    private MockRestServiceServer server;
    private ExternalLoggingClient client;
    private DepositWithdrawRequest request;

    @BeforeEach
    void setUp() {
        RestClient.Builder builder = RestClient.builder();
        server = MockRestServiceServer.bindTo(builder).build();
        client = new ExternalLoggingClient(builder.build(), URL);
        request = new DepositWithdrawRequest(Currency.EUR, new BigDecimal("100.00")); // adjust to your constructor
    }

    @Test
    void logWithdrawalSendsGetRequest() {
        server.expect(requestTo(URL))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess());

        client.logWithdrawal(request);

        server.verify();
    }

    @Test
    void logWithdrawalThrowsWhenExternalServiceFails() {
        server.expect(requestTo(URL))
                .andRespond(withServerError());

        assertThrows(ExternalLoggingException.class, () -> client.logWithdrawal(request));

        server.verify();
    }
}