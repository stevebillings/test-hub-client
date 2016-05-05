package com.blackducksoftware.tools.testhubclient.model.notification;

import java.util.ArrayList;
import java.util.List;

public class NotificationMeta {
    public List<String> allow = new ArrayList<String>();
    public String href;

    public List<String> getAllow() {
	return allow;
    }

    public String getHref() {
	return href;
    }

    @Override
    public String toString() {
	return "Meta [allow=" + allow + ", href=" + href + "]";
    }

}
