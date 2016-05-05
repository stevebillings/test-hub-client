package com.blackducksoftware.tools.testhubclient.model.notification;

public class ComponentVersionStatus {
    private String componentName;
    private String componentVersion;
    private String bomComponentVersionPolicyStatus;

    public String getComponentName() {
	return componentName;
    }

    public String getComponentVersion() {
	return componentVersion;
    }

    public String getBomComponentVersionPolicyStatus() {
	return bomComponentVersionPolicyStatus;
    }

    @Override
    public String toString() {
	return "ComponentVersionStatus [componentName=" + componentName
		+ ", componentVersion=" + componentVersion
		+ ", bomComponentVersionPolicyStatus="
		+ bomComponentVersionPolicyStatus + "]";
    }

}
