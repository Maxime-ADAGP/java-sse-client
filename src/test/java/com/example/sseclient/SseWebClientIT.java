package com.example.sseclient;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.springframework.web.reactive.function.client.WebClient;

import reactor.core.publisher.Flux;

public class SseWebClientIT {
	
	private final WebClient webClient = WebClient.builder()
			.baseUrl("http://localhost:3000")
			.build();

	@Test
	void shouldRetrieveDataFromServer() {
		SseWebClient<String> sseWebClient = new SseWebClient<>(webClient, String.class);
		Flux<String> times = sseWebClient.retrieveData("/events");
		
		assertNotNull(times);
		assertTrue(times.take(5).count().block() > 0);
	}
}
