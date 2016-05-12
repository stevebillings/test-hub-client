package com.blackducksoftware.tools.testhubclient;

import static org.junit.Assert.assertEquals;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import com.blackducksoftware.tools.testhubclient.dao.NotificationDao;
import com.blackducksoftware.tools.testhubclient.dao.hub.HubNotificationDao;

public class HubCommonClientTest {

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
		"http://eng-hub-valid03.dc1.lan", username, password);

	HubCommonClient client = new HubCommonClient(dao);
	Statistics stats = client.run("2016-05-01T00:00:00.000Z",
		"2016-05-11T00:00:00.000Z", 1000);
	assertEquals(711, stats.getNotificationCount());
	assertEquals(567, stats.getTicketCount());
	assertEquals(437, stats.getDuplicateCount());
    }

}
