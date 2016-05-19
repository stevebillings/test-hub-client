package com.blackducksoftware.tools.testhubclient.model.projectversion;

import com.blackducksoftware.tools.testhubclient.model.Item;
import com.blackducksoftware.tools.testhubclient.model.Source;

public class ProjectVersionItem extends Item {
    private String versionName;
    private Phase phase;
    private Distribution distribution;
    private Source source;

    public String getVersionName() {
	return versionName;
    }

    public Phase getPhase() {
	return phase;
    }

    public Distribution getDistribution() {
	return distribution;
    }

    public Source getSource() {
	return source;
    }

    @Override
    public String toString() {
	return "VersionItem [versionName=" + versionName + ", phase=" + phase
		+ ", distribution=" + distribution + ", source=" + source
		+ ", meta=" + getMeta() + "]";
    }

}