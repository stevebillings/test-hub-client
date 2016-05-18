package com.blackducksoftware.tools.testhubclient.model.notification;

import java.util.List;

import com.blackducksoftware.tools.testhubclient.model.ModelClass;
import com.blackducksoftware.tools.testhubclient.model.component.VulnerableComponentItem;

public class VulnerableComponentsResponse extends ModelClass {
    private int totalCount;
    private List<VulnerableComponentItem> items;

    public int getTotalCount() {
	return totalCount;
    }

    public List<VulnerableComponentItem> getItems() {
	return items;
    }

    @Override
    public String toString() {
	return "VulnerableComponentsResponse [totalCount=" + totalCount
		+ ", items=" + items + ", getDescription()=" + getDescription()
		+ "]";
    }

}
