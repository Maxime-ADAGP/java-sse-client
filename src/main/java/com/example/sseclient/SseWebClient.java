package com.example.sseclient;

import java.io.IOException;
import java.time.Duration;

import org.springframework.web.reactive.function.client.WebClient;

import lombok.extern.log4j.Log4j2;
import reactor.core.publisher.Flux;
import reactor.util.retry.Retry;

@Log4j2
public class SseWebClient {

	private WebClient webClient;

	public SseWebClient(WebClient webClient) {
		this.webClient = webClient;
	}

	public Flux<String> retrieveData(String fromPath) {
		return webClient.get()
			.uri(fromPath)
			.retrieve()
			.bodyToFlux(String.class)
			.retryWhen(Retry.backoff(5, Duration.ofSeconds(1)))
			.doOnError(IOException.class, e -> log.error(e.getMessage()));
	}

}
