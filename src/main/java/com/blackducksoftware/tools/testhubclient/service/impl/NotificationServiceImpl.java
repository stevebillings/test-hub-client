package com.blackducksoftware.tools.testhubclient.service.impl;

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.blackducksoftware.tools.testhubclient.ClientLogger;
import com.blackducksoftware.tools.testhubclient.dao.NotificationDao;
import com.blackducksoftware.tools.testhubclient.dao.NotificationDaoException;
import com.blackducksoftware.tools.testhubclient.model.notification.NotificationItem;
import com.blackducksoftware.tools.testhubclient.model.notification.HubItemList;
import com.blackducksoftware.tools.testhubclient.model.notification.NotificationType;
import com.blackducksoftware.tools.testhubclient.model.notification.PolicyOverrideNotificationItem;
import com.blackducksoftware.tools.testhubclient.model.notification.RuleViolationNotificationItem;
import com.blackducksoftware.tools.testhubclient.model.notification.VulnerabilityNotificationItem;
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
    public List<NotificationItem> getNotifications(String startDate,
	    String endDate, int limit) throws NotificationServiceException {

	HubItemList notifResponse = getNotificationResponse(startDate,
		endDate, limit);

	List<NotificationItem> notificationItems = new ArrayList<>(
		notifResponse.getItems().size());

	for (NotificationItem genericNotif : notifResponse.getItems()) {
	    log.info("\n\n======================================================================\n"
		    + "NotificationItem: " + genericNotif);
	    if (NotificationType.VULNERABILITY.equals(genericNotif.getType())) {
		VulnerabilityNotificationItem vulnNotif;
		try {
		    vulnNotif = dao.getItemFromCache(
			    VulnerabilityNotificationItem.class, genericNotif
				    .getMeta().getHref());
		} catch (NotificationDaoException e) {
		    throw new NotificationServiceException(
			    "Error converting notification " + genericNotif
				    + " as a VulnerabilityNotificationItem");
		}
		notificationItems.add(vulnNotif);
	    } else if (NotificationType.RULE_VIOLATION.equals(genericNotif
		    .getType())) {
		RuleViolationNotificationItem ruleViolationNotif;
		try {
		    ruleViolationNotif = dao.getItemFromCache(
			    RuleViolationNotificationItem.class, genericNotif
				    .getMeta().getHref());
		} catch (NotificationDaoException e) {
		    throw new NotificationServiceException(
			    "Error converting notification " + genericNotif
				    + " as a RuleViolationNotificationItem");
		}
		notificationItems.add(ruleViolationNotif);
	    } else if (NotificationType.POLICY_OVERRIDE.equals(genericNotif
		    .getType())) {
		PolicyOverrideNotificationItem policyOverrideNotif;
		try {
		    policyOverrideNotif = dao.getItemFromCache(
			    PolicyOverrideNotificationItem.class, genericNotif
				    .getMeta().getHref());
		} catch (NotificationDaoException e) {
		    throw new NotificationServiceException(
			    "Error converting notification " + genericNotif
				    + " as a PolicyOverrideNotificationItem");
		}
		notificationItems.add(policyOverrideNotif);
	    } else {
		throw new NotificationServiceException(
			"Unknown notification type: " + genericNotif.getType()
				+ ": " + genericNotif);
	    }
	}

	return notificationItems;
    }

    private HubItemList getNotificationResponse(String startDate,
	    String endDate, int limit) throws NotificationServiceException {
	HubItemList notifResponse;
	List<String> urlSegments = new ArrayList<>();
	urlSegments.add("api");
	urlSegments.add("notifications");

	Set<AbstractMap.SimpleEntry<String, String>> queryParameters = new HashSet<>();
	queryParameters.add(new AbstractMap.SimpleEntry<String, String>(
		"startDate", startDate));
	queryParameters.add(new AbstractMap.SimpleEntry<String, String>(
		"endDate", endDate));
	queryParameters.add(new AbstractMap.SimpleEntry<String, String>(
		"limit", String.valueOf(limit)));

	try {
	    notifResponse = dao.getAndCacheItemsFromRelativeUrl(
		    HubItemList.class, urlSegments, queryParameters);
	} catch (NotificationDaoException e) {
	    throw new NotificationServiceException(e);
	}
	return notifResponse;
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
    public <T> T getResourceFromAbsoluteUrl(Class<T> modelClass, String url)
	    throws NotificationServiceException {
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
    public <T> T getLinkedResourceFromAbsoluteUrl(Class<T> modelClass,
	    String url) throws NotificationServiceException {
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
