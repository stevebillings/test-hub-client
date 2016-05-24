package com.blackducksoftware.tools.testhubclient.json;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.AbstractMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.restlet.data.Reference;
import org.restlet.resource.ClientResource;

import com.blackducksoftware.integration.hub.HubIntRestService;
import com.blackducksoftware.integration.hub.exception.ResourceDoesNotExistException;
import com.blackducksoftware.integration.hub.util.RestletUtil;
import com.blackducksoftware.tools.testhubclient.model.notification.NotificationItem;
import com.blackducksoftware.tools.testhubclient.model.notification.NotificationResponse;
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

public class ItemListParser {
    private Gson gson;
    private HubIntRestService hub;
    private final TypeToken<NotificationItem> requestListTypeToken;

    public <T1, T2 extends T1> ItemListParser(HubIntRestService hub,
	    Class<T1> itemParentClass, Map<String, Class<T2>> typeToSubclassMap) {

	this.hub = hub;
	GsonBuilder gsonBuilder = new GsonBuilder();
	requestListTypeToken = new TypeToken<NotificationItem>() {
	};
	RuntimeTypeAdapterFactory<NotificationItem> pojoAdapter = (RuntimeTypeAdapterFactory<NotificationItem>) RuntimeTypeAdapterFactory
		.of(itemParentClass, "type")
		.registerSubtype(
			(Class<? extends T1>) VulnerabilityNotificationItem.class,
			"VULNERABILITY")
		.registerSubtype(
			(Class<? extends T1>) RuleViolationNotificationItem.class,
			"RULE_VIOLATION")
		.registerSubtype(
			(Class<? extends T1>) PolicyOverrideNotificationItem.class,
			"POLICY_OVERRIDE");

	gsonBuilder.registerTypeAdapterFactory(pojoAdapter);
	gson = gsonBuilder.setDateFormat(RestletUtil.JSON_DATE_FORMAT).create();
    }

    public void parseNotificationItemList(List<String> urlSegments,
	    Set<AbstractMap.SimpleEntry<String, String>> queryParameters)
	    throws IOException, URISyntaxException,
	    ResourceDoesNotExistException {

	// TODO: Change to use non reusable resource approach
	Reference queryRef = RestletUtil.createReference(hub.getBaseUrl(),
		urlSegments, queryParameters);
	ClientResource resource = RestletUtil.getResource(
		hub.createClientResource(), queryRef);
	System.out.println("Resource: " + resource);
	int responseCode = RestletUtil.getResponseStatusCode(resource);

	if (RestletUtil.isSuccess(responseCode)) {
	    final String response = RestletUtil.readResponseAsString(resource
		    .getResponse());

	    JsonParser parser = new JsonParser();
	    JsonObject json = parser.parse(response).getAsJsonObject();
	    NotificationResponse notificationResponse = gson.fromJson(json,
		    NotificationResponse.class);
	    System.out.println(notificationResponse);
	    JsonArray array = json.get("items").getAsJsonArray();
	    for (JsonElement elem : array) {
		NotificationItem genericItem = gson.fromJson(elem,
			requestListTypeToken.getType());

		if (genericItem instanceof VulnerabilityNotificationItem) {
		    VulnerabilityNotificationItem specificItem = (VulnerabilityNotificationItem) genericItem;
		    System.out.println(specificItem);
		} else if (genericItem instanceof RuleViolationNotificationItem) {
		    RuleViolationNotificationItem specificItem = (RuleViolationNotificationItem) genericItem;
		    System.out.println(specificItem);
		} else if (genericItem instanceof PolicyOverrideNotificationItem) {
		    PolicyOverrideNotificationItem specificItem = (PolicyOverrideNotificationItem) genericItem;
		    System.out.println(specificItem);
		}

	    }
	} else {
	    throw new ResourceDoesNotExistException(
		    "Error getting resource from relative url segments "
			    + urlSegments + " and query parameters "
			    + queryParameters + "; errorCode: " + responseCode
			    + "; " + resource, resource);
	}
    }

}
