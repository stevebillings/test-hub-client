package com.blackducksoftware.tools.testhubclient.service;

import java.util.List;

import com.blackducksoftware.tools.testhubclient.dao.NotificationDaoException;
import com.blackducksoftware.tools.testhubclient.model.notification.NotificationItem;

public interface NotificationService {
    List<NotificationItem> getNotifications(String startDate, String endDate,
	    int limit) throws NotificationDaoException,
	    NotificationServiceException;
}
