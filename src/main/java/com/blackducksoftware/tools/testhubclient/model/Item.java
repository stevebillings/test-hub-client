package com.blackducksoftware.tools.testhubclient.model;

import com.google.gson.annotations.SerializedName;

public class Item {
    @SerializedName("_meta")
    private Meta meta;

    public Meta getMeta() {
	return meta;
    }

    public void setMeta(Meta meta) {
	this.meta = meta;
    }

    @Override
    public String toString() {
	return "Item [meta=" + meta + "]";
    }

}
