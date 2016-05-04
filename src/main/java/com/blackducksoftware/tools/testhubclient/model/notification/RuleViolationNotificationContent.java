package com.blackducksoftware.tools.testhubclient.model.notification;

import java.util.List;

public class RuleViolationNotificationContent {
    private String projectName;
    private String projectVersionName;
    private String componentVersionsInViolation;
    private List<ComponentVersionStatus> componentVersionStatuses;
    private String projectVersion;

    public String getProjectName() {
	return projectName;
    }

    public String getProjectVersionName() {
	return projectVersionName;
    }

    public String getComponentVersionsInViolation() {
	return componentVersionsInViolation;
    }

    public List<ComponentVersionStatus> getComponentVersionStatuses() {
	return componentVersionStatuses;
    }

    public String getProjectVersion() {
	return projectVersion;
    }

    @Override
    public String toString() {
	return "RuleViolationNotificationContent [projectName=" + projectName
		+ ", projectVersionName=" + projectVersionName
		+ ", componentVersionsInViolation="
		+ componentVersionsInViolation + ", componentVersionStatuses="
		+ componentVersionStatuses + ", projectVersion="
		+ projectVersion + "]";
    }

}
