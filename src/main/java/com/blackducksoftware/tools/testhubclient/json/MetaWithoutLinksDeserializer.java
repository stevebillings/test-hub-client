package com.blackducksoftware.tools.testhubclient.json;

import java.lang.reflect.Type;

import com.blackducksoftware.tools.testhubclient.model.MetaWithoutLinks;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;

public final class MetaWithoutLinksDeserializer<T> implements
	JsonDeserializer<T> {

    public T deserialize(JsonElement elem, Type interfaceType,
	    JsonDeserializationContext context) throws JsonParseException {

	final Type actualType = MetaWithoutLinks.class;
	T deserializedObject = context.deserialize(elem, actualType);
	return deserializedObject;
    }

    @Override
    public String toString() {
	return "MetaWithoutLinksDeserializer";
    }

}