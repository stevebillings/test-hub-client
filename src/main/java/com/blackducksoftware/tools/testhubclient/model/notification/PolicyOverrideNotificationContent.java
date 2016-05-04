package com.blackducksoftware.tools.testhubclient.model.notification;

public class PolicyOverrideNotificationContent {
    private String projectName;
    private String projectVersionName;
    private String componentName;
    private String componentVersionName;
    private String firstName;
    private String lastName;
    private String projectVersion;
    private String componentVersion;
    private String bomComponentVersionPolicyStatus;

    public String getProjectName() {
	return projectName;
    }

    public String getProjectVersionName() {
	return projectVersionName;
    }

    public String getComponentName() {
	return componentName;
    }

    public String getComponentVersionName() {
	return componentVersionName;
    }

    public String getFirstName() {
	return firstName;
    }

    public String getLastName() {
	return lastName;
    }

    public String getProjectVersion() {
	return projectVersion;
    }

    public String getComponentVersion() {
	return componentVersion;
    }

    public String getBomComponentVersionPolicyStatus() {
	return bomComponentVersionPolicyStatus;
    }

    @Override
    public String toString() {
	return "PolicyOverrideNotificationContent [projectName=" + projectName
		+ ", projectVersionName=" + projectVersionName
		+ ", componentName=" + componentName
		+ ", componentVersionName=" + componentVersionName
		+ ", firstName=" + firstName + ", lastName=" + lastName
		+ ", projectVersion=" + projectVersion + ", componentVersion="
		+ componentVersion + ", bomComponentVersionPolicyStatus="
		+ bomComponentVersionPolicyStatus + "]";
    }

}
