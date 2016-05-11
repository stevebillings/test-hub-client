package com.blackducksoftware.tools.testhubclient.dao;

import org.restlet.data.Cookie;
import org.restlet.util.Series;

import com.blackducksoftware.tools.testhubclient.model.ModelClass;
import com.google.gson.Gson;
import com.google.gson.JsonElement;

public interface NotificationDao {

    <T extends ModelClass> T getFromUrl(Class<T> modelClass,
	    Series<Cookie> cookies, String url) throws Exception;

    <T extends ModelClass> T getFromJsonElement(Class<T> modelClass, Gson gson,
	    JsonElement elem) throws Exception;
}
