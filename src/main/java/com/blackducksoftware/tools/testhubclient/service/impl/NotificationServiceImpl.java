package com.blackducksoftware.tools.testhubclient.service.impl;

import java.util.List;

import com.blackducksoftware.tools.testhubclient.ClientLogger;
import com.blackducksoftware.tools.testhubclient.dao.NotificationDao;
import com.blackducksoftware.tools.testhubclient.dao.NotificationDaoException;
import com.blackducksoftware.tools.testhubclient.model.notification.NotificationItem;
import com.blackducksoftware.tools.testhubclient.service.NotificationService;
import com.blackducksoftware.tools.testhubclient.service.NotificationServiceException;

public class NotificationServiceImpl implements NotificationService {
	private final NotificationDao dao;
	private final ClientLogger log;

	public NotificationServiceImpl(NotificationDao dao) {
		this.dao = dao;
		log = new ClientLogger();
	}

	@Override
	public List<NotificationItem> getNotifications(String startDate, String endDate, int limit)
			throws NotificationServiceException {

		try {
			return dao.getNotifications(startDate, endDate, limit);
		} catch (NotificationDaoException e) {
			throw new NotificationServiceException(e);
		}
	}

	@Override
	public String getVersion() throws NotificationServiceException {
		try {
			return dao.getVersion();
		} catch (NotificationDaoException e) {
			throw new NotificationServiceException(e);
		}
	}

	@Override
	public <T> T getResourceFromAbsoluteUrl(Class<T> modelClass, String url) throws NotificationServiceException {
		if (url == null) {
			throw new NotificationServiceException("URL provided is null");
		}
		try {
			return dao.getFromAbsoluteUrl(modelClass, url);
		} catch (NotificationDaoException e) {
			throw new NotificationServiceException(e);
		}
	}

	@Override
	public <T> T getLinkedResourceFromAbsoluteUrl(Class<T> modelClass, String url) throws NotificationServiceException {
		if (url == null) {
			throw new NotificationServiceException("URL provided is null");
		}
		try {
			return dao.getFromAbsoluteUrl(modelClass, url);
		} catch (NotificationDaoException e) {
			throw new NotificationServiceException(e);
		}
	}
}
