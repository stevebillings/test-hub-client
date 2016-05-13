package com.blackducksoftware.tools.testhubclient.service;

import java.util.List;

import com.blackducksoftware.tools.testhubclient.model.component.ComponentVersion;
import com.blackducksoftware.tools.testhubclient.model.notification.NotificationItem;
import com.blackducksoftware.tools.testhubclient.model.policy.PolicyStatus;

public interface NotificationService {
    List<NotificationItem> getNotifications(String startDate, String endDate,
	    int limit) throws NotificationServiceException;

    String getVersion() throws NotificationServiceException;

    PolicyStatus getPolicyStatusFromLink(String url)
	    throws NotificationServiceException;

    ComponentVersion getComponentVersionFromLink(String url)
	    throws NotificationServiceException;

}
