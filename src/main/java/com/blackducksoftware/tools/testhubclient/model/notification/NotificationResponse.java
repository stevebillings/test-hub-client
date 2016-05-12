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

    public void setTotalCount(String totalCount) {
	this.totalCount = totalCount;
    }

    public void setItems(List<NotificationItem> items) {
	this.items = items;
    }

    @Override
    public String toString() {
	return "NotificationResponse [totalCount=" + totalCount + ", items="
		+ items + ", getDescription()=" + getDescription() + "]";
    }

}
