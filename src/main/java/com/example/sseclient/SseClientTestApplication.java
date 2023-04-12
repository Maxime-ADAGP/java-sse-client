package com.example.sseclient;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.reactive.function.client.WebClient;

import reactor.core.publisher.Flux;

@SpringBootApplication
public class SseClientTestApplication {
	
	private static WebClient webClient = WebClient.builder()
			.baseUrl("http://localhost:3000")
			.build();

	public static void main(String[] args) {
		SpringApplication.run(SseClientTestApplication.class, args);
		
		SseWebClient<String> client = new SseWebClient<>(webClient, String.class);
		Flux<String> strs = client.retrieveData("/events");
		strs.subscribe(new EventSubscriber<String>());
		System.out.println(strs.blockFirst());
	}

}
