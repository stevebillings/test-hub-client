package com.blackducksoftware.tools.testhubclient.model.policy;

import com.blackducksoftware.tools.testhubclient.model.Link;
import com.blackducksoftware.tools.testhubclient.model.ModelClass;
import com.google.gson.annotations.SerializedName;

public class ApprovalStatus extends ModelClass {
    private String approvalStatus;

    @SerializedName("_meta")
    private ApprovalStatusMeta meta;

    public String getApprovalStatus() {
	return approvalStatus;
    }

    public ApprovalStatusMeta getMeta() {
	return meta;
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
	return "PolicyStatus [approvalStatus=" + approvalStatus + ", meta="
		+ meta + "]";
    }

}
