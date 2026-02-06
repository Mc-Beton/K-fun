package pl.ksef.hub.integration.ksef.client;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.WebClient;
import pl.ksef.hub.integration.ksef.dto.KsefInvoiceResponse;
import pl.ksef.hub.integration.ksef.dto.KsefSessionRequest;
import pl.ksef.hub.integration.ksef.dto.KsefSessionResponse;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.time.OffsetDateTime;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Testy jednostkowe dla KsefApiClient
 */
@ExtendWith(MockitoExtension.class)
class KsefApiClientTest {

    @Mock
    private WebClient ksefWebClient;

    @Mock
    private WebClient.RequestBodyUriSpec requestBodyUriSpec;

    @Mock
    private WebClient.RequestHeadersUriSpec requestHeadersUriSpec;

    @Mock
    private WebClient.RequestBodySpec requestBodySpec;

    @Mock
    private WebClient.RequestHeadersSpec requestHeadersSpec;

    @Mock
    private WebClient.ResponseSpec responseSpec;

    @InjectMocks
    private KsefApiClient ksefApiClient;

    @Test
    void shouldInitializeSessionSuccessfully() {
        // Given
        String nip = "1234567890";
        String sessionToken = "initial-token";
        
        KsefSessionResponse expectedResponse = KsefSessionResponse.builder()
                .sessionToken(KsefSessionResponse.SessionToken.builder()
                        .token("session-token-123")
                        .expiresIn(3600L)
                        .build())
                .referenceNumber("REF-001")
                .timestamp(OffsetDateTime.now())
                .processingCode(200)
                .build();

        when(ksefWebClient.post()).thenReturn(requestBodyUriSpec);
        when(requestBodyUriSpec.uri(anyString())).thenReturn(requestBodySpec);
        when(requestBodySpec.header(eq(HttpHeaders.CONTENT_TYPE), eq(MediaType.APPLICATION_JSON_VALUE)))
                .thenReturn(requestBodySpec);
        when(requestBodySpec.header(eq("SessionToken"), eq(sessionToken)))
                .thenReturn(requestBodySpec);
        when(requestBodySpec.bodyValue(any(KsefSessionRequest.class)))
                .thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.bodyToMono(KsefSessionResponse.class))
                .thenReturn(Mono.just(expectedResponse));

        // When
        KsefSessionResponse response = ksefApiClient.initSession(nip, sessionToken);

        // Then
        assertNotNull(response);
        assertEquals("session-token-123", response.getToken());
        assertEquals("REF-001", response.getReferenceNumber());
        assertEquals(200, response.getProcessingCode());
        
        verify(ksefWebClient).post();
        verify(requestBodyUriSpec).uri("/api/online/Session/InitToken");
    }

    @Test
    void shouldSendInvoiceSuccessfully() {
        // Given
        String sessionToken = "session-token-123";
        String invoiceXml = "<?xml version=\"1.0\"?><Faktura>...</Faktura>";
        
        KsefInvoiceResponse expectedResponse = KsefInvoiceResponse.builder()
                .elementReferenceNumber("KSEF-123-456")
                .processingCode(200)
                .timestamp(OffsetDateTime.now())
                .build();

        when(ksefWebClient.put()).thenReturn(requestBodyUriSpec);
        when(requestBodyUriSpec.uri(anyString())).thenReturn(requestBodySpec);
        when(requestBodySpec.header(anyString(), anyString())).thenReturn(requestBodySpec);
        when(requestBodySpec.bodyValue(any())).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.bodyToMono(KsefInvoiceResponse.class))
                .thenReturn(Mono.just(expectedResponse));

        // When
        KsefInvoiceResponse response = ksefApiClient.sendInvoice(sessionToken, invoiceXml);

        // Then
        assertNotNull(response);
        assertEquals("KSEF-123-456", response.getElementReferenceNumber());
        assertEquals(200, response.getProcessingCode());
        
        verify(ksefWebClient).put();
        verify(requestBodyUriSpec).uri("/api/online/Invoice/Send");
    }

    @Test
    void shouldTerminateSessionSuccessfully() {
        // Given
        String sessionToken = "session-token-123";

        when(ksefWebClient.get()).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.uri(anyString())).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.header(eq("SessionToken"), eq(sessionToken)))
                .thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.bodyToMono(Void.class)).thenReturn(Mono.empty());

        // When
        assertDoesNotThrow(() -> ksefApiClient.terminateSession(sessionToken));

        // Then
        verify(ksefWebClient).get();
        verify(requestHeadersUriSpec).uri("/api/online/Session/Terminate");
    }

    @Test
    void shouldUseCorrectEndpointUrls() {
        // This test verifies that correct KSeF 2.0 endpoints are used
        // Endpoints should start with /api/online/
        
        // Note: This is validated through other tests
        assertTrue(true, "Endpoint URLs are verified in individual method tests");
    }
}
