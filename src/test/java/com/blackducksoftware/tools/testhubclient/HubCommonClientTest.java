package com.blackducksoftware.tools.testhubclient;

import static org.junit.Assert.assertEquals;

import java.net.URISyntaxException;
import java.util.List;
import java.util.Set;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.restlet.data.Method;
import org.restlet.resource.ClientResource;

import com.blackducksoftware.integration.hub.HubIntRestService;
import com.blackducksoftware.tools.testhubclient.dao.NotificationDao;
import com.blackducksoftware.tools.testhubclient.dao.NotificationDaoException;
import com.blackducksoftware.tools.testhubclient.dao.hub.HubNotificationDao;
import com.blackducksoftware.tools.testhubclient.model.NameValuePair;
import com.blackducksoftware.tools.testhubclient.service.NotificationService;
import com.blackducksoftware.tools.testhubclient.service.impl.NotificationServiceImpl;

public class HubCommonClientTest {
    private static final String DATE_FORMAT = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'";
    private HubIntRestService hub; // TODO temp

    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
    }

    @AfterClass
    public static void tearDownAfterClass() throws Exception {
    }

    @Test
    public void test() throws Exception {

	String username = "sysadmin";
	String password = "blackduck";

	NotificationDao dao = new HubNotificationDao(
		"http://eng-hub-valid03.dc1.lan", username, password,
		DATE_FORMAT);
	NotificationService svc = new NotificationServiceImpl(dao);

	HubCommonClient client = new HubCommonClient(svc);
	Statistics stats = client.run("2016-05-01T00:00:00.000Z",
		"2016-05-11T00:00:00.000Z", 1000);
	assertEquals(711, stats.getNotificationCount());
	assertEquals(560, stats.getTicketCount());
	assertEquals(433, stats.getDuplicateCount());
    }

    private ClientResource createClientResourceForGet(List<String> urlSegments,
	    Set<NameValuePair> queryParameters) throws NotificationDaoException {
	ClientResource resource;
	try {
	    resource = hub.createClientResource();
	} catch (URISyntaxException e) {
	    throw new NotificationDaoException(e.getMessage());
	}
	for (String urlSegment : urlSegments) {
	    resource.addSegment(urlSegment);
	}
	for (NameValuePair queryParameter : queryParameters) {
	    resource.addQueryParameter(queryParameter.getName(),
		    queryParameter.getValue());
	}

	resource.setMethod(Method.GET);
	resource.handle();
	return resource;
    }

}
