package com.blackducksoftware.tools.testhubclient.json;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.restlet.data.Reference;
import org.restlet.resource.ClientResource;

import com.blackducksoftware.integration.hub.HubIntRestService;
import com.blackducksoftware.integration.hub.exception.ResourceDoesNotExistException;
import com.blackducksoftware.integration.hub.util.RestletUtil;
import com.blackducksoftware.tools.testhubclient.model.notification.NotificationItem;
import com.blackducksoftware.tools.testhubclient.model.notification.HubItemList;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.reflect.TypeToken;

public class ItemListParser<T> {
    private Gson gson;
    private HubIntRestService hub;
    private final TypeToken<NotificationItem> requestListTypeToken;

    public ItemListParser(Class<T> baseType, HubIntRestService hub,
	    Map<String, Class<? extends T>> typeToSubclassMap) {

	this.hub = hub;
	GsonBuilder gsonBuilder = new GsonBuilder();
	requestListTypeToken = new TypeToken<NotificationItem>() {
	};
	RuntimeTypeAdapterFactory<T> pojoAdapter = (RuntimeTypeAdapterFactory<T>) RuntimeTypeAdapterFactory
		.of(baseType, "type");

	for (String typeName : typeToSubclassMap.keySet()) {
	    pojoAdapter.registerSubtype(typeToSubclassMap.get(typeName),
		    typeName);
	}

	gsonBuilder.registerTypeAdapterFactory(pojoAdapter);
	gson = gsonBuilder.setDateFormat(RestletUtil.JSON_DATE_FORMAT).create();
    }

    public List<T> parseNotificationItemList(List<String> urlSegments,
	    Set<AbstractMap.SimpleEntry<String, String>> queryParameters)
	    throws IOException, URISyntaxException,
	    ResourceDoesNotExistException {

	List<T> items = new ArrayList<>();

	// TODO: Change to use non reusable resource approach
	Reference queryRef = RestletUtil.createReference(hub.getBaseUrl(),
		urlSegments, queryParameters);
	ClientResource resource = RestletUtil.getResource(
		hub.createClientResource(), queryRef);
	System.out.println("Resource: " + resource);
	int responseCode = RestletUtil.getResponseStatusCode(resource);

	if (RestletUtil.isSuccess(responseCode)) {
	    final String responseString = RestletUtil.readResponseAsString(resource
		    .getResponse());

	    JsonParser parser = new JsonParser();
	    JsonObject json = parser.parse(responseString).getAsJsonObject();
	    HubItemList response = gson.fromJson(json,
		    HubItemList.class);
	    System.out.println(response);
	    JsonArray array = json.get("items").getAsJsonArray();
	    for (JsonElement elem : array) {
		T genericItem = gson.fromJson(elem,
			requestListTypeToken.getType());

		items.add(genericItem);

	    }
	} else {
	    throw new ResourceDoesNotExistException(
		    "Error getting resource from relative url segments "
			    + urlSegments + " and query parameters "
			    + queryParameters + "; errorCode: " + responseCode
			    + "; " + resource, resource);
	}
	return items;
    }

}
