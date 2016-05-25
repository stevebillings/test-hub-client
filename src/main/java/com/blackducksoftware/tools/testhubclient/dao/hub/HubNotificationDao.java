package com.blackducksoftware.tools.testhubclient.dao.hub;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.net.URISyntaxException;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.restlet.Response;
import org.restlet.data.Method;
import org.restlet.data.Reference;
import org.restlet.resource.ClientResource;

import com.blackducksoftware.integration.hub.HubIntRestService;
import com.blackducksoftware.integration.hub.exception.BDRestException;
import com.blackducksoftware.integration.hub.exception.HubIntegrationException;
import com.blackducksoftware.integration.hub.exception.ResourceDoesNotExistException;
import com.blackducksoftware.integration.hub.item.HubItemListParser;
import com.blackducksoftware.integration.hub.rest.RestConnection;
import com.blackducksoftware.tools.testhubclient.ClientLogger;
import com.blackducksoftware.tools.testhubclient.dao.NotificationDao;
import com.blackducksoftware.tools.testhubclient.dao.NotificationDaoException;
import com.blackducksoftware.tools.testhubclient.json.JsonModelParser;
import com.blackducksoftware.tools.testhubclient.model.notification.NotificationItem;
import com.blackducksoftware.tools.testhubclient.model.notification.PolicyOverrideNotificationItem;
import com.blackducksoftware.tools.testhubclient.model.notification.RuleViolationNotificationItem;
import com.blackducksoftware.tools.testhubclient.model.notification.VulnerabilityNotificationItem;
import com.google.gson.JsonElement;
import com.google.gson.reflect.TypeToken;

/**
 * Communicates with the Hub to get Notifications and data they point to.
 * 
 * @author sbillings
 *
 */
public class HubNotificationDao implements NotificationDao {
	private String dateFormat;
	private ClientLogger log = new ClientLogger();
	private RestConnection restConnection;
	private HubIntRestService hub;
	private String hubUrl;
	private Map<String, JsonElement> itemJsonCache; // URL -> Json Element cache
	private final JsonModelParser jsonModelParser;
	private final ClientResource reUsableResource;
	HubItemListParser<NotificationItem> hubItemListParser;

	public HubNotificationDao(String hubUrl, String username, String password, String dateFormat)
			throws HubIntegrationException, URISyntaxException, BDRestException, NotificationDaoException {
		this.hubUrl = hubUrl;

		restConnection = new RestConnection(hubUrl);
		restConnection.setCookies(username, password);

		hub = new HubIntRestService(restConnection);
		itemJsonCache = new HashMap<>();
		jsonModelParser = new JsonModelParser(dateFormat);
		this.dateFormat = dateFormat;

		try {
			reUsableResource = restConnection.createClientResource();
		} catch (URISyntaxException e) {
			throw new NotificationDaoException(e.getMessage());
		}
		reUsableResource.setMethod(Method.GET);

		TypeToken<NotificationItem> typeToken = new TypeToken<NotificationItem>() {
		};
		Map<String, Class<? extends NotificationItem>> typeToSubclassMap = new HashMap<>();
		typeToSubclassMap.put("VULNERABILITY", VulnerabilityNotificationItem.class);
		typeToSubclassMap.put("RULE_VIOLATION", RuleViolationNotificationItem.class);
		typeToSubclassMap.put("POLICY_OVERRIDE", PolicyOverrideNotificationItem.class);

		hubItemListParser = new HubItemListParser<NotificationItem>(restConnection, NotificationItem.class, typeToken,
				typeToSubclassMap);
	}

	@Override
	public <T> T getFromRelativeUrl(Class<T> modelClass, List<String> urlSegments,
			Set<AbstractMap.SimpleEntry<String, String>> queryParameters) throws NotificationDaoException {

		try {
			return restConnection.getFromRelativeUrl(modelClass, urlSegments, queryParameters);
		} catch (URISyntaxException | IOException | ResourceDoesNotExistException e) {
			throw new NotificationDaoException("Error getting resource from relative url segments " + urlSegments
					+ " and query parameters " + queryParameters + "; errorCode: " + e.getMessage());
		}
	}

	private ClientResource getClientResourceForGet(List<String> urlSegments,
			Set<AbstractMap.SimpleEntry<String, String>> queryParameters) throws NotificationDaoException {

		Reference queryRef = new Reference(hubUrl);
		for (String urlSegment : urlSegments) {
			queryRef.addSegment(urlSegment);
		}
		for (AbstractMap.SimpleEntry<String, String> queryParameter : queryParameters) {
			queryRef.addQueryParameter(queryParameter.getKey(), queryParameter.getValue());
		}
		reUsableResource.setReference(queryRef);

		reUsableResource.handle();
		return reUsableResource;
	}

	public <T> T getFromAbsoluteUrl(Class<T> modelClass, String url) throws NotificationDaoException {
		if (url == null) {
			return null;
		}
		try {
			return restConnection.getFromAbsoluteUrl(modelClass, url);
		} catch (ResourceDoesNotExistException | URISyntaxException | IOException e) {
			throw new NotificationDaoException("Error getting resource from " + url + ": " + e.getMessage());
		}
	}

	private String readResponseAsString(final Response response) throws NotificationDaoException {
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

	@Override
	public String getVersion() throws NotificationDaoException {
		try {
			return hub.getHubVersion();
		} catch (IOException | BDRestException | URISyntaxException e) {
			throw new NotificationDaoException(e.getMessage());
		}
	}

	@Override
	public List<NotificationItem> getNotifications(String startDate, String endDate, int limit)
			throws NotificationDaoException {

		List<String> urlSegments = new ArrayList<>();
		urlSegments.add("api");
		urlSegments.add("notifications");

		Set<AbstractMap.SimpleEntry<String, String>> queryParameters = new HashSet<>();
		queryParameters.add(new AbstractMap.SimpleEntry<String, String>("startDate", startDate));
		queryParameters.add(new AbstractMap.SimpleEntry<String, String>("endDate", endDate));
		queryParameters.add(new AbstractMap.SimpleEntry<String, String>("limit", String.valueOf(limit)));
		try {
			return hubItemListParser.parseItemList(urlSegments, queryParameters);
		} catch (IOException | URISyntaxException | ResourceDoesNotExistException e) {
			throw new NotificationDaoException("Error parsing NotificationItemList: " + e.getMessage(), e);
		}
	}

}
