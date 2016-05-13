package com.blackducksoftware.tools.testhubclient.model;

import java.util.List;

public class MetaWithLinks extends Meta {
    private List<Link> links;

    public List<Link> getLinks() {
	return links;
    }

    public void setLinks(List<Link> links) {
	this.links = links;
    }

}
