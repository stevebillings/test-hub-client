package com.blackducksoftware.tools.testhubclient.model.notification;

import com.google.gson.annotations.SerializedName;

public class ComponentVersionStatus {
	private String componentName;

	@SerializedName("componentVersion")
	private String componentVersionLink;

	@SerializedName("bomComponentVersionPolicyStatus")
	private String bomComponentVersionPolicyStatusLink;

	public String getComponentName() {
		return componentName;
	}

	public String getComponentVersionLink() {
		return componentVersionLink;
	}

	public String getBomComponentVersionPolicyStatusLink() {
		return bomComponentVersionPolicyStatusLink;
	}

	@Override
	public String toString() {
		return "ComponentVersionStatus [componentName=" + componentName + ", componentVersion=" + componentVersionLink
				+ ", bomComponentVersionPolicyStatusLink=" + bomComponentVersionPolicyStatusLink + "]";
	}

}
