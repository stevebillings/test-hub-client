package com.blackducksoftware.tools.testhubclient.model;

import java.util.List;

public interface Meta {

    public List<String> getAllow();

    public String getHref();

    public void setAllow(List<String> allow);

    public void setHref(String href);

    public abstract List<Link> getLinks();

    public abstract void setLinks(List<Link> links);

}