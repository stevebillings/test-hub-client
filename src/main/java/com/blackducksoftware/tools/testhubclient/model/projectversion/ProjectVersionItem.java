package com.blackducksoftware.tools.testhubclient.model.projectversion;

import com.blackducksoftware.tools.testhubclient.model.Item;
import com.blackducksoftware.tools.testhubclient.model.Link;
import com.blackducksoftware.tools.testhubclient.model.Source;

public class ProjectVersionItem extends Item {
    private String versionName;
    private Phase phase;
    private Distribution distribution;
    private Source source;

    public String getVersionName() {
	return versionName;
    }

    public Phase getPhase() {
	return phase;
    }

    public Distribution getDistribution() {
	return distribution;
    }

    public Source getSource() {
	return source;
    }

    public String getLink(final String linkRel) {
	if (getMeta() != null && getMeta().getLinks() != null
		&& !getMeta().getLinks().isEmpty()) {
	    for (final Link link : getMeta().getLinks()) {
		if (link.getRel().equalsIgnoreCase(linkRel)) {
		    return link.getHref();
		}
	    }
	}
	return null;
    }

    @Override
    public String toString() {
	return "VersionItem [versionName=" + versionName + ", phase=" + phase
		+ ", distribution=" + distribution + ", source=" + source
		+ ", meta=" + getMeta() + "]";
    }

}