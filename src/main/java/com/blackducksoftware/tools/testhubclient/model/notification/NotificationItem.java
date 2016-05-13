package com.blackducksoftware.tools.testhubclient.model.notification;

import com.blackducksoftware.tools.testhubclient.model.Item;

public class NotificationItem extends Item {
    public String contentType;
    public String type;
    public String createdAt;

    public String getContentType() {
	return contentType;
    }

    public String getType() {
	return type;
    }

    public String getCreatedAt() {
	return createdAt;
    }

    public void setContentType(String contentType) {
	this.contentType = contentType;
    }

    public void setType(String type) {
	this.type = type;
    }

    public void setCreatedAt(String createdAt) {
	this.createdAt = createdAt;
    }

    @Override
    public String toString() {
	return "NotificationItem [contentType=" + contentType + ", type="
		+ type + ", createdAt=" + createdAt + ", Meta=" + getMeta()
		+ ", getDescription()=" + getDescription() + "]";
    }

}
