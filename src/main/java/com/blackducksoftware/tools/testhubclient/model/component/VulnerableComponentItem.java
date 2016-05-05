package com.blackducksoftware.tools.testhubclient.model.component;

import com.blackducksoftware.tools.testhubclient.model.vulnerability.VulnerabilityWithRemediation;

public class VulnerableComponentItem {
    private String componentVersion;
    private String componentName;
    private String componentVersionName;
    private VulnerabilityWithRemediation vulnerabilityWithRemediation;

    public String getComponentVersion() {
	return componentVersion;
    }

    public String getComponentName() {
	return componentName;
    }

    public String getComponentVersionName() {
	return componentVersionName;
    }

    public VulnerabilityWithRemediation getVulnerabilityWithRemediation() {
	return vulnerabilityWithRemediation;
    }

    @Override
    public String toString() {
	return "VulnerableComponentItem [componentVersion=" + componentVersion
		+ ", componentName=" + componentName
		+ ", componentVersionName=" + componentVersionName
		+ ", vulnerabilityWithRemediation="
		+ vulnerabilityWithRemediation + "]";
    }

}
