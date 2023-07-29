package com.example.sseclient;

import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;

import java.io.IOException;
import java.lang.reflect.Executable;
import java.net.ConnectException;
import java.text.SimpleDateFormat;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;

public class SseWebClientIT {

	private final MockWebServer mockWebServer = new MockWebServer();
	private final SseWebClient<String> webClient = new SseWebClient<>(
			WebClient.create(mockWebServer.url("/").toString()),
			String.class
	);

	@AfterEach
	void teardownMockServer() throws IOException {
		mockWebServer.shutdown();
	}

	@Test
	void shouldRetrieveDataFromServer() {
		final SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd"); // ISO 8601
		final StringBuilder eventStreamBuilder = new StringBuilder();

		// add 15 events
		for (int i = 0; i < 15; i++) {
			final String dateString = simpleDateFormat.format(new Date());
			eventStreamBuilder
					.append("event: newEvent\ndata: {\"event\":\"a new event occurred!\",\"eventDate\":\"")
					.append(dateString)
					.append("\"}\n\n");
		}

		mockWebServer.enqueue(
				new MockResponse()
						.setResponseCode(200)
						.setHeader(HttpHeaders.CONTENT_TYPE, MediaType.TEXT_EVENT_STREAM)
						.setBody(eventStreamBuilder.toString())
		);

		Flux<String> times = webClient.retrieveData("/events");
		
		assertNotNull(times);
		times.take(5).count().subscribe((c) -> assertTrue(c > 0));
	}

	@ParameterizedTest
	@ValueSource(ints = { 200, 401, 403 })
	void shouldRetrievePrivilegedEvents(int httpCode) {
		final SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd"); // ISO 8601
		final StringBuilder eventStreamBuilder = new StringBuilder();

		// add 15 events
		for (int i = 0; i < 15; i++) {
			final String dateString = simpleDateFormat.format(new Date());
			eventStreamBuilder
					.append("event: newEvent\ndata: {\"event\":\"a new event occurred!\",\"eventDate\":\"")
					.append(dateString)
					.append("\"}\n\n");
		}

		mockWebServer.enqueue(
				new MockResponse()
						.setResponseCode(httpCode)
						.setHeader(HttpHeaders.CONTENT_TYPE, MediaType.TEXT_EVENT_STREAM)
						.setBody(eventStreamBuilder.toString())
		);

		Flux<String> times = webClient.retrievePrivilegedData("/events", "bearerToken");

		assertNotNull(times);

		assertDoesNotThrow(() -> times.take(5).count().subscribe((c) -> assertTrue(c > 0)));
	}
}
