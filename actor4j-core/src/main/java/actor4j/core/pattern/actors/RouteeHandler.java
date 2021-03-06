/*
 * Copyright (c) 2015-2016, David A. Bauer
 */
package actor4j.core.pattern.actors;

import java.util.UUID;
import java.util.function.Predicate;

import actor4j.core.messages.ActorMessage;

public class RouteeHandler {
	protected Predicate<ActorMessage<?>> predicate;
	protected UUID routee;
	
	public RouteeHandler(Predicate<ActorMessage<?>> predicate, UUID routee) {
		super();
		this.predicate = predicate;
		this.routee = routee;
	}

	public Predicate<ActorMessage<?>> getPredicate() {
		return predicate;
	}
	
	public void setPredicate(Predicate<ActorMessage<?>> predicate) {
		this.predicate = predicate;
	}
	
	public UUID getRoutee() {
		return routee;
	}
	
	public void setRoutee(UUID routee) {
		this.routee = routee;
	}
}
