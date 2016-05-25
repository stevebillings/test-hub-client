package com.blackducksoftware.tools.testhubclient.model.policy;

import com.blackducksoftware.tools.testhubclient.model.Item;

/**
 * Describes Rule Violation status
 * 
 * @author sbillings
 *
 */
public class ApprovalStatusItem extends Item {
	private PolicyStatus approvalStatus;

	public PolicyStatus getApprovalStatus() {
		return approvalStatus;
	}

	@Override
	public String toString() {
		return "PolicyStatus [approvalStatus=" + approvalStatus + ", meta=" + getMeta() + "]";
	}

}
