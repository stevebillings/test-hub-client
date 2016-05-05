package com.blackducksoftware.tools.testhubclient.model.projectversion;

public class ProjectVersion {
    private String projectName;
    private String projectVersionName;
    private String projectVersion;

    public String getProjectName() {
	return projectName;
    }

    public String getProjectVersionName() {
	return projectVersionName;
    }

    public String getProjectVersion() {
	return projectVersion;
    }

    @Override
    public String toString() {
	return "ProjectVersion [projectName=" + projectName
		+ ", projectVersionName=" + projectVersionName
		+ ", projectVersion=" + projectVersion + "]";
    }

}
