package com.blackducksoftware.tools.testhubclient.model.projectversion;

import java.util.ArrayList;
import java.util.List;

import com.blackducksoftware.tools.testhubclient.model.Link;

public class ProjectVersionMeta {
    public List<String> allow = new ArrayList<String>();
    public String href;
    public List<Link> links;

    public List<String> getAllow() {
	return allow;
    }

    public String getHref() {
	return href;
    }

    public List<Link> getLinks() {
	return links;
    }

    @Override
    public String toString() {
	return "VersionMeta [allow=" + allow + ", href=" + href + ", links="
		+ links + "]";
    }

}
