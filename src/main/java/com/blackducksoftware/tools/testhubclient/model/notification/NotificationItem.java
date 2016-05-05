package com.blackducksoftware.tools.testhubclient.model.notification;

import com.google.gson.annotations.SerializedName;

public class NotificationItem {
    // public VulnerabilityNotificationContent content;
    public String contentType;
    public String type;
    public String createdAt;

    @SerializedName("_meta")
    public com.blackducksoftware.tools.testhubclient.model.notification.NotificationMeta Meta;

    // public VulnerabilityNotificationContent getContent() {
    // return content;
    // }

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

    @Override
    public String toString() {
	return "NotificationItem [contentType=" + contentType + ", type="
		+ type + ", createdAt=" + createdAt + ", Meta=" + Meta + "]";
    }

}
