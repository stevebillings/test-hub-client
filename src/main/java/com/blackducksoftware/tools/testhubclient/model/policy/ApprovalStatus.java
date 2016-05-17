package com.blackducksoftware.tools.testhubclient.model.policy;

import com.blackducksoftware.tools.testhubclient.model.Item;
import com.blackducksoftware.tools.testhubclient.model.Link;

public class ApprovalStatus extends Item {
    private String approvalStatus;

    public String getApprovalStatus() {
	return approvalStatus;
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
		+ getMeta() + "]";
    }

}
