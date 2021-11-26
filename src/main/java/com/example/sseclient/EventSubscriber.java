package com.example.sseclient;

import java.util.function.Consumer;

import lombok.extern.log4j.Log4j2;

@Log4j2
public class EventSubscriber<T> implements Consumer<T> {
	
	@Override
	public void accept(T t) {
		log.info("new event received: {}", t);
	}

}
