package com.blackducksoftware.tools.testhubclient.dao;

import java.util.List;
import java.util.Set;

import com.blackducksoftware.tools.testhubclient.model.ModelClass;
import com.blackducksoftware.tools.testhubclient.model.NameValuePair;
import com.google.gson.Gson;
import com.google.gson.JsonElement;

public interface NotificationDao {

    String getVersion() throws Exception;

    <T extends ModelClass> T getFromRelativeUrl(Class<T> modelClass,
	    List<String> urlSegments, Set<NameValuePair> queryParameters)
	    throws NotificationDaoException;

    <T extends ModelClass> T getFromAbsoluteUrl(Class<T> modelClass, String url)
	    throws NotificationDaoException;

    <T extends ModelClass> T getFromJsonElement(Class<T> modelClass, Gson gson,
	    JsonElement elem) throws NotificationDaoException;
}
