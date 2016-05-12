package com.blackducksoftware.tools.testhubclient.json;

import com.blackducksoftware.tools.testhubclient.dao.NotificationDaoException;
import com.blackducksoftware.tools.testhubclient.model.ModelClass;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;

public class JsonModelParser {
    private final Gson gson;

    public JsonModelParser() {
	this.gson = new GsonBuilder().create();
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
    public <T extends ModelClass> T parse(Class<T> modelClass, JsonElement elem) {
	T modelObject = gson.fromJson(elem, modelClass);
	modelObject
		.setDescription("Instantiated via gson from JsonElement by JsonModelParser");

	return modelObject;
    }
}