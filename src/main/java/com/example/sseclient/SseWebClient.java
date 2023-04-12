package com.example.sseclient;

import java.io.IOException;
import java.time.Duration;

import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.WebClient;

import lombok.extern.log4j.Log4j2;
import reactor.core.publisher.Flux;
import reactor.util.retry.Retry;

@Log4j2
public class SseWebClient<T> {

	private final WebClient webClient;

	private final Class<T> type;

	public SseWebClient(WebClient webClient, Class<T> type) {
		this.webClient = webClient;
		this.type = type;
	}

	/**
	 * Retrieve data from the provided route. For example, a simple client
	 * retrieving {@code String} objects from an SSE route could consist in:
	 * <p><pre>
	 * WebClient webClient = WebClient.builder()
	 *     .baseUrl("http://localhost:3000")
	 *     .build();
	 *
	 * SseWebClient&lt;String&gt; client = new SseWebClient&lt;&gt;(webClient, String.class);
	 *
	 * Flux&lt;String&gt; events = client.retrieveData("/events");
	 *
	 * // retrieve data here
	 * events.subscribe(&#47;* ... *&#47;);
	 * </pre>
	 * <p>One should probably handle status errors (4xx and 5xx) on the
	 * {@link WebClient} level.
	 * @param fromPath the path from which to retrieve
		{@code T}-typed events
	 */
	public Flux<T> retrieveData(String fromPath) {
		return webClient.get()
			.uri(fromPath)
			.accept(MediaType.TEXT_EVENT_STREAM)
			.retrieve()
			.bodyToFlux(this.type)
			.retryWhen(Retry.backoff(5, Duration.ofSeconds(1)))
			.doOnError(IOException.class, e -> log.error(e.getMessage()));
	}
	
	/**
	 * Retrieve data from the provided route, using an Authorization header.
	 * For example, a simple client retrieving {@code String} objects from
	 * an SSE privileged route could consist in:
	 * <p><pre>
	 * WebClient webClient = WebClient.builder()
	 *     .baseUrl("http://localhost:3000")
	 *     .build();
	 *
	 * SseWebClient&lt;String&gt; client = new SseWebClient&lt;&gt;(webClient, String.class);
	 *
	 * Flux&lt;String&gt; events = client.retrievePrivilegedData("/events", jwtToken);
	 *
	 * // retrieve data here
	 * events.subscribe(&#47;* ... *&#47;);
	 * </pre>
	 * <p>One should probably handle status errors (4xx and 5xx) on the
	 * {@link WebClient} level. Unauthorized errors in particular will not
	 * be gracefully handled.
	 * @param fromPath	the path from which to retrieve events
	 * @param bearer	a privileged HTTP Bearer token (a JWT, for example)
	 */
	public Flux<T> retrievePrivilegedData(String fromPath, String bearer) {
		return webClient.get()
				.uri(fromPath)
				.accept(MediaType.TEXT_EVENT_STREAM)
				.header("Authorization", "Bearer " + bearer)
				.retrieve()
				.bodyToFlux(this.type)
				.retryWhen(Retry.backoff(5, Duration.ofSeconds(1)))
				.doOnError(IOException.class, e -> log.error(e.getMessage()));
	}
}
