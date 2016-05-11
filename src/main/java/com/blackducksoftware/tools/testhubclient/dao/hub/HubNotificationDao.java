package com.blackducksoftware.tools.testhubclient.dao.hub;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.net.URI;
import java.net.URISyntaxException;

import org.restlet.Context;
import org.restlet.Response;
import org.restlet.data.Cookie;
import org.restlet.data.Method;
import org.restlet.resource.ClientResource;
import org.restlet.util.Series;

import com.blackducksoftware.tools.testhubclient.ClientLogger;
import com.blackducksoftware.tools.testhubclient.dao.NotificationDao;
import com.blackducksoftware.tools.testhubclient.model.ModelClass;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public class HubNotificationDao implements NotificationDao {
    private ClientLogger log = new ClientLogger();

    public <T extends ModelClass> T getFromUrl(Class<T> modelClass,
	    Series<Cookie> cookies, String url) throws Exception {

	if (url == null) {
	    return null;
	}

	final ClientResource resource = createGetClientResourceWithGivenLink(
		cookies, url);

	log.debug("Resource: " + resource);
	int responseCode = resource.getResponse().getStatus().getCode();
	if (responseCode == 200 || responseCode == 204 || responseCode == 202) {
	    log.debug("SUCCESS getting resource from Hub");
	    final String response = readResponseAsString(resource.getResponse());

	    Gson gson = new GsonBuilder().create();
	    JsonParser parser = new JsonParser();
	    JsonObject json = parser.parse(response).getAsJsonObject();

	    T modelObject = gson.fromJson(json, modelClass);
	    modelObject
		    .setDescription("Instantiated via gson from JsonObject fetched from Hub by HubNotificationDao");
	    return modelObject;
	} else {
	    throw new Exception("Error getting resource from " + url + ": "
		    + responseCode);
	}
    }

    private String readResponseAsString(final Response response)
	    throws IOException {
	final StringBuilder sb = new StringBuilder();
	final Reader reader = response.getEntity().getReader();
	final BufferedReader bufReader = new BufferedReader(reader);
	try {
	    String line;
	    while ((line = bufReader.readLine()) != null) {
		sb.append(line);
		sb.append("\n");
	    }
	} finally {
	    bufReader.close();
	}
	return sb.toString();
    }

    private ClientResource createGetClientResourceWithGivenLink(
	    Series<Cookie> cookies, String givenLink) throws URISyntaxException {
	final ClientResource resource = createClientResourceWithGivenLink(
		cookies, givenLink);

	resource.setMethod(Method.GET);
	resource.handle();
	return resource;
    }

    private ClientResource createClientResourceWithGivenLink(
	    Series<Cookie> cookies, String givenLink) throws URISyntaxException {

	final Context context = new Context();

	// the socketTimeout parameter is used in the httpClient extension that
	// we do not use
	// We can probably remove this parameter
	final String stringTimeout = String.valueOf(120000);

	context.getParameters().add("socketTimeout", stringTimeout);

	context.getParameters().add("socketConnectTimeoutMs", stringTimeout);
	context.getParameters().add("readTimeout", stringTimeout);
	// Should throw timeout exception after the specified timeout, default
	// is 2 minutes

	ClientResource resource = new ClientResource(context,
		new URI(givenLink));
	resource.getRequest().setCookies(cookies);
	return resource;
    }

    public <T extends ModelClass> T instantiateModelClass(Class<T> modelClass)
	    throws Exception {
	T modelObject = null;
	Constructor<?> constructor = null;
	;
	try {
	    constructor = modelClass.getConstructor();
	} catch (SecurityException e) {
	    throw new Exception(e.getMessage());
	} catch (NoSuchMethodException e) {
	    throw new Exception(e.getMessage());
	}

	try {
	    modelObject = (T) constructor.newInstance();
	} catch (IllegalArgumentException e) {
	    throw new Exception(e.getMessage());
	} catch (InstantiationException e) {
	    throw new Exception(e.getMessage());
	} catch (IllegalAccessException e) {
	    throw new Exception(e.getMessage());
	} catch (InvocationTargetException e) {
	    throw new Exception(e.getMessage());
	}

	modelObject.setDescription("Instantiated by HubNotificationDao");
	return modelObject;
    }

    @Override
    public <T extends ModelClass> T getFromJsonElement(Class<T> modelClass,
	    Gson gson, JsonElement elem) throws Exception {
	T modelObject = gson.fromJson(elem, modelClass);
	modelObject
		.setDescription("Instantiated via gson from JsonElement by HubNotificationDao");

	return modelObject;
    }
}
