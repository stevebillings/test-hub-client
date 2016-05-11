package com.blackducksoftware.tools.testhubclient;

public class JiraTicket {
    private final String dateCreated;
    private final JiraTicketType ticketType;
    private final String projectName;
    private final String projectVersionName;
    private final String componentName;
    private final String componentVersionName;
    private final String vulnerabilityName;
    private final String ruleName;
    private final ActionRequired actionRequired;

    public JiraTicket(String dateCreated, JiraTicketType ticketType,
	    String projectName, String projectVersionName,
	    String componentName, String componentVersionName,
	    String vulnerabilityName, String ruleName,
	    ActionRequired actionRequired) {
	super();
	this.dateCreated = dateCreated;
	this.ticketType = ticketType;
	this.projectName = projectName;
	this.projectVersionName = projectVersionName;
	this.componentName = componentName;
	this.componentVersionName = componentVersionName;
	this.vulnerabilityName = vulnerabilityName;
	this.ruleName = ruleName;
	this.actionRequired = actionRequired;
    }

    public String getDateCreated() {
	return dateCreated;
    }

    public JiraTicketType getTicketType() {
	return ticketType;
    }

    public String getProjectName() {
	return projectName;
    }

    public String getProjectVersionName() {
	return projectVersionName;
    }

    public String getComponentName() {
	return componentName;
    }

    public String getComponentVersionName() {
	return componentVersionName;
    }

    public String getVulnerabilityName() {
	return vulnerabilityName;
    }

    public String getRuleName() {
	return ruleName;
    }

    public ActionRequired getActionRequired() {
	return actionRequired;
    }

    @Override
    public String toString() {
	return "JiraTicket [dateCreated=" + dateCreated + ", ticketType="
		+ ticketType + ", projectName=" + projectName
		+ ", projectVersionName=" + projectVersionName
		+ ", componentName=" + componentName
		+ ", componentVersionName=" + componentVersionName
		+ ", vulnerabilityName=" + vulnerabilityName + ", ruleName="
		+ ruleName + ", actionRequired=" + actionRequired + "]";
    }

    @Override
    public int hashCode() {
	final int prime = 31;
	int result = 1;
	result = prime * result
		+ ((actionRequired == null) ? 0 : actionRequired.hashCode());
	result = prime * result
		+ ((componentName == null) ? 0 : componentName.hashCode());
	result = prime
		* result
		+ ((componentVersionName == null) ? 0 : componentVersionName
			.hashCode());
	result = prime * result
		+ ((projectName == null) ? 0 : projectName.hashCode());
	result = prime
		* result
		+ ((projectVersionName == null) ? 0 : projectVersionName
			.hashCode());
	result = prime * result
		+ ((ruleName == null) ? 0 : ruleName.hashCode());
	result = prime * result
		+ ((ticketType == null) ? 0 : ticketType.hashCode());
	result = prime
		* result
		+ ((vulnerabilityName == null) ? 0 : vulnerabilityName
			.hashCode());
	return result;
    }

    @Override
    public boolean equals(Object obj) {
	if (this == obj)
	    return true;
	if (obj == null)
	    return false;
	if (getClass() != obj.getClass())
	    return false;
	JiraTicket other = (JiraTicket) obj;
	if (actionRequired != other.actionRequired)
	    return false;
	if (componentName == null) {
	    if (other.componentName != null)
		return false;
	} else if (!componentName.equals(other.componentName))
	    return false;
	if (componentVersionName == null) {
	    if (other.componentVersionName != null)
		return false;
	} else if (!componentVersionName.equals(other.componentVersionName))
	    return false;
	if (projectName == null) {
	    if (other.projectName != null)
		return false;
	} else if (!projectName.equals(other.projectName))
	    return false;
	if (projectVersionName == null) {
	    if (other.projectVersionName != null)
		return false;
	} else if (!projectVersionName.equals(other.projectVersionName))
	    return false;
	if (ruleName == null) {
	    if (other.ruleName != null)
		return false;
	} else if (!ruleName.equals(other.ruleName))
	    return false;
	if (ticketType != other.ticketType)
	    return false;
	if (vulnerabilityName == null) {
	    if (other.vulnerabilityName != null)
		return false;
	} else if (!vulnerabilityName.equals(other.vulnerabilityName))
	    return false;
	return true;
    }

}
