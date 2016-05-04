package com.blackducksoftware.tools.testhubclient;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;

public class SimpleHubClient {

    public static void main(String[] args) {
	Client client = ClientBuilder.newClient();
	WebTarget myResource = client
		.target("http://eng-hub-valid03.dc1.lan/api/notifications?startDate=2016-01-03T18%3A44%3A55.805Z&endDate=2016-01-09T18%3A44%3A55.805Z");
	Invocation.Builder builder = myResource
		.request(MediaType.APPLICATION_JSON);
	String response = builder.get(String.class);
	System.out.println("response=" + response);

    }

}
