package com.blackducksoftware.tools.testhubclient.service.impl;

import static org.junit.Assert.assertEquals;

import java.util.List;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import com.blackducksoftware.tools.testhubclient.dao.NotificationDao;
import com.blackducksoftware.tools.testhubclient.dao.NotificationDaoException;
import com.blackducksoftware.tools.testhubclient.dao.mock.MockNotificationDao;
import com.blackducksoftware.tools.testhubclient.model.notification.NotificationItem;
import com.blackducksoftware.tools.testhubclient.service.NotificationServiceException;

public class NotificationServiceImplTest {

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
	}

	@Test
	public void test() throws NotificationDaoException, NotificationServiceException {
		NotificationDao dao = new MockNotificationDao();
		NotificationServiceImpl svc = new NotificationServiceImpl(dao);

		int limit = 3; // Mock returns 3, one of each
		List<NotificationItem> notifs = svc.getNotifications("2016-05-01T00:00:00.000Z", "2016-05-11T00:00:00.000Z",
				limit);

		assertEquals(limit, notifs.size());
	}

}
