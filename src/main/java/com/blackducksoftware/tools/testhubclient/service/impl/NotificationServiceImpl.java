package com.blackducksoftware.tools.testhubclient.service.impl;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.blackducksoftware.tools.testhubclient.ClientLogger;
import com.blackducksoftware.tools.testhubclient.dao.NotificationDao;
import com.blackducksoftware.tools.testhubclient.dao.NotificationDaoException;
import com.blackducksoftware.tools.testhubclient.model.ModelClass;
import com.blackducksoftware.tools.testhubclient.model.NameValuePair;
import com.blackducksoftware.tools.testhubclient.model.notification.NotificationItem;
import com.blackducksoftware.tools.testhubclient.model.notification.NotificationResponse;
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

	NotificationResponse notifResponse = getNotificationResponse(startDate,
		endDate, limit);

	List<NotificationItem> notificationItems = new ArrayList<>(
		notifResponse.getItems().size());

	for (NotificationItem genericNotif : notifResponse.getItems()) {
	    log.info("\n\n======================================================================\n"
		    + "NotificationItem: " + genericNotif);
	    if ("VULNERABILITY".equals(genericNotif.getType())) {
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
	    } else if ("RULE_VIOLATION".equals(genericNotif.getType())) {
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
	    } else if ("POLICY_OVERRIDE".equals(genericNotif.getType())) {
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

    private NotificationResponse getNotificationResponse(String startDate,
	    String endDate, int limit) throws NotificationServiceException {
	NotificationResponse notifResponse;
	List<String> urlSegments = new ArrayList<>();
	urlSegments.add("api");
	urlSegments.add("notifications");

	Set<NameValuePair> queryParameters = new HashSet<>();
	queryParameters.add(new NameValuePair("startDate", startDate));
	queryParameters.add(new NameValuePair("endDate", endDate));
	queryParameters.add(new NameValuePair("limit", String.valueOf(limit)));

	try {
	    notifResponse = dao.getAndCacheItemsFromRelativeUrl(
		    NotificationResponse.class, urlSegments, queryParameters);
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
    public <T extends ModelClass> T getFromAbsoluteUrl(Class<T> modelClass,
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
