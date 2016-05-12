package com.blackducksoftware.tools.testhubclient.model.notification;

import com.blackducksoftware.tools.testhubclient.model.ModelClass;
import com.google.gson.annotations.SerializedName;

public class NotificationItem extends ModelClass {
    // public VulnerabilityNotificationContent content;
    public String contentType;
    public String type;
    public String createdAt;

    @SerializedName("_meta")
    public com.blackducksoftware.tools.testhubclient.model.notification.NotificationMeta Meta;

    public String getContentType() {
	return contentType;
    }

    public String getType() {
	return type;
    }

    public String getCreatedAt() {
	return createdAt;
    }

    public com.blackducksoftware.tools.testhubclient.model.notification.NotificationMeta getMeta() {
	return Meta;
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

    public void setMeta(
	    com.blackducksoftware.tools.testhubclient.model.notification.NotificationMeta meta) {
	Meta = meta;
    }

    @Override
    public String toString() {
	return "NotificationItem [contentType=" + contentType + ", type="
		+ type + ", createdAt=" + createdAt + ", Meta=" + Meta
		+ ", getDescription()=" + getDescription() + "]";
    }

}
