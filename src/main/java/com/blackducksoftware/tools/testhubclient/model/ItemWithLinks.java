package com.blackducksoftware.tools.testhubclient.model;

public class ItemWithLinks extends Item {

    private MetaWithLinks meta;

    public MetaWithLinks getMeta() {
	return meta;
    }

    public void setMeta(MetaWithLinks meta) {
	this.meta = meta;
    }

    @Override
    public String toString() {
	return "ItemWithLinks [meta=" + meta + ", getDescription()="
		+ getDescription() + "]";
    }

}
