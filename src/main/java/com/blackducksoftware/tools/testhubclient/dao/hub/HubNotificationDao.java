package com.blackducksoftware.tools.testhubclient.dao.hub;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Set;

import org.restlet.Context;
import org.restlet.Response;
import org.restlet.data.Cookie;
import org.restlet.data.Method;
import org.restlet.resource.ClientResource;
import org.restlet.util.Series;

import com.blackducksoftware.integration.hub.HubIntRestService;
import com.blackducksoftware.integration.hub.exception.BDRestException;
import com.blackducksoftware.integration.hub.exception.HubIntegrationException;
import com.blackducksoftware.tools.testhubclient.ClientLogger;
import com.blackducksoftware.tools.testhubclient.dao.NotificationDao;
import com.blackducksoftware.tools.testhubclient.dao.NotificationDaoException;
import com.blackducksoftware.tools.testhubclient.model.ModelClass;
import com.blackducksoftware.tools.testhubclient.model.NameValuePair;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public class HubNotificationDao implements NotificationDao {
    private ClientLogger log = new ClientLogger();
    private HubIntRestService svc;

    public HubNotificationDao(String hubUrl, String username, String password)
	    throws HubIntegrationException, URISyntaxException, BDRestException {
	svc = new HubIntRestService(hubUrl);
	svc.setCookies(username, password);
    }

    @Override
    public <T extends ModelClass> T getFromRelativeUrl(Class<T> modelClass,
	    List<String> urlSegments, Set<NameValuePair> queryParameters)
	    throws NotificationDaoException {

	final ClientResource resource = createClientResourceForGet(urlSegments,
		queryParameters);
	log.info("Resource: " + resource);
	int responseCode = resource.getResponse().getStatus().getCode();

	if (responseCode == 200 || responseCode == 204 || responseCode == 202) {
	    final String response = readResponseAsString(resource.getResponse());

	    Gson gson = new GsonBuilder().create();
	    JsonParser parser = new JsonParser();
	    JsonObject json = parser.parse(response).getAsJsonObject();
	    T modelObject = gson.fromJson(json, modelClass);
	    modelObject
		    .setDescription("Instantiated via gson from JsonObject fetched from Hub by HubNotificationDao");
	    modelObject.setJsonObject(json);
	    return modelObject;
	} else {
	    throw new NotificationDaoException(
		    "Error getting resource from relative url segments "
			    + urlSegments + " and query parameters "
			    + queryParameters + "; errorCode: " + responseCode);
	}
    }

    private ClientResource createClientResourceForGet(List<String> urlSegments,
	    Set<NameValuePair> queryParameters) throws NotificationDaoException {
	ClientResource resource;
	try {
	    resource = svc.createClientResource();
	} catch (URISyntaxException e) {
	    throw new NotificationDaoException(e.getMessage());
	}
	for (String urlSegment : urlSegments) {
	    resource.addSegment(urlSegment);
	}
	for (NameValuePair queryParameter : queryParameters) {
	    resource.addQueryParameter(queryParameter.getName(),
		    queryParameter.getValue());
	}

	resource.setMethod(Method.GET);
	resource.handle();
	return resource;
    }

    public <T extends ModelClass> T getFromUrl(Class<T> modelClass, String url)
	    throws NotificationDaoException {

	if (url == null) {
	    return null;
	}

	final ClientResource resource = createGetClientResourceWithGivenLink(
		svc.getCookies(), url);

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
	    throw new NotificationDaoException("Error getting resource from "
		    + url + ": " + responseCode);
	}
    }

    private String readResponseAsString(final Response response)
	    throws NotificationDaoException {
	final StringBuilder sb = new StringBuilder();
	Reader reader;
	try {
	    reader = response.getEntity().getReader();
	} catch (IOException e1) {
	    throw new NotificationDaoException(e1.getMessage());
	}
	final BufferedReader bufReader = new BufferedReader(reader);
	try {
	    String line;
	    try {
		while ((line = bufReader.readLine()) != null) {
		    sb.append(line);
		    sb.append("\n");
		}
	    } catch (IOException e) {
		throw new NotificationDaoException(e.getMessage());
	    }
	} finally {
	    try {
		bufReader.close();
	    } catch (IOException e) {
	    }
	}
	return sb.toString();
    }

    private ClientResource createGetClientResourceWithGivenLink(
	    Series<Cookie> cookies, String givenLink)
	    throws NotificationDaoException {
	final ClientResource resource = createClientResourceWithGivenLink(
		cookies, givenLink);

	resource.setMethod(Method.GET);
	resource.handle();
	return resource;
    }

    private ClientResource createClientResourceWithGivenLink(
	    Series<Cookie> cookies, String givenLink)
	    throws NotificationDaoException {

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

	ClientResource resource;
	try {
	    resource = new ClientResource(context, new URI(givenLink));
	} catch (URISyntaxException e) {
	    throw new NotificationDaoException(e.getMessage());
	}
	resource.getRequest().setCookies(cookies);
	return resource;
    }

    @Override
    public <T extends ModelClass> T getFromJsonElement(Class<T> modelClass,
	    Gson gson, JsonElement elem) throws NotificationDaoException {
	T modelObject = gson.fromJson(elem, modelClass);
	modelObject
		.setDescription("Instantiated via gson from JsonElement by HubNotificationDao");

	return modelObject;
    }

    @Override
    public String getVersion() throws NotificationDaoException {
	try {
	    return svc.getHubVersion();
	} catch (IOException | BDRestException | URISyntaxException e) {
	    throw new NotificationDaoException(e.getMessage());
	}
    }

}
