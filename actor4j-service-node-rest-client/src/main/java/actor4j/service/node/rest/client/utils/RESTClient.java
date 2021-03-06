/*
 * Copyright (c) 2015, David A. Bauer
 */
package actor4j.service.node.rest.client.utils;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;

public class RESTClient {
	public static Client createClient() {
		return ClientBuilder.newClient();
	}

	public static Response get(Client client, String uri, String resourceName, String acceptedResponseTypes,
			MultivaluedMap<String, Object> headers) {
		return client.target(uri).path(resourceName).request(acceptedResponseTypes).headers(headers).get();
	}

	public static Response post(Client client, String uri, String resourceName, String acceptedResponseTypes,
			MultivaluedMap<String, Object> headers, Entity<?> entity) {
		return client.target(uri).path(resourceName).request(acceptedResponseTypes).headers(headers).post(entity);

	}
}
