package com.blackducksoftware.tools.testhubclient.model.notification;

import java.util.List;

import com.blackducksoftware.tools.testhubclient.model.ModelClass;

public class NotificationResponse extends ModelClass {
    private String totalCount;
    private List<NotificationItem> items;

    public String getTotalCount() {
	return totalCount;
    }

    public List<NotificationItem> getItems() {
	return items;
    }

    @Override
    public String toString() {
	return "NotificationResponse [totalCount=" + totalCount + ", items="
		+ items + ", getDescription()=" + getDescription() + "]";
    }

}
