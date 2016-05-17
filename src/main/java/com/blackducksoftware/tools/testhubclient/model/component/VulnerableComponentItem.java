package com.blackducksoftware.tools.testhubclient.model.component;

import com.blackducksoftware.tools.testhubclient.model.vulnerability.VulnerabilityWithRemediation;
import com.google.gson.annotations.SerializedName;

public class VulnerableComponentItem {
    @SerializedName("componentVersion")
    private String componentVersionLink;

    private String componentName;
    private String componentVersionName;
    private VulnerabilityWithRemediation vulnerabilityWithRemediation;

    public String getComponentVersionLink() {
	return componentVersionLink;
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
	return "VulnerableComponentItem [componentVersionLink="
		+ componentVersionLink + ", componentName=" + componentName
		+ ", componentVersionName=" + componentVersionName
		+ ", vulnerabilityWithRemediation="
		+ vulnerabilityWithRemediation + "]";
    }

}
