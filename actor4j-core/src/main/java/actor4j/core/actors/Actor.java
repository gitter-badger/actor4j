/*
 * Copyright (c) 2015-2017, David A. Bauer
 */
package actor4j.core.actors;

import static actor4j.core.protocols.ActorProtocolTag.*;

import java.util.Queue;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.function.Predicate;

import actor4j.core.ActorCell;
import actor4j.core.ActorServiceNode;
import actor4j.core.ActorSystem;
import actor4j.core.messages.ActorMessage;
import actor4j.core.supervisor.DefaultSupervisiorStrategy;
import actor4j.core.supervisor.SupervisorStrategy;
import actor4j.core.utils.ActorFactory;

public abstract class Actor {
	protected ActorCell cell;
	
	protected String name;
	
	protected Queue<ActorMessage<?>> stash; //must be initialized by hand
	
	public static final int POISONPILL = INTERNAL_STOP;
	public static final int TERMINATED = INTERNAL_STOP_SUCCESS;
	public static final int KILL       = INTERNAL_KILL;
	
	public static final int STOP       = INTERNAL_STOP;
	public static final int RESTART    = INTERNAL_RESTART;
	
	public static final int ACTIVATE   = INTERNAL_ACTIVATE;
	public static final int DEACTIVATE = INTERNAL_DEACTIVATE;
	
	/**
	 * Don't create here, new actors as child or send messages too other actors. You will 
	 * get a NullPointerException, because the variable cell is not initialized. It will 
	 * injected later by the framework. Use instead the method preStart for these reasons.
	 */
	public Actor() {
		this(null);
	}
	
	/**
	 * Don't create here, new actors as child or send messages too other actors. You will 
	 * get a NullPointerException, because the variable cell is not initialized. It will 
	 * injected later by the framework. Use instead the method preStart for these reasons.
	 */
	public Actor(String name) {
		super();
		
		this.name = name;
	}
	
	public ActorCell getCell() {
		return cell;
	}

	public void setCell(ActorCell cell) {
		this.cell = cell;
	}
	
	public ActorSystem getSystem() {
		return cell.getSystemWrapper();
	}
	
	public String getName() {
		return name;
	}
	
	public UUID getId() {
		return cell.getId();
	}
	
	public UUID self() {
		return cell.getId();
	}
	
	public UUID getParent() {
		return cell.getParent();
	}
	
	public Queue<UUID> getChildren() {
		return cell.getChildren();
	}
	
	public boolean isRoot() {
		return cell.isRoot();
	}
	
	public boolean isRootInUser() {
		return cell.isRootInUser();
	}
	
	public abstract void receive(ActorMessage<?> message);
	
	public void become(Consumer<ActorMessage<?>> behaviour, boolean replace) {
		cell.become(behaviour, replace);
	}
	
	public void become(Consumer<ActorMessage<?>> behaviour) {
		become(behaviour, true);
	}
	
	public void unbecome() {
		cell.unbecome();
	}
	
	public void unbecomeAll() {
		cell.unbecomeAll();
	}
	
	public void await(final UUID source, final Consumer<ActorMessage<?>> action) {
		become(new Consumer<ActorMessage<?>>() {
			@Override
			public void accept(ActorMessage<?> message) {
				if (message.source.equals(source)) {
					action.accept(message);
					unbecome();
				}
			}
		}, false);
	}
	
	public void await(final int tag, final Consumer<ActorMessage<?>> action) {
		become(new Consumer<ActorMessage<?>>() {
			@Override
			public void accept(ActorMessage<?> message) {
				if (message.tag==tag) {
					action.accept(message);
					unbecome();
				}
			}
		}, false);
	}
	
	public void await(final UUID source, final int tag, final Consumer<ActorMessage<?>> action) {
		become(new Consumer<ActorMessage<?>>() {
			@Override
			public void accept(ActorMessage<?> message) {
				if (message.source.equals(source) && message.tag==tag) {
					action.accept(message);
					unbecome();
				}
			}
		}, false);
	}
	
	public void await(final Predicate<ActorMessage<?>> predicate, final Consumer<ActorMessage<?>> action) {
		become(new Consumer<ActorMessage<?>>() {
			@Override
			public void accept(ActorMessage<?> message) {
				if (predicate.test(message)) {
					action.accept(message);
					unbecome();
				}
			}
		}, false);
	}
	
	public void send(ActorMessage<?> message) {
		cell.send(message);
	}
	
	public void sendViaPath(ActorMessage<?> message, String path) {
		UUID dest = cell.getSystem().getActorFromPath(path);
		if (dest!=null)
			send(message, dest);
	}
	
	public void sendViaPath(ActorMessage<?> message, String nodeName, String path) {
		ActorServiceNode found = null;
		for (ActorServiceNode node : cell.getSystem().getServiceNodes())
			if (node.getName().equals(nodeName)) {
				found = node;
				break;
			}
		
		if (found!=null)
			sendViaPath(message, found, path);
	}
	
	public void sendViaPath(ActorMessage<?> message, ActorServiceNode node, String path) {
		cell.send(message, node, path);
	}
	
	public void sendViaAlias(ActorMessage<?> message, String alias) {
		cell.send(message, alias);
	}
	
	public void send(ActorMessage<?> message, UUID dest) {
		message.source = self();
		message.dest   = dest;
		send(message);
	}
	
	public <T> void tell(T value, int tag, UUID dest) {
		send(new ActorMessage<T>(value, tag, self(), dest));
	}
	
	public <T> void tell(T value, int tag, String alias) {
		sendViaAlias(new ActorMessage<T>(value, tag, self(), null), alias);
	}
	
	public void forward(ActorMessage<?> message, UUID dest) {
		message.dest   = dest;
		send(message);
	}
	
	public void unhandled(ActorMessage<?> message) {
		cell.unhandled(message);
	}
	
	public void setAlias(String alias) {
		cell.getSystem().setAlias(self(), alias);
	}
	
	public UUID addChild(Class<? extends Actor> clazz, Object... args) {
		return cell.addChild(clazz, args);
	}
	
	public UUID addChild(ActorFactory factory) {
		return cell.addChild(factory);
	}
	
	public SupervisorStrategy supervisorStrategy() {
		return new DefaultSupervisiorStrategy();
	}
	
	/**
	 * Initialize here, your actor code. Create new actors as child or send too other actors messages, 
	 * before the first message for this actor could be processed.
	 */
	public void preStart() {
		// empty
	}
	
	public void preRestart(Exception reason) {
		cell.restart(reason);
	}
	
	public void postRestart(Exception reason) {
		preStart();
	}
	
	public void postStop() {
		// empty
	}
	
	public void stop() {
		cell.stop();
	}
	
	public void watch(UUID dest) {
		cell.watch(dest);
	}
	
	public void unwatch(UUID dest) {
		cell.unwatch(dest);
	}
}
