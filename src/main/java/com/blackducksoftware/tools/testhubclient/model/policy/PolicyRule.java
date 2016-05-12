package com.blackducksoftware.tools.testhubclient.model.policy;

import com.blackducksoftware.tools.testhubclient.model.ModelClass;

public class PolicyRule extends ModelClass {
    private String name;

    public String getName() {
	return name;
    }

    @Override
    public String toString() {
	return "PolicyRule [name=" + name + "]";
    }

}
