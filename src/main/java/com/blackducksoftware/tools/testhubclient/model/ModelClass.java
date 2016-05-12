package com.blackducksoftware.tools.testhubclient.model;

import com.google.gson.JsonObject;

public class ModelClass {
    private String description;
    private JsonObject jsonObject;

    public String getDescription() {
	return description;
    }

    public void setDescription(String description) {
	this.description = description;
    }

    /**
     * Get the underlying JSON object. Use this if you need to re-parse it into
     * a different class than the original parsing (such as a type-specific
     * class vs. the original generic class used to determine its type).
     * 
     * @return
     */
    public JsonObject getJsonObject() {
	return jsonObject;
    }

    public void setJsonObject(JsonObject jsonObject) {
	this.jsonObject = jsonObject;
    }

    @Override
    public String toString() {
	return "ModelClass [description=" + description + "]";
    }

}
