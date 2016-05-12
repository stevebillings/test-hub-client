package com.blackducksoftware.tools.testhubclient.service.impl;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.blackducksoftware.tools.testhubclient.ClientLogger;
import com.blackducksoftware.tools.testhubclient.dao.NotificationDao;
import com.blackducksoftware.tools.testhubclient.dao.NotificationDaoException;
import com.blackducksoftware.tools.testhubclient.json.JsonModelParser;
import com.blackducksoftware.tools.testhubclient.model.NameValuePair;
import com.blackducksoftware.tools.testhubclient.model.notification.NotificationItem;
import com.blackducksoftware.tools.testhubclient.model.notification.NotificationResponse;
import com.blackducksoftware.tools.testhubclient.model.notification.PolicyOverrideNotificationItem;
import com.blackducksoftware.tools.testhubclient.model.notification.RuleViolationNotificationItem;
import com.blackducksoftware.tools.testhubclient.model.notification.VulnerabilityNotificationItem;
import com.blackducksoftware.tools.testhubclient.model.policy.PolicyStatus;
import com.blackducksoftware.tools.testhubclient.service.NotificationService;
import com.blackducksoftware.tools.testhubclient.service.NotificationServiceException;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

public class NotificationServiceImpl implements NotificationService {
    private final NotificationDao dao;
    private final ClientLogger log;
    private final JsonModelParser jsonModelParser;

    public NotificationServiceImpl(NotificationDao dao) {
	this.dao = dao;
	log = new ClientLogger();
	jsonModelParser = new JsonModelParser();
    }

    @Override
    public List<NotificationItem> getNotifications(String startDate,
	    String endDate, int limit) throws NotificationServiceException {

	List<String> urlSegments = new ArrayList<>();
	urlSegments.add("api");
	urlSegments.add("notifications");

	Set<NameValuePair> queryParameters = new HashSet<>();
	queryParameters.add(new NameValuePair("startDate", startDate));
	queryParameters.add(new NameValuePair("endDate", endDate));
	queryParameters.add(new NameValuePair("limit", String.valueOf(limit)));
	NotificationResponse notifResponse;
	try {
	    notifResponse = dao.getFromRelativeUrl(NotificationResponse.class,
		    urlSegments, queryParameters);
	} catch (NotificationDaoException e) {
	    throw new NotificationServiceException(e);
	}

	// Since we don't know the type of each item in advance, we
	// re-parse each into a type-specific object
	JsonObject jsonObject = notifResponse.getJsonObject();

	JsonArray array = jsonObject.get("items").getAsJsonArray();

	List<NotificationItem> notificationItems = new ArrayList<>(array.size());

	for (JsonElement elem : array) {
	    NotificationItem genericNotif = jsonModelParser.parse(
		    NotificationItem.class, elem);

	    log.info("\n\n======================================================================\n"
		    + "NotificationItem: " + genericNotif);
	    if ("VULNERABILITY".equals(genericNotif.getType())) {
		VulnerabilityNotificationItem vulnNotif = jsonModelParser
			.parse(VulnerabilityNotificationItem.class, elem);
		notificationItems.add(vulnNotif);
	    } else if ("RULE_VIOLATION".equals(genericNotif.getType())) {
		RuleViolationNotificationItem ruleViolationNotif = jsonModelParser
			.parse(RuleViolationNotificationItem.class, elem);
		notificationItems.add(ruleViolationNotif);
	    } else if ("POLICY_OVERRIDE".equals(genericNotif.getType())) {
		PolicyOverrideNotificationItem policyOverrideNotif = jsonModelParser
			.parse(PolicyOverrideNotificationItem.class, elem);
		notificationItems.add(policyOverrideNotif);
	    } else {
		throw new NotificationServiceException(
			"Unknown notification type: " + genericNotif.getType()
				+ ": " + genericNotif);
	    }
	}

	return notificationItems;
    }

    @Override
    public PolicyStatus getPolicyStatusFromLink(String url)
	    throws NotificationServiceException {
	try {
	    return dao.getFromAbsoluteUrl(PolicyStatus.class, url);
	} catch (NotificationDaoException e) {
	    throw new NotificationServiceException(e);
	}
    }

}
