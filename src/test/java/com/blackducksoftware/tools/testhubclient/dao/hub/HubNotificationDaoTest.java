package com.blackducksoftware.tools.testhubclient.dao.hub;

import static org.junit.Assert.assertEquals;

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import com.blackducksoftware.tools.testhubclient.dao.NotificationDao;
import com.blackducksoftware.tools.testhubclient.json.JsonModelParser;
import com.blackducksoftware.tools.testhubclient.model.notification.HubItemList;
import com.blackducksoftware.tools.testhubclient.model.notification.NotificationItem;
import com.blackducksoftware.tools.testhubclient.model.notification.NotificationType;
import com.blackducksoftware.tools.testhubclient.model.projectversion.ProjectVersionItem;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public class HubNotificationDaoTest {
	private static final String DATE_FORMAT = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'";
	private static NotificationDao hub;

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {

		hub = new HubNotificationDao("http://eng-hub-valid03.dc1.lan", "sysadmin", "blackduck", DATE_FORMAT);
		System.out.println("Hub version: " + hub.getVersion());
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
	}

	@Test
	public void testGetFromUrlWithoutLinks() throws Exception {

		NotificationItem notifItem = hub.getFromAbsoluteUrl(NotificationItem.class,
				"http://eng-hub-valid03.dc1.lan/api/notifications/51b42223-c093-4305-b383-ba73a02fcd30");
		System.out.println(notifItem);
		assertEquals("application/json", notifItem.getContentType());
		assertEquals(NotificationType.RULE_VIOLATION, notifItem.getType());
		assertEquals(1460920827990L, notifItem.getCreatedAt().getTime());
		assertEquals("GET", notifItem.getMeta().getAllow().get(0));
		assertEquals("http://eng-hub-valid03.dc1.lan/api/notifications/51b42223-c093-4305-b383-ba73a02fcd30", notifItem
				.getMeta().getHref());
	}

	@Test
	public void testGetFromUrlWithLinks() throws Exception {

		ProjectVersionItem projectVersionItem = hub
				.getFromAbsoluteUrl(
						ProjectVersionItem.class,
						"http://eng-hub-valid03.dc1.lan/api/projects/fa359df3-3319-4f6d-a00b-fe5ae5e8c15e/versions/dc377e25-4d16-4e58-91f0-8a90f4d23aa1");
		System.out.println(projectVersionItem);

		assertEquals("GET", projectVersionItem.getMeta().getAllow().get(0));
		assertEquals(
				"http://eng-hub-valid03.dc1.lan/api/projects/fa359df3-3319-4f6d-a00b-fe5ae5e8c15e/versions/dc377e25-4d16-4e58-91f0-8a90f4d23aa1",
				projectVersionItem.getMeta().getHref());

		// Test a link
		assertEquals("http://eng-hub-valid03.dc1.lan/api/projects/fa359df3-3319-4f6d-a00b-fe5ae5e8c15e",
				projectVersionItem.getLink("project"));
	}

	@Test
	public void testGetFromRelativeUrl() throws Exception {
		List<String> urlSegments = new ArrayList<>();
		urlSegments.add("api");
		urlSegments.add("notifications");

		Set<AbstractMap.SimpleEntry<String, String>> queryParameters = new HashSet<>();
		queryParameters.add(new AbstractMap.SimpleEntry<String, String>("startDate", "2016-05-01T00:00:00.000Z"));
		queryParameters.add(new AbstractMap.SimpleEntry<String, String>("endDate", "2016-05-02T00:00:00.000Z"));
		queryParameters.add(new AbstractMap.SimpleEntry<String, String>("limit", "1"));
		HubItemList notifResponse = hub.getFromRelativeUrl(HubItemList.class, urlSegments, queryParameters);
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

		JsonParser parser = new JsonParser();
		JsonObject json = parser.parse(response).getAsJsonObject();
		JsonArray array = json.get("items").getAsJsonArray();

		JsonModelParser jsonModelParser = new JsonModelParser(DATE_FORMAT);
		NotificationItem notifItem = jsonModelParser.parse(NotificationItem.class, array.get(0));

		System.out.println(notifItem);
		assertEquals("application/json", notifItem.getContentType());
		assertEquals(NotificationType.RULE_VIOLATION, notifItem.getType());
		assertEquals(1462114809830L, notifItem.getCreatedAt().getTime());
		assertEquals("GET", notifItem.getMeta().getAllow().get(0));
		assertEquals("http://eng-hub-valid03.dc1.lan/api/notifications/e5071453-cbae-457f-b84c-9d60c79d0409", notifItem
				.getMeta().getHref());
	}

}
