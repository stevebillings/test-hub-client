package com.blackducksoftware.tools.testhubclient.json;

import com.blackducksoftware.tools.testhubclient.dao.NotificationDaoException;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;

public class JsonModelParser {
	private final Gson gson;

	public JsonModelParser(String dateFormat) {
		gson = new GsonBuilder().setDateFormat(dateFormat).create();
	}

	/**
	 * Get an object of the given type from the given JSON element, using the
	 * given Gson object.
	 * 
	 * @param modelClass
	 * @param gson
	 * @param elem
	 * @return
	 * @throws NotificationDaoException
	 */
	public <T> T parse(Class<T> modelClass, JsonElement elem) {
		return gson.fromJson(elem, modelClass);
	}
}
