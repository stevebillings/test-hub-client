package com.blackducksoftware.tools.testhubclient.model.notification;

public class ComponentVersionStatus {
    private String componentName;
    private String bomComponentVersionPolicyStatus;
    private String component;

    public String getComponentName() {
	return componentName;
    }

    public String getBomComponentVersionPolicyStatus() {
	return bomComponentVersionPolicyStatus;
    }

    public String getComponent() {
	return component;
    }

    @Override
    public String toString() {
	return "ComponentVersionStatus [componentName=" + componentName
		+ ", bomComponentVersionPolicyStatus="
		+ bomComponentVersionPolicyStatus + ", component=" + component
		+ "]";
    }

}
