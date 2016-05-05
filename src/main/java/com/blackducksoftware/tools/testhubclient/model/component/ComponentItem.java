package com.blackducksoftware.tools.testhubclient.model.component;

public class ComponentItem {
    private String componentVersion;
    private String componentName;
    private String componentVersionName;

    public String getComponentVersion() {
	return componentVersion;
    }

    public String getComponentName() {
	return componentName;
    }

    public String getComponentVersionName() {
	return componentVersionName;
    }

    @Override
    public String toString() {
	return "ComponentItem [componentVersion=" + componentVersion
		+ ", componentName=" + componentName
		+ ", componentVersionName=" + componentVersionName + "]";
    }

}
