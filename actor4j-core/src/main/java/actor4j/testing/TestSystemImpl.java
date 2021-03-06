/*
 * Copyright (c) 2015-2017, David A. Bauer
 */
package actor4j.testing;

import java.util.Iterator;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import java.util.Map.Entry;

import actor4j.core.ActorCell;
import actor4j.core.ActorSystem;
import actor4j.core.DefaultActorSystemImpl;
import actor4j.core.actors.Actor;
import actor4j.core.actors.PseudoActor;
import actor4j.core.messages.ActorMessage;
import bdd4j.Story;

import static org.junit.Assert.*;

public class TestSystemImpl extends DefaultActorSystemImpl  {
	protected PseudoActor pseudoActor;
	protected volatile UUID pseudoActorId;
	protected volatile UUID testActorId;
	protected volatile CompletableFuture<ActorMessage<?>> actualMessage;
	
	public TestSystemImpl(ActorSystem wrapper) {
		this(null, wrapper);
	}

	public TestSystemImpl(String name, ActorSystem wrapper) {
		super(name, wrapper);
		
		messageDispatcher = new TestActorMessageDispatcher(this);
	}
	
	public ActorCell underlyingCell(UUID id) {
		return getCells().get(id);
	}
	
	public Actor underlyingActor(UUID id) {
		ActorCell cell = getCells().get(id);
		return (cell!=null)? cell.getActor() : null;
	}
	
	protected void testActor(Actor actor) {
		if (actor!=null && actor instanceof ActorTest) {
			testActorId = actor.getId();
			List<Story> list = ((ActorTest)actor).test();
			if (list!=null)
				for (Story story : list) {
					pseudoActor.reset();
					try { // workaround, Java hangs, when an AssertionError is thrown!
						story.run();
					}
					catch (AssertionError e) {
						e.printStackTrace();
					}
				}
			testActorId = null;
		}
	}
	
	public void testActor(UUID id) {
		testActor(underlyingActor(id));
	}
	
	public void testAllActors() {
		Iterator<Entry<UUID, ActorCell>> iterator = getCells().entrySet().iterator();
		while (iterator.hasNext())
			testActor(iterator.next().getValue().getActor());
	}

	public ActorMessage<?> awaitMessage(long timeout, TimeUnit unit) throws InterruptedException, TimeoutException {
		return pseudoActor.await(timeout, unit);
	}
	
	public void assertNoMessages() {
		assertFalse(pseudoActor.runOnce());
	}
}
