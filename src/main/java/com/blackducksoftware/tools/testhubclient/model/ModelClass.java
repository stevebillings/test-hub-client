package com.blackducksoftware.tools.testhubclient.model;

public class ModelClass {
    private String description;

    public String getDescription() {
	return description;
    }

    public void setDescription(String description) {
	this.description = description;
    }

    @Override
    public String toString() {
	return "ModelClass [description=" + description + "]";
    }

}
