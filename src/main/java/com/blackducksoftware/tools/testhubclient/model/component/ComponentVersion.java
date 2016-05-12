package com.blackducksoftware.tools.testhubclient.model.component;

import com.blackducksoftware.tools.testhubclient.model.ModelClass;

public class ComponentVersion extends ModelClass {
    private String versionName;

    public String getVersionName() {
	return versionName;
    }

    @Override
    public String toString() {
	return "ComponentVersion [versionName=" + versionName + "]";
    }

}
