/*
 * Copyright (c) 2015-2017, David A. Bauer
 */
package actor4j.service.node.websocket;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;

import javax.websocket.Session;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;

import actor4j.core.ActorClientRunnable;
import actor4j.core.ActorServiceNode;
import actor4j.core.messages.ActorMessage;
import actor4j.service.node.utils.TransferActorMessage;
import actor4j.service.node.websocket.endpoints.ActorClientEndpoint;

public class WebsocketActorClientRunnable implements ActorClientRunnable {
	protected final Object annotatedEndpointInstance;
	
	protected Map<ActorServiceNode, Session> sessions;
	protected List<ActorServiceNode> serviceNodes;
	protected LoadingCache<UUID, Integer> cache;
	protected LoadingCache<String, UUID> cacheAlias;
	
	public WebsocketActorClientRunnable(final List<ActorServiceNode> serviceNodes, int concurrencyLevel, int cachesize) {
		this.serviceNodes = serviceNodes;
		
		annotatedEndpointInstance = new ActorClientEndpoint();
		sessions = new ConcurrentHashMap<>();
		
		cache =  CacheBuilder.newBuilder()
				.maximumSize(cachesize)
				.concurrencyLevel(concurrencyLevel)
				.build(new CacheLoader<UUID, Integer>() {
			@Override
			public Integer load(UUID dest) throws Exception {
				String uuid = dest.toString();
				
				int found = -1;
				int i = 0;
				for (ActorServiceNode serviceNode : serviceNodes) {
					Session session = getSession(serviceNode);
					String response = WebSocketActorClientManager.hasActor(session, uuid).get();
					if (response.equals("1")) {
						found = i;
						break;
							
					}
					i++;
				}
				
				return found;
			}
		});
		
		cacheAlias =  CacheBuilder.newBuilder()
				.maximumSize(cachesize)
				.concurrencyLevel(concurrencyLevel)
				.build(new CacheLoader<String, UUID>() {
			@Override
			public UUID load(String alias) throws Exception {
				UUID result = null;
				int i = 0;
				for (ActorServiceNode serviceNode : serviceNodes) {
					Session session = getSession(serviceNode);
					String response = WebSocketActorClientManager.getActorFromAlias(session, alias).get();
					if (!response.equals("")) {
						result = UUID.fromString(response);
						final int found = i;
						cache.get(result, new Callable<Integer>() {
							@Override
							public Integer call() throws Exception {
								return found;
							}
						});
						break;
					}	
					i++;
				}
				
				return result;
			}
		});
	}
	
	@Override
	public void runViaAlias(ActorMessage<?> message, String alias) {
		if (alias!=null) {
			UUID uuid = null;
			if ((uuid=cacheAlias.getUnchecked(alias))!=null)
				message.dest = uuid;
			else
				return;	
		}
		
		int index;
		if ((index=cache.getUnchecked(message.dest))!=-1) {
			try {
				Session session = getSession(serviceNodes.get(index));
				try {
					WebSocketActorClientManager.sendMessage(session, new TransferActorMessage(message.value, message.tag, message.source.toString(), message.dest.toString()));
				} catch (InterruptedException e) {
					e.printStackTrace();
				} catch (ExecutionException e) {
					e.printStackTrace();
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	
	@Override
	public void runViaPath(ActorMessage<?> message, ActorServiceNode node, String path) {
		try {
			Session session = getSession(node);
			String response = null;
			try {
				response = WebSocketActorClientManager.getActorFromPath(session, path).get();
				if (response!=null && !response.equals(""))
					WebSocketActorClientManager.sendMessage(session, new TransferActorMessage(message.value, message.tag, message.source.toString(), response));
			} catch (InterruptedException e) {
				e.printStackTrace();
			} catch (ExecutionException e) {
				e.printStackTrace();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	protected Session getSession(ActorServiceNode serviceNode) {
		Session result = sessions.get(serviceNode);
		if (result==null) {
			try {
				result = WebSocketActorClientManager.connectToServer(annotatedEndpointInstance, new URI(serviceNode.getUri()));
				sessions.put(serviceNode, result);
			} catch (URISyntaxException e) {
				e.printStackTrace();
			}
		}
		
		return result;
	}
	
	public void closeAll() {
		Iterator<Entry<ActorServiceNode, Session>> iterator = sessions.entrySet().iterator();
		
		while (iterator.hasNext())
			try {
				iterator.next().getValue().close();
			} catch (IOException e) {
				e.printStackTrace();
			}
	}
}
