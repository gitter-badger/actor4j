/*
 * Copyright (c) 2015-2017, David A. Bauer
 */
package actor4j.core;

import java.util.UUID;

import actor4j.core.messages.ActorMessage;

public class ActorService extends ActorSystem {
	public ActorService() {
		this(null);
	}
	
	public ActorService(String name) {
		super(name);
	}
	
	public String getServiceNodeName() {
		return system.getServiceNodeName();
	}

	public void setServiceNodeName(String serviceNodeName) {
		system.setServiceNodeName(serviceNodeName);
	}

	public boolean hasActor(String uuid) {
		return system.hasActor(uuid);
	}
	
	public UUID getActorFromAlias(String alias) {
		return system.getActorFromAlias(alias);
	}
	
	public void sendAsServer(ActorMessage<?> message) {
		system.sendAsServer(message);
	}
	
	public ActorClientRunnable getClientRunnable() {
		return system.getClientRunnable();
	}
}
