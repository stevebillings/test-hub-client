package com.blackducksoftware.tools.testhubclient.model.component;


public class ComponentVersion {
    private String versionName;

    public String getVersionName() {
	return versionName;
    }

    @Override
    public String toString() {
	return "ComponentVersion [versionName=" + versionName + "]";
    }

}
