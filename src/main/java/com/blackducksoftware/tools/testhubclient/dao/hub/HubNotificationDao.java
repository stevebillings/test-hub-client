package com.blackducksoftware.tools.testhubclient.dao.hub;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.net.URISyntaxException;
import java.util.AbstractMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.restlet.Response;
import org.restlet.data.Cookie;
import org.restlet.data.Method;
import org.restlet.data.Reference;
import org.restlet.resource.ClientResource;
import org.restlet.util.Series;

import com.blackducksoftware.integration.hub.HubIntRestService;
import com.blackducksoftware.integration.hub.exception.BDRestException;
import com.blackducksoftware.integration.hub.exception.HubIntegrationException;
import com.blackducksoftware.integration.hub.exception.ResourceDoesNotExistException;
import com.blackducksoftware.tools.testhubclient.ClientLogger;
import com.blackducksoftware.tools.testhubclient.dao.NotificationDao;
import com.blackducksoftware.tools.testhubclient.dao.NotificationDaoException;
import com.blackducksoftware.tools.testhubclient.json.JsonModelParser;
import com.blackducksoftware.tools.testhubclient.model.Item;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

/**
 * Communicates with the Hub to get Notifications and data they point to.
 * 
 * @author sbillings
 *
 */
public class HubNotificationDao implements NotificationDao {
    private String dateFormat;
    private ClientLogger log = new ClientLogger();
    private HubIntRestService hub;
    private String hubUrl;
    private Map<String, JsonElement> itemJsonCache; // URL -> Json Element cache
    private final JsonModelParser jsonModelParser;
    private final ClientResource reUsableResource;

    public HubNotificationDao(String hubUrl, String username, String password,
	    String dateFormat) throws HubIntegrationException,
	    URISyntaxException, BDRestException, NotificationDaoException {
	this.hubUrl = hubUrl;
	hub = new HubIntRestService(hubUrl);
	hub.setCookies(username, password);
	itemJsonCache = new HashMap<>();
	jsonModelParser = new JsonModelParser(dateFormat);
	this.dateFormat = dateFormat;

	try {
	    reUsableResource = hub.createClientResource();
	} catch (URISyntaxException e) {
	    throw new NotificationDaoException(e.getMessage());
	}
	reUsableResource.setMethod(Method.GET);
    }

    @Override
    public <T> T getFromRelativeUrl(Class<T> modelClass,
	    List<String> urlSegments,
	    Set<AbstractMap.SimpleEntry<String, String>> queryParameters)
	    throws NotificationDaoException {

	try {
	    return hub.getFromRelativeUrl(modelClass, urlSegments,
		    queryParameters);
	} catch (URISyntaxException | IOException
		| ResourceDoesNotExistException e) {
	    throw new NotificationDaoException(
		    "Error getting resource from relative url segments "
			    + urlSegments + " and query parameters "
			    + queryParameters + "; errorCode: "
			    + e.getMessage());
	}
    }

    @Override
    public <T> T getAndCacheItemsFromRelativeUrl(Class<T> modelClass,
	    List<String> urlSegments,
	    Set<AbstractMap.SimpleEntry<String, String>> queryParameters)
	    throws NotificationDaoException {

	final ClientResource resource = getClientResourceForGet(urlSegments,
		queryParameters);
	log.info("Resource: " + resource);
	int responseCode = resource.getResponse().getStatus().getCode();

	if (responseCode == 200 || responseCode == 204 || responseCode == 202) {
	    final String response = readResponseAsString(resource.getResponse());

	    Gson gson = new GsonBuilder().setDateFormat(dateFormat).create();
	    JsonParser parser = new JsonParser();
	    JsonObject json = parser.parse(response).getAsJsonObject();
	    T modelObject = gson.fromJson(json, modelClass);

	    JsonArray array = json.get("items").getAsJsonArray();
	    for (JsonElement elem : array) {
		Item genericItem = jsonModelParser.parse(Item.class, elem);
		String itemUrl = genericItem.getMeta().getHref();
		log.info("Caching: Key: " + itemUrl + "; Value: "
			+ elem.toString());
		itemJsonCache.put(itemUrl, elem);
	    }

	    return modelObject;
	} else {
	    throw new NotificationDaoException(
		    "Error getting resource from relative url segments "
			    + urlSegments + " and query parameters "
			    + queryParameters + "; errorCode: " + responseCode);
	}
    }

    @Override
    public <T extends Item> T getItemFromCache(Class<T> itemClass,
	    String itemUrl) throws NotificationDaoException {
	if (!itemJsonCache.containsKey(itemUrl)) {
	    throw new NotificationDaoException(
		    "Item with URL "
			    + itemUrl
			    + " is not in cache. Make sure it was fetched via getAndCacheItemsFromRelativeUrl(...)");
	}
	T item = jsonModelParser.parse(itemClass, itemJsonCache.get(itemUrl));
	return item;

    }

    private ClientResource getClientResourceForGet(List<String> urlSegments,
	    Set<AbstractMap.SimpleEntry<String, String>> queryParameters)
	    throws NotificationDaoException {

	Reference queryRef = new Reference(hubUrl);
	for (String urlSegment : urlSegments) {
	    queryRef.addSegment(urlSegment);
	}
	for (AbstractMap.SimpleEntry<String, String> queryParameter : queryParameters) {
	    queryRef.addQueryParameter(queryParameter.getKey(),
		    queryParameter.getValue());
	}
	reUsableResource.setReference(queryRef);

	reUsableResource.handle();
	return reUsableResource;
    }

    public <T> T getFromAbsoluteUrl(Class<T> modelClass, String url)
	    throws NotificationDaoException {
	if (url == null) {
	    return null;
	}
	try {
	    return hub.getFromAbsoluteUrl(modelClass, url);
	} catch (ResourceDoesNotExistException | URISyntaxException
		| IOException e) {
	    throw new NotificationDaoException("Error getting resource from "
		    + url + ": " + e.getMessage());
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

    private ClientResource getGetClientResourceWithGivenLink(
	    Series<Cookie> cookies, String givenLink)
	    throws NotificationDaoException {
	final ClientResource resource = getClientResourceWithGivenLink(cookies,
		givenLink);

	resource.handle();
	return resource;
    }

    private ClientResource getClientResourceWithGivenLink(
	    Series<Cookie> cookies, String givenLink)
	    throws NotificationDaoException {

	Reference queryRef = new Reference(givenLink);
	reUsableResource.setReference(queryRef);

	return reUsableResource;
    }

    @Override
    public String getVersion() throws NotificationDaoException {
	try {
	    return hub.getHubVersion();
	} catch (IOException | BDRestException | URISyntaxException e) {
	    throw new NotificationDaoException(e.getMessage());
	}
    }

}
