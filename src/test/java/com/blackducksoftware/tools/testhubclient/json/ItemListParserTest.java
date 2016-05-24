package com.blackducksoftware.tools.testhubclient.json;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import com.blackducksoftware.integration.hub.HubIntRestService;
import com.blackducksoftware.integration.hub.exception.BDRestException;
import com.blackducksoftware.integration.hub.exception.HubIntegrationException;
import com.blackducksoftware.integration.hub.exception.ResourceDoesNotExistException;
import com.blackducksoftware.tools.testhubclient.model.notification.NotificationItem;

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
	ItemListParser parser = new ItemListParser(restService,
		NotificationItem.class, null);

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

	parser.parseNotificationItemList(urlSegments, queryParameters);
    }

}
