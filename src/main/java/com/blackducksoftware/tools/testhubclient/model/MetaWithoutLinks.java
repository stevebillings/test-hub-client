package com.blackducksoftware.tools.testhubclient.model;

import java.util.ArrayList;
import java.util.List;

public class MetaWithoutLinks implements Meta {

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
    public List<Link> getLinks() {
	throw new UnsupportedOperationException(
		"This type of item does not support links");
    }

    @Override
    public void setLinks(List<Link> links) {
	throw new UnsupportedOperationException(
		"This type of item does not support links");
    }

    @Override
    public String toString() {
	return "MetaWithoutLinks [allow=" + allow + ", href=" + href + "]";
    }

}
