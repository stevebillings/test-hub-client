package com.blackducksoftware.tools.testhubclient.model.policy;

public class PolicyStatus {
    private String overallStatus;
    private String updatedAt;

    public String getOverallStatus() {
	return overallStatus;
    }

    public String getUpdatedAt() {
	return updatedAt;
    }

    @Override
    public String toString() {
	return "PolicyStatus [overallStatus=" + overallStatus + ", updatedAt="
		+ updatedAt + "]";
    }

}
