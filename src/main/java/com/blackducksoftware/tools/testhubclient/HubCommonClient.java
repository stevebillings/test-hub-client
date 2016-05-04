package com.blackducksoftware.tools.testhubclient;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.lang.reflect.Type;
import java.net.URISyntaxException;
import java.util.List;

import org.restlet.Response;
import org.restlet.data.Method;
import org.restlet.resource.ClientResource;

import com.blackducksoftware.integration.hub.HubIntRestService;
import com.blackducksoftware.integration.hub.exception.BDRestException;
import com.blackducksoftware.integration.hub.exception.HubIntegrationException;
import com.blackducksoftware.tools.testhubclient.model.notification.NotificationItem;
import com.blackducksoftware.tools.testhubclient.model.notification.PolicyOverrideNotificationItem;
import com.blackducksoftware.tools.testhubclient.model.notification.RuleViolationNotificationItem;
import com.blackducksoftware.tools.testhubclient.model.notification.VulnerabilityNotificationItem;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.reflect.TypeToken;

public class HubCommonClient {
    private String baseUrl;
    private HubIntRestService svc;

    public static void main(String[] args) throws HubIntegrationException,
	    URISyntaxException, BDRestException, IOException {
	HubCommonClient client = new HubCommonClient();
	client.run();
    }

    private void run() throws HubIntegrationException, URISyntaxException,
	    BDRestException, IOException {

	baseUrl = "http://eng-hub-valid03.dc1.lan";
	String username = "sbillings";
	String password = "blackduck";

	login(baseUrl, username, password);

	String hubVersion = svc.getHubVersion();
	System.out.println("Hub version: " + hubVersion);

	// getSomeNotifications(baseUrl, "2016-01-01T00:00:00.000Z",
	// "2016-05-09T00:00:00.000Z", 100);

	getSomeNotifications2(baseUrl, "2016-01-01T00:00:00.000Z",
		"2016-05-01T20:00:00.000Z", 1000);

    }

    private String getSomeNotifications2(String hubUrl, String startDate,
	    String endDate, int limit) throws URISyntaxException,
	    BDRestException, IOException {

	final ClientResource resource = svc.createClientResource();
	resource.addSegment("api");
	resource.addSegment("notifications");
	resource.addQueryParameter("startDate", startDate);
	resource.addQueryParameter("endDate", endDate);
	resource.addQueryParameter("limit", String.valueOf(limit));

	System.out.println("Resource: " + resource);

	int responseCode = 0;

	resource.setMethod(Method.GET);
	resource.handle();
	responseCode = resource.getResponse().getStatus().getCode();

	if (responseCode == 200 || responseCode == 204 || responseCode == 202) {
	    final String response = readResponseAsString(resource.getResponse());

	    Gson gson = new GsonBuilder().create();
	    JsonParser parser = new JsonParser();
	    JsonObject json = parser.parse(response).getAsJsonObject();
	    JsonArray array = json.get("items").getAsJsonArray();

	    for (JsonElement elem : array) {
		NotificationItem notif = gson.fromJson(elem,
			NotificationItem.class);
		// System.out.println("NotificationItem: " + notif);
		if ("VULNERABILITY".equals(notif.getType())) {
		    VulnerabilityNotificationItem vulnNotif = gson.fromJson(
			    elem, VulnerabilityNotificationItem.class);
		    // System.out.println("Vuln Notif: " + vulnNotif);
		} else if ("RULE_VIOLATION".equals(notif.getType())) {
		    RuleViolationNotificationItem ruleViolationNotif = gson
			    .fromJson(elem, RuleViolationNotificationItem.class);
		    // System.out.println("RuleViolation Notif: "
		    // + ruleViolationNotif);
		} else if ("POLICY_OVERRIDE".equals(notif.getType())) {
		    PolicyOverrideNotificationItem policyOverrideNotif = gson
			    .fromJson(elem,
				    PolicyOverrideNotificationItem.class);
		    System.out.println("PolicyOverride Notif: "
			    + policyOverrideNotif);
		} else {
		    System.out
			    .println("I don't know how to parse/print this type: "
				    + notif.getType() + ": " + notif);
		}
	    }

	    final Response resp = resource.getResponse();
	    return resp.getEntityAsText();
	} else {
	    throw new BDRestException(
		    "There was a problem getting notifications. Error Code: "
			    + responseCode, resource);
	}
    }

    private String getSomeNotifications(String hubUrl, String startDate,
	    String endDate, int limit) throws URISyntaxException,
	    BDRestException, IOException {

	final ClientResource resource = svc.createClientResource();
	resource.addSegment("api");
	resource.addSegment("notifications");
	resource.addQueryParameter("startDate", startDate);
	resource.addQueryParameter("endDate", endDate);
	resource.addQueryParameter("limit", String.valueOf(limit));

	System.out.println("Resource: " + resource);

	int responseCode = 0;

	resource.setMethod(Method.GET);
	resource.handle();
	responseCode = resource.getResponse().getStatus().getCode();

	if (responseCode == 200 || responseCode == 204 || responseCode == 202) {
	    final String response = readResponseAsString(resource.getResponse());
	    final Gson gson = new GsonBuilder().create();
	    final JsonParser parser = new JsonParser();
	    final JsonObject json = parser.parse(response).getAsJsonObject();

	    JsonElement itemsElement = json.get("items");
	    Type type = new TypeToken<List<NotificationItem>>() {
	    }.getType();
	    List<NotificationItem> notifications = gson.fromJson(itemsElement,
		    type);

	    for (NotificationItem notif : notifications) {
		System.out.println("Notification: " + notif);

		if (!"VULNERABILITY".equals(notif.getType())) {
		    System.out.println("**** Non-VULN Notification: "
			    + notif.getType());
		}
	    }

	    final Response resp = resource.getResponse();
	    return resp.getEntityAsText();
	} else {
	    throw new BDRestException(
		    "There was a problem getting notifications. Error Code: "
			    + responseCode, resource);
	}
    }

    private String readResponseAsString(final Response response)
	    throws IOException {
	final StringBuilder sb = new StringBuilder();
	final Reader reader = response.getEntity().getReader();
	final BufferedReader bufReader = new BufferedReader(reader);
	try {
	    String line;
	    while ((line = bufReader.readLine()) != null) {
		sb.append(line);
		sb.append("\n");
	    }
	} finally {
	    bufReader.close();
	}
	return sb.toString();
    }

    private void login(String hubUrl, String username, String password)
	    throws HubIntegrationException, URISyntaxException, BDRestException {
	svc = new HubIntRestService(hubUrl);
	svc.setCookies(username, password);

    }
}
