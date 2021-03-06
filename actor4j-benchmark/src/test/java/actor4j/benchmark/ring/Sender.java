/*
 * Copyright (c) 2015, David A. Bauer
 */
package actor4j.benchmark.ring;

import java.util.UUID;

import actor4j.core.actors.Actor;
import actor4j.core.messages.ActorMessage;

public class Sender extends Actor {
	protected UUID next;
	
	public Sender(UUID next) {
		super();
		
		this.next = next;
	}

	@Override
	public void receive(ActorMessage<?> message) {
		send(new ActorMessage<UUID>(self(), 0, self(), next));
	}
}
