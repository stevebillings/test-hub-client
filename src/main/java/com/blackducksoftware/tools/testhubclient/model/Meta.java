package com.blackducksoftware.tools.testhubclient.model;

import java.util.ArrayList;
import java.util.List;

public class Meta {

    public List<String> allow = new ArrayList<String>();
    public String href;

    public List<String> getAllow() {
	return allow;
    }

    public String getHref() {
	return href;
    }

    public void setAllow(List<String> allow) {
	this.allow = allow;
    }

    public void setHref(String href) {
	this.href = href;
    }

    @Override
    public String toString() {
	return "Meta [allow=" + allow + ", href=" + href + "]";
    }

}
