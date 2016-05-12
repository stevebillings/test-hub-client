package com.blackducksoftware.tools.testhubclient.model.projectversion;

import com.blackducksoftware.tools.testhubclient.model.Link;
import com.blackducksoftware.tools.testhubclient.model.ModelClass;
import com.google.gson.annotations.SerializedName;

public class ProjectVersionItem extends ModelClass {
    private String versionName;
    private String phase;
    private String distribution;
    private String source;
    @SerializedName("_meta")
    private ProjectVersionMeta meta;

    public String getVersionName() {
	return versionName;
    }

    public String getPhase() {
	return phase;
    }

    public String getDistribution() {
	return distribution;
    }

    public String getSource() {
	return source;
    }

    public ProjectVersionMeta getMeta() {
	return meta;
    }

    // TODO should extend AbstractLinkedResource instead of this
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
		+ ", meta=" + meta + "]";
    }

}