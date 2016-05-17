package com.blackducksoftware.tools.testhubclient.model;

import java.util.ArrayList;
import java.util.List;

public class Meta {
    public List<String> allow = new ArrayList<String>();
    public String href;
    private List<Link> links;

    public List<String> getAllow() {
	return allow;
    }

    public void setAllow(List<String> allow) {
	this.allow = allow;
    }

    public String getHref() {
	return href;
    }

    public void setHref(String href) {
	this.href = href;
    }

    public List<Link> getLinks() {
	return links;
    }

    public void setLinks(List<Link> links) {
	this.links = links;
    }

    @Override
    public String toString() {
	return "Meta [allow=" + allow + ", href=" + href + ", links=" + links
		+ "]";
    }

}