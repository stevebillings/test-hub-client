package com.blackducksoftware.tools.testhubclient.dao.hub;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import com.blackducksoftware.tools.testhubclient.dao.NotificationDao;
import com.blackducksoftware.tools.testhubclient.model.NameValuePair;
import com.blackducksoftware.tools.testhubclient.model.notification.NotificationItem;
import com.blackducksoftware.tools.testhubclient.model.notification.NotificationResponse;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public class HubNotificationDaoTest {
    private static NotificationDao hub;

    @BeforeClass
    public static void setUpBeforeClass() throws Exception {

	hub = new HubNotificationDao("http://eng-hub-valid03.dc1.lan",
		"sysadmin", "blackduck");
	System.out.println("Hub version: " + hub.getVersion());
    }

    @AfterClass
    public static void tearDownAfterClass() throws Exception {
    }

    @Test
    public void testGetFromUrl() throws Exception {

	NotificationItem notifItem = hub
		.getFromUrl(
			NotificationItem.class,
			"http://eng-hub-valid03.dc1.lan/api/notifications/51b42223-c093-4305-b383-ba73a02fcd30");
	System.out.println(notifItem);
	assertEquals("application/json", notifItem.getContentType());
	assertEquals("RULE_VIOLATION", notifItem.getType());
	assertEquals("2016-04-17T15:20:27.990Z", notifItem.getCreatedAt());
	assertEquals("GET", notifItem.getMeta().getAllow().get(0));
	assertEquals(
		"http://eng-hub-valid03.dc1.lan/api/notifications/51b42223-c093-4305-b383-ba73a02fcd30",
		notifItem.getMeta().getHref());
	assertEquals(
		"Instantiated via gson from JsonObject fetched from Hub by HubNotificationDao",
		notifItem.getDescription());
    }

    @Test
    public void testGetFromRelativeUrl() throws Exception {
	List<String> urlSegments = new ArrayList<>();
	urlSegments.add("api");
	urlSegments.add("notifications");

	Set<NameValuePair> queryParameters = new HashSet<>();
	queryParameters.add(new NameValuePair("startDate",
		"2016-05-01T00:00:00.000Z"));
	queryParameters.add(new NameValuePair("endDate",
		"2016-05-02T00:00:00.000Z"));
	queryParameters.add(new NameValuePair("limit", "1"));
	NotificationResponse notifResponse = hub.getFromRelativeUrl(
		NotificationResponse.class, urlSegments, queryParameters);
	List<NotificationItem> notifs = notifResponse.getItems();
	for (NotificationItem notif : notifs) {
	    System.out.println(notif);
	}
    }

    @Test
    public void testGetFromJsonElement() throws Exception {

	final String response = "{\n"
		+ "\"totalCount\": 6,\n"
		+ "\"items\": [\n"
		+ "{\n"
		+ "\"content\": {\n"
		+ "\"projectName\": \"HUB-MAR28a\",\n"
		+ "\"projectVersionName\": \"1\",\n"
		+ "\"componentVersionsInViolation\": 1,\n"
		+ "\"componentVersionStatuses\": [\n"
		+ "{\n"
		+ "\"componentName\": \"Apache Camel\",\n"
		+ "\"bomComponentVersionPolicyStatus\": \"http://eng-hub-valid03.dc1.lan/api/projects/0a03fd84-d442-4eb4-829d-fb36abd03b08/versions/cf6c140a-8c91-495a-851f-d7b196e98f46/components/17064005-756d-4313-8472-e0c34e404b00/policy-status\",\n"
		+ "\"component\": \"http://eng-hub-valid03.dc1.lan/api/components/17064005-756d-4313-8472-e0c34e404b00\"\n"
		+ "}\n"
		+ "],\n"
		+ "\"projectVersion\": \"http://eng-hub-valid03.dc1.lan/api/projects/0a03fd84-d442-4eb4-829d-fb36abd03b08/versions/cf6c140a-8c91-495a-851f-d7b196e98f46\"\n"
		+ "},\n"
		+ "\"contentType\": \"application/json\",\n"
		+ "\"type\": \"RULE_VIOLATION\",\n"
		+ "\"createdAt\": \"2016-05-01T11:00:09.830Z\",\n"
		+ "\"_meta\": {\n"
		+ "\"allow\": [\n"
		+ "\"GET\"\n"
		+ "],\n"
		+ "\"href\": \"http://eng-hub-valid03.dc1.lan/api/notifications/e5071453-cbae-457f-b84c-9d60c79d0409\"\n"
		+ "}\n" + "}\n" + "]\n" + "}";

	Gson gson = new GsonBuilder().create();
	JsonParser parser = new JsonParser();
	JsonObject json = parser.parse(response).getAsJsonObject();
	JsonArray array = json.get("items").getAsJsonArray();

	NotificationItem notifItem = hub.getFromJsonElement(
		NotificationItem.class, gson, array.get(0));

	System.out.println(notifItem);
	assertEquals("application/json", notifItem.getContentType());
	assertEquals("RULE_VIOLATION", notifItem.getType());
	assertEquals("2016-05-01T11:00:09.830Z", notifItem.getCreatedAt());
	assertEquals("GET", notifItem.getMeta().getAllow().get(0));
	assertEquals(
		"http://eng-hub-valid03.dc1.lan/api/notifications/e5071453-cbae-457f-b84c-9d60c79d0409",
		notifItem.getMeta().getHref());
	assertEquals(
		"Instantiated via gson from JsonElement by HubNotificationDao",
		notifItem.getDescription());

    }

}
