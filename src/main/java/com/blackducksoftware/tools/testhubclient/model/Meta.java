package com.blackducksoftware.tools.testhubclient.model;

import java.util.ArrayList;
import java.util.List;

public class Meta {
    public List<HttpMethod> allow = new ArrayList<>(); // Operations supported
    public String href; // Link to the resource that this meta is included in
    private List<Link> links;

    public List<HttpMethod> getAllow() {
	return allow;
    }

    public void setAllow(List<HttpMethod> allow) {
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