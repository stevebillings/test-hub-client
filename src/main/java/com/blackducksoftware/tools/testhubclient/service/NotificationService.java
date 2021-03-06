package com.blackducksoftware.tools.testhubclient.service;

import java.util.List;

import com.blackducksoftware.tools.testhubclient.model.notification.NotificationItem;

public interface NotificationService {
	List<NotificationItem> getNotifications(String startDate, String endDate, int limit)
			throws NotificationServiceException;

	String getVersion() throws NotificationServiceException;

	public <T> T getLinkedResourceFromAbsoluteUrl(Class<T> modelClass, String url) throws NotificationServiceException;

	public <T> T getResourceFromAbsoluteUrl(Class<T> modelClass, String url) throws NotificationServiceException;

}
