/*
 * Copyright (c) 2015-2017, David A. Bauer
 */
package actor4j.service.node.websocket;

import java.io.IOException;
import java.net.URI;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;

import javax.websocket.ContainerProvider;
import javax.websocket.DeploymentException;
import javax.websocket.Session;
import javax.websocket.WebSocketContainer;

import com.fasterxml.jackson.databind.ObjectMapper;

import actor4j.service.node.utils.TransferActorMessage;
import actor4j.service.node.websocket.endpoints.ActorServerEndpoint;

public class WebSocketActorClientManager {
	public static Map<Session, CompletableFuture<String>> sessionMap;
	
	public static final String HAS_ACTOR            = String.valueOf(ActorServerEndpoint.HAS_ACTOR);
	public static final String GET_ACTOR_FROM_ALIAS = String.valueOf(ActorServerEndpoint.GET_ACTOR_FROM_ALIAS);
	public static final String GET_ACTOR_FROM_PATH  = String.valueOf(ActorServerEndpoint.GET_ACTOR_FROM_PATH);
	public static final String SEND_MESSAGE         = String.valueOf(ActorServerEndpoint.SEND_MESSAGE);
	
	static {
		sessionMap = new ConcurrentHashMap<>();
	}
	
	private WebSocketActorClientManager() {
	}
	
	public static Session connectToServer(Class<?> annotatedEndpointClass, URI path) {
		Session result = null;
		WebSocketContainer container = ContainerProvider.getWebSocketContainer();
		try {
			result = container.connectToServer(annotatedEndpointClass, path);
		} catch (DeploymentException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		return result;
	}
	
	public static Session connectToServer(Object annotatedEndpointInstance, URI path) {
		Session result = null;
		WebSocketContainer container = ContainerProvider.getWebSocketContainer();
		try {
			result = container.connectToServer(annotatedEndpointInstance, path);
		} catch (DeploymentException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		return result;
	}
	
	public static CompletableFuture<String> sendText(Session session, String message) throws IOException, InterruptedException, ExecutionException {
		CompletableFuture<String> result = new CompletableFuture<>();
		sessionMap.put(session, result);
        session.getBasicRemote().sendText(message);
        return result;
	}
	
	public static CompletableFuture<String> sendText(Session session, String tag, String message) throws IOException, InterruptedException, ExecutionException {
		return sendText(session, tag+message);
	}
	
	public static CompletableFuture<String> getActorFromAlias(Session session, String alias) throws IOException, InterruptedException, ExecutionException  {
		return sendText(session, GET_ACTOR_FROM_ALIAS, alias);
	}
	
	public static CompletableFuture<String> getActorFromPath(Session session, String path) throws IOException, InterruptedException, ExecutionException  {
		return sendText(session, GET_ACTOR_FROM_PATH, path);
	}
	
	public static CompletableFuture<String> hasActor(Session session, String uuid) throws IOException, InterruptedException, ExecutionException  {
		return sendText(session, HAS_ACTOR, uuid);
	}
	
	public static CompletableFuture<String> sendMessage(Session session, TransferActorMessage message) throws IOException, InterruptedException, ExecutionException  {
		return sendText(session, SEND_MESSAGE, new ObjectMapper().writeValueAsString(message));
	}
	
	public static String getActorFromAliasSync(Session session, String alias) throws IOException, InterruptedException, ExecutionException  {
		return getActorFromAlias(session, alias).get();
	}
	
	public static String getActorFromPathSync(Session session, String path) throws IOException, InterruptedException, ExecutionException  {
		return getActorFromPath(session, path).get();
	}
	
	public static Boolean hasActorSync(Session session, String uuid) throws IOException, InterruptedException, ExecutionException  {
		return hasActor(session, uuid).get().equals("1")? true:false;
	}
	
	public static Boolean sendMessageSync(Session session, TransferActorMessage message) throws IOException, InterruptedException, ExecutionException  {
		return sendMessage(session, message).get().equals("1")? true:false;
	}
}
