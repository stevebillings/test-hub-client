package com.blackducksoftware.tools.testhubclient.dao.hub;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import com.blackducksoftware.tools.testhubclient.dao.NotificationDao;
import com.blackducksoftware.tools.testhubclient.json.JsonModelParser;
import com.blackducksoftware.tools.testhubclient.json.MetaWithLinksDeserializer;
import com.blackducksoftware.tools.testhubclient.json.MetaWithoutLinksDeserializer;
import com.blackducksoftware.tools.testhubclient.model.Meta;
import com.blackducksoftware.tools.testhubclient.model.NameValuePair;
import com.blackducksoftware.tools.testhubclient.model.notification.NotificationItem;
import com.blackducksoftware.tools.testhubclient.model.notification.NotificationResponse;
import com.blackducksoftware.tools.testhubclient.model.notification.PolicyOverrideNotificationItem;
import com.blackducksoftware.tools.testhubclient.model.notification.RuleViolationNotificationItem;
import com.blackducksoftware.tools.testhubclient.model.notification.VulnerabilityNotificationItem;
import com.blackducksoftware.tools.testhubclient.model.projectversion.ProjectVersionItem;
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
    public void testGetFromUrlWithoutLinks() throws Exception {

	NotificationItem notifItem = hub
		.getFromAbsoluteUrl(
			NotificationItem.class,
			"http://eng-hub-valid03.dc1.lan/api/notifications/51b42223-c093-4305-b383-ba73a02fcd30",
			new MetaWithoutLinksDeserializer<Meta>());
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
    public void testGetFromUrlWithLinks() throws Exception {

	ProjectVersionItem projectVersionItem = hub
		.getFromAbsoluteUrl(
			ProjectVersionItem.class,
			"http://eng-hub-valid03.dc1.lan/api/projects/fa359df3-3319-4f6d-a00b-fe5ae5e8c15e/versions/dc377e25-4d16-4e58-91f0-8a90f4d23aa1",
			new MetaWithLinksDeserializer<Meta>());
	System.out.println(projectVersionItem);

	assertEquals("GET", projectVersionItem.getMeta().getAllow().get(0));
	assertEquals(
		"http://eng-hub-valid03.dc1.lan/api/projects/fa359df3-3319-4f6d-a00b-fe5ae5e8c15e/versions/dc377e25-4d16-4e58-91f0-8a90f4d23aa1",
		projectVersionItem.getMeta().getHref());
	assertEquals(
		"Instantiated via gson from JsonObject fetched from Hub by HubNotificationDao",
		projectVersionItem.getDescription());

	// Test a link
	assertEquals(
		"http://eng-hub-valid03.dc1.lan/api/projects/fa359df3-3319-4f6d-a00b-fe5ae5e8c15e",
		projectVersionItem.getLink("project"));
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
		NotificationResponse.class, urlSegments, queryParameters,
		new MetaWithoutLinksDeserializer<Meta>());
	List<NotificationItem> notifs = notifResponse.getItems();
	for (NotificationItem notif : notifs) {
	    System.out.println(notif);
	}
    }

    @Test
    public void testGetFromRelativeUrlJsonObject() throws Exception {
	List<String> urlSegments = new ArrayList<>();
	urlSegments.add("api");
	urlSegments.add("notifications");

	Set<NameValuePair> queryParameters = new HashSet<>();
	queryParameters.add(new NameValuePair("startDate",
		"2016-05-01T00:00:00.000Z"));
	queryParameters.add(new NameValuePair("endDate",
		"2016-05-05T00:00:00.000Z"));
	queryParameters.add(new NameValuePair("limit", "100"));
	NotificationResponse notifResponse = hub
		.getAndCacheItemsFromRelativeUrl(NotificationResponse.class,
			urlSegments, queryParameters,
			new MetaWithoutLinksDeserializer<Meta>());

	for (NotificationItem genericNotif : notifResponse.getItems()) {
	    if ("VULNERABILITY".equals(genericNotif.getType())) {
		VulnerabilityNotificationItem vulnerabilityNotif = hub
			.getItemFromCache(VulnerabilityNotificationItem.class,
				genericNotif.getMeta().getHref());
		System.out.println(vulnerabilityNotif);
		assertTrue(vulnerabilityNotif.getContent()
			.getNewVulnerabilityCount() > 0);
	    } else if ("RULE_VIOLATION".equals(genericNotif.getType())) {
		RuleViolationNotificationItem ruleViolationNotif = hub
			.getItemFromCache(RuleViolationNotificationItem.class,
				genericNotif.getMeta().getHref());
		System.out.println(ruleViolationNotif);
		assertTrue(ruleViolationNotif.getContent()
			.getComponentVersionStatuses().size() > 0);
	    } else if ("POLICY_OVERRIDE".equals(genericNotif.getType())) {
		PolicyOverrideNotificationItem policyOverrideNotif = hub
			.getItemFromCache(PolicyOverrideNotificationItem.class,
				genericNotif.getMeta().getHref());
		System.out.println(policyOverrideNotif);
		assertTrue(policyOverrideNotif.getContent().getProjectName() != null);
	    }
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

	JsonModelParser jsonModelParser = new JsonModelParser(
		new MetaWithoutLinksDeserializer<Meta>());
	NotificationItem notifItem = jsonModelParser.parse(
		NotificationItem.class, array.get(0));

	System.out.println(notifItem);
	assertEquals("application/json", notifItem.getContentType());
	assertEquals("RULE_VIOLATION", notifItem.getType());
	assertEquals("2016-05-01T11:00:09.830Z", notifItem.getCreatedAt());
	assertEquals("GET", notifItem.getMeta().getAllow().get(0));
	assertEquals(
		"http://eng-hub-valid03.dc1.lan/api/notifications/e5071453-cbae-457f-b84c-9d60c79d0409",
		notifItem.getMeta().getHref());
	assertEquals(
		"Instantiated via gson from JsonElement by JsonModelParser",
		notifItem.getDescription());

    }

}
