package com.blackducksoftware.tools.testhubclient.model;

public class Link {
    private String rel;
    private String href;

    public String getRel() {
	return rel;
    }

    public String getHref() {
	return href;
    }

    @Override
    public String toString() {
	return "Link [rel=" + rel + ", href=" + href + "]";
    }

}
