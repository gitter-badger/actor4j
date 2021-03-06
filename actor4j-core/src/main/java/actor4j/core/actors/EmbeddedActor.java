/*
 * Copyright (c) 2015-2017, David A. Bauer
 */
package actor4j.core.actors;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Queue;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.function.Predicate;

import actor4j.core.messages.ActorMessage;

public abstract class EmbeddedActor {
	protected Actor host;
	
	protected String name;
	
	protected UUID id;
	
	protected boolean active;
	
	protected Deque<Predicate<ActorMessage<?>>> behaviourStack;
	
	protected Queue<ActorMessage<?>> stash; //must be initialized by hand
	
	public EmbeddedActor(Actor host) {
		this(null, host);
	}
	
	public EmbeddedActor(String name, Actor host) {
		super();
		this.name = name;
		this.host = host;
		id = UUID.randomUUID();
		active = true;
		behaviourStack = new ArrayDeque<>();
	}
	
	public Actor host() {
		return host;
	}

	public String getName() {
		return name;
	}
	
	public UUID getId() {
		return id;
	}
	
	public UUID self() {
		return id;
	}
	
	public boolean isActive() {
		return active;
	}

	public void setActive(boolean active) {
		this.active = active;
	}
	
	public boolean embedded(ActorMessage<?> message) {
		boolean result = false;
		
		if (active) {
			Predicate<ActorMessage<?>> behaviour = behaviourStack.peek();
			if (behaviour==null)
				result = receive(message);
			else
				result = behaviour.test(message);	
		}
		
		return result;
	}
	
	public abstract boolean receive(ActorMessage<?> message);
	
	public void become(Predicate<ActorMessage<?>> behaviour, boolean replace) {
		if (replace && !behaviourStack.isEmpty())
			behaviourStack.pop();
		behaviourStack.push(behaviour);
	}
	
	public void become(Predicate<ActorMessage<?>> behaviour) {
		become(behaviour, true);
	}
	
	public void unbecome() {
		behaviourStack.pop();
	}
	
	public void unbecomeAll() {
		behaviourStack.clear();
	}
	
	public void await(final UUID source, final Consumer<ActorMessage<?>> action) {
		become(new Predicate<ActorMessage<?>>() {
			@Override
			public boolean test(ActorMessage<?> message) {
				boolean result = false;
				if (message.source.equals(source)) {
					action.accept(message);
					result = true;
					unbecome();
				}
				return result;
			}
		}, false);
	}
	
	public void await(final int tag, final Consumer<ActorMessage<?>> action) {
		become(new Predicate<ActorMessage<?>>() {
			@Override
			public boolean test(ActorMessage<?> message) {
				boolean result = false;
				if (message.tag==tag) {
					action.accept(message);
					result = true;
					unbecome();
				}
				return result;
			}
		}, false);
	}
	
	public void await(final UUID source, final int tag, final Consumer<ActorMessage<?>> action) {
		become(new Predicate<ActorMessage<?>>() {
			@Override
			public boolean test(ActorMessage<?> message) {
				boolean result = false;
				if (message.source.equals(source) && message.tag==tag) {
					action.accept(message);
					result = true;
					unbecome();
				}
				return result;
			}
		}, false);
	}
	
	public void await(final Predicate<ActorMessage<?>> predicate, final Consumer<ActorMessage<?>> action) {
		become(new Predicate<ActorMessage<?>>() {
			@Override
			public boolean test(ActorMessage<?> message) {
				boolean result = false;
				if (predicate.test(message)) {
					action.accept(message);
					result = true;
					unbecome();
				}
				return result;
			}
		}, false);
	}
	
	public void preStart() {
		// empty
	}
	
	public void preRestart(Exception reason) {
		// empty
	}
	
	public void postRestart(Exception reason) {
		preStart();
	}
	
	public void postStop() {
		// empty
	}
}
