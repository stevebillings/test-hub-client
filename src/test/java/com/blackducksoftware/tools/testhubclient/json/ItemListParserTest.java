package com.blackducksoftware.tools.testhubclient.json;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import com.blackducksoftware.integration.hub.HubIntRestService;
import com.blackducksoftware.integration.hub.exception.BDRestException;
import com.blackducksoftware.integration.hub.exception.HubIntegrationException;
import com.blackducksoftware.integration.hub.exception.ResourceDoesNotExistException;
import com.blackducksoftware.tools.testhubclient.model.notification.NotificationItem;
import com.blackducksoftware.tools.testhubclient.model.notification.PolicyOverrideNotificationItem;
import com.blackducksoftware.tools.testhubclient.model.notification.RuleViolationNotificationItem;
import com.blackducksoftware.tools.testhubclient.model.notification.VulnerabilityNotificationItem;

public class ItemListParserTest {

    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
    }

    @AfterClass
    public static void tearDownAfterClass() throws Exception {
    }

    @Test
    public void test() throws URISyntaxException, HubIntegrationException,
	    BDRestException, IOException, ResourceDoesNotExistException {
	HubIntRestService restService = new HubIntRestService(
		"http://eng-hub-valid03.dc1.lan");
	restService.setCookies("sysadmin", "blackduck");

	Map<String, Class<? extends NotificationItem>> typeToSubclassMap = new HashMap<>();
	typeToSubclassMap.put("VULNERABILITY",
		VulnerabilityNotificationItem.class);
	typeToSubclassMap.put("RULE_VIOLATION",
		RuleViolationNotificationItem.class);
	typeToSubclassMap.put("POLICY_OVERRIDE",
		PolicyOverrideNotificationItem.class);

	ItemListParser<NotificationItem> parser = new ItemListParser<NotificationItem>(
		NotificationItem.class, restService, typeToSubclassMap);

	List<String> urlSegments = new ArrayList<>();
	urlSegments.add("api");
	urlSegments.add("notifications");

	Set<AbstractMap.SimpleEntry<String, String>> queryParameters = new HashSet<>();
	queryParameters.add(new AbstractMap.SimpleEntry<String, String>(
		"startDate", "2016-05-01T00:00:00.000Z"));
	queryParameters.add(new AbstractMap.SimpleEntry<String, String>(
		"endDate", "2016-05-11T00:00:00.000Z"));
	queryParameters.add(new AbstractMap.SimpleEntry<String, String>(
		"limit", "100"));

	List<NotificationItem> items = parser.parseNotificationItemList(
		urlSegments, queryParameters);

	int vulnerabilityCount = 0;
	int ruleViolationCount = 0;
	int policyOverrideCount = 0;

	for (NotificationItem genericItem : items) {
	    if (genericItem instanceof VulnerabilityNotificationItem) {
		VulnerabilityNotificationItem specificItem = (VulnerabilityNotificationItem) genericItem;
		System.out.println(specificItem);
		vulnerabilityCount++;
	    } else if (genericItem instanceof RuleViolationNotificationItem) {
		RuleViolationNotificationItem specificItem = (RuleViolationNotificationItem) genericItem;
		System.out.println(specificItem);
		ruleViolationCount++;
	    } else if (genericItem instanceof PolicyOverrideNotificationItem) {
		PolicyOverrideNotificationItem specificItem = (PolicyOverrideNotificationItem) genericItem;
		System.out.println(specificItem);
		policyOverrideCount++;
	    } else {
		System.out.println("Don't recognize this type: " + genericItem);
	    }
	}
	assertEquals(0, policyOverrideCount);
	assertEquals(70, ruleViolationCount);
	assertEquals(30, vulnerabilityCount);
    }

}
