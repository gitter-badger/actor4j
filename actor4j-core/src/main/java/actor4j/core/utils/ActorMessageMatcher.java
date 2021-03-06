/*
 * Copyright (c) 2015, David A. Bauer
 */
package actor4j.core.utils;

import java.util.LinkedList;
import java.util.List;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.function.Predicate;

import actor4j.core.messages.ActorMessage;

public class ActorMessageMatcher {
	protected static class MatchTuple {
		public Predicate<ActorMessage<?>> predicate;
		public Consumer<ActorMessage<?>> action;
	}
	
	protected List<MatchTuple> matches;
	protected List<MatchTuple> matchesElse;
	protected List<MatchTuple> matchesAny;
	
	public ActorMessageMatcher() {
		matches     = new LinkedList<>();
		matchesElse = new LinkedList<>();
		matchesAny  = new LinkedList<>();
	}
		
	public ActorMessageMatcher match(final UUID source, Consumer<ActorMessage<?>> action) {
		checkAction(action);
		
		MatchTuple tuple = new MatchTuple();
		tuple.predicate = new Predicate<ActorMessage<?>>(){
			@Override
			public boolean test(ActorMessage<?> message) {
				return message.source.equals(source);
			}
		};
		tuple.action = action;
		matches.add(tuple);
		
		return this;
	}
	
	public ActorMessageMatcher match(final UUID[] sources, Consumer<ActorMessage<?>> action) {
		checkAction(action);
		
		MatchTuple tuple = new MatchTuple();
		tuple.predicate = new Predicate<ActorMessage<?>>(){
			@Override
			public boolean test(ActorMessage<?> message) {
				boolean result = false;
				for (UUID source : sources)
					if (message.source.equals(source)) {
						result = true;
						break;
					}
				return result;
			}
		};
		tuple.action = action;
		matches.add(tuple);
		
		return this;
	}
	
	public ActorMessageMatcher match(final int tag, Consumer<ActorMessage<?>> action) {
		checkAction(action);
		
		MatchTuple tuple = new MatchTuple();
		tuple.predicate = new Predicate<ActorMessage<?>>(){
			@Override
			public boolean test(ActorMessage<?> message) {
				return message.tag==tag;
			}
		};
		tuple.action = action;
		matches.add(tuple);
		
		return this;
	}
	
	public ActorMessageMatcher match(final int[] tags, Consumer<ActorMessage<?>> action) {
		checkAction(action);
		
		MatchTuple tuple = new MatchTuple();
		tuple.predicate = new Predicate<ActorMessage<?>>(){
			@Override
			public boolean test(ActorMessage<?> message) {
				boolean result = false;
				for (int tag : tags)
					if (message.tag==tag) {
						result = true;
						break;
					}
				return result;
			}
		};
		tuple.action = action;
		matches.add(tuple);
		
		return this;
	}
	
	public ActorMessageMatcher match(final UUID source, final int tag, Consumer<ActorMessage<?>> action) {
		checkAction(action);
		
		MatchTuple tuple = new MatchTuple();
		tuple.predicate = new Predicate<ActorMessage<?>>(){
			@Override
			public boolean test(ActorMessage<?> message) {
				return message.source.equals(source) && message.tag==tag;
			}
		};
		tuple.action = action;
		matches.add(tuple);
		
		return this;
	}
	
	public ActorMessageMatcher match(final UUID[] sources, final int tag, Consumer<ActorMessage<?>> action) {
		checkAction(action);
		
		MatchTuple tuple = new MatchTuple();
		tuple.predicate = new Predicate<ActorMessage<?>>(){
			@Override
			public boolean test(ActorMessage<?> message) {
				boolean result = false;
				if (message.tag==tag)
					for (UUID source : sources)
						if (message.source.equals(source)) {
							result = true;
							break;
						}
				return result;
			}
		};
		tuple.action = action;
		matches.add(tuple);
		
		return this;
	}
	
	public ActorMessageMatcher match(final UUID source, final int[] tags, Consumer<ActorMessage<?>> action) {
		checkAction(action);
		
		MatchTuple tuple = new MatchTuple();
		tuple.predicate = new Predicate<ActorMessage<?>>(){
			@Override
			public boolean test(ActorMessage<?> message) {
				boolean result = false;
				if (message.source.equals(source))
					for (int tag : tags)
						if (message.tag==tag) {
							result = true;
							break;
						}
				return result;
			}
		};
		tuple.action = action;
		matches.add(tuple);
		
		return this;
	}
	
	public ActorMessageMatcher match(final UUID[] sources, final int[] tags, Consumer<ActorMessage<?>> action) {
		checkAction(action);
		
		MatchTuple tuple = new MatchTuple();
		tuple.predicate = new Predicate<ActorMessage<?>>(){
			@Override
			public boolean test(ActorMessage<?> message) {
				boolean result = false;
				for (UUID source : sources)
					if (message.source.equals(source)) {
						result = true;
						break;
					}
				if (result) {
					result = false;
					for (int tag : tags)
						if (message.tag==tag) {
							result = true;
							break;
						}
				}
				return result;
			}
		};
		tuple.action = action;
		matches.add(tuple);
		
		return this;
	}
	
	public ActorMessageMatcher match(final Class<?> clazz, Consumer<ActorMessage<?>> action) {
		return match(clazz, null, action);
	}
	
	public ActorMessageMatcher match(final Class<?> clazz, Predicate<ActorMessage<?>> predicate, Consumer<ActorMessage<?>> action) {
		checkAction(action);
		
		MatchTuple tuple = new MatchTuple();
		tuple.predicate = new Predicate<ActorMessage<?>>(){
			@Override
			public boolean test(ActorMessage<?> message) {
				boolean result = false;
				if (message.value!=null) {
					result = message.value.getClass().equals(clazz);
					if (predicate!=null)
						result = result && predicate.test(message);
				}
				
				return result;
			}
		};
		tuple.action = action;
		matches.add(tuple);
		
		return this;
	}
	
	public ActorMessageMatcher match(Predicate<ActorMessage<?>> predicate, Consumer<ActorMessage<?>> action) {
		checkPredicate(predicate);
		checkAction(action);
		
		MatchTuple tuple = new MatchTuple();
		tuple.predicate = predicate;
		tuple.action = action;
		matches.add(tuple);
		
		return this;
	}
	
	public ActorMessageMatcher matchElse(Consumer<ActorMessage<?>> action) {
		checkAction(action);
		
		MatchTuple tuple = new MatchTuple();
		tuple.action = action;
		matchesElse.add(tuple);
		
		return this;
	}
	
	public ActorMessageMatcher matchAny(Consumer<ActorMessage<?>> action) {
		checkAction(action);
		
		MatchTuple tuple = new MatchTuple();
		tuple.action = action;
		matchesAny.add(tuple);
		
		return this;
	}
	
	public <T> boolean apply(ActorMessage<T> message) {
		boolean result = false;
		
		for (MatchTuple tuple : matches)
			if (tuple.predicate.test(message)) {
				tuple.action.accept(message);
				result = true;
			}
		if (!result)
			for (MatchTuple tuple : matchesElse) {
				tuple.action.accept(message);
				result = true;
			}
		for (MatchTuple tuple : matchesAny) {
			tuple.action.accept(message);
			result = true;
		}
		
		return result;
	}
	
	protected void checkPredicate(Predicate<ActorMessage<?>> predicate) {
		if (predicate==null)
			throw new NullPointerException("predicate is null");
	}
	
	protected void checkAction(Consumer<ActorMessage<?>> action) {
		if (action==null)
			throw new NullPointerException("action is null");
	}
}
