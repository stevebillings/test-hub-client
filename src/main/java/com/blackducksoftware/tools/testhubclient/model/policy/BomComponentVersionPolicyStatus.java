package com.blackducksoftware.tools.testhubclient.model.policy;

import java.util.Date;

import com.blackducksoftware.tools.testhubclient.model.ModelClass;

public class BomComponentVersionPolicyStatus extends ModelClass {
    private PolicyStatus overallStatus;
    private Date updatedAt;

    public PolicyStatus getOverallStatus() {
	return overallStatus;
    }

    public Date getUpdatedAt() {
	return updatedAt;
    }

    @Override
    public String toString() {
	return "PolicyStatus [overallStatus=" + overallStatus + ", updatedAt="
		+ updatedAt + "]";
    }

}
