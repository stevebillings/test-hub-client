package com.blackducksoftware.tools.testhubclient.model.notification;

import java.util.Date;

import com.blackducksoftware.tools.testhubclient.model.Item;

public class NotificationItem extends Item {
    public String contentType;
    public NotificationType type;
    public Date createdAt;

    public String getContentType() {
	return contentType;
    }

    public NotificationType getType() {
	return type;
    }

    public Date getCreatedAt() {
	return createdAt;
    }

    public void setContentType(String contentType) {
	this.contentType = contentType;
    }

    public void setType(NotificationType type) {
	this.type = type;
    }

    public void setCreatedAt(Date createdAt) {
	this.createdAt = createdAt;
    }

    @Override
    public String toString() {
	return "NotificationItem [contentType=" + contentType + ", type="
		+ type + ", createdAt=" + createdAt + ", Meta=" + getMeta()
		+ "]";
    }

}
