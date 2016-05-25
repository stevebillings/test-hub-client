package com.blackducksoftware.tools.testhubclient.dao.hub;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.blackducksoftware.integration.hub.HubIntRestService;
import com.blackducksoftware.integration.hub.exception.BDRestException;
import com.blackducksoftware.integration.hub.exception.HubIntegrationException;
import com.blackducksoftware.integration.hub.exception.ResourceDoesNotExistException;
import com.blackducksoftware.integration.hub.item.PolymorphicHubItemListService;
import com.blackducksoftware.integration.hub.rest.RestConnection;
import com.blackducksoftware.tools.testhubclient.dao.NotificationDao;
import com.blackducksoftware.tools.testhubclient.dao.NotificationDaoException;
import com.blackducksoftware.tools.testhubclient.model.notification.NotificationItem;
import com.blackducksoftware.tools.testhubclient.model.notification.PolicyOverrideNotificationItem;
import com.blackducksoftware.tools.testhubclient.model.notification.RuleViolationNotificationItem;
import com.blackducksoftware.tools.testhubclient.model.notification.VulnerabilityNotificationItem;
import com.google.gson.reflect.TypeToken;

/**
 * Communicates with the Hub to get Notifications and data they point to.
 *
 * @author sbillings
 *
 */
public class HubNotificationDao implements NotificationDao {

	private final RestConnection restConnection;
	private final HubIntRestService hub;
	PolymorphicHubItemListService<NotificationItem> hubItemListParser;

	public HubNotificationDao(final String hubUrl, final String username, final String password, final String dateFormat)
			throws HubIntegrationException, URISyntaxException, BDRestException, NotificationDaoException {

		restConnection = new RestConnection(hubUrl);
		restConnection.setCookies(username, password);

		hub = new HubIntRestService(restConnection); // used to get Hub version

		final TypeToken<NotificationItem> typeToken = new TypeToken<NotificationItem>() {
		};
		final Map<String, Class<? extends NotificationItem>> typeToSubclassMap = new HashMap<>();
		typeToSubclassMap.put("VULNERABILITY", VulnerabilityNotificationItem.class);
		typeToSubclassMap.put("RULE_VIOLATION", RuleViolationNotificationItem.class);
		typeToSubclassMap.put("POLICY_OVERRIDE", PolicyOverrideNotificationItem.class);

		hubItemListParser = new PolymorphicHubItemListService<NotificationItem>(restConnection, NotificationItem.class,
				typeToken,
				typeToSubclassMap);
	}

	@Override
	public <T> T getFromRelativeUrl(final Class<T> modelClass, final List<String> urlSegments,
			final Set<AbstractMap.SimpleEntry<String, String>> queryParameters) throws NotificationDaoException {

		try {
			return restConnection.getFromRelativeUrl(modelClass, urlSegments, queryParameters);
		} catch (URISyntaxException | IOException | ResourceDoesNotExistException | BDRestException e) {
			throw new NotificationDaoException("Error getting resource from relative url segments " + urlSegments
					+ " and query parameters " + queryParameters + "; errorCode: " + e.getMessage());
		}
	}

	@Override
	public <T> T getFromAbsoluteUrl(final Class<T> modelClass, final String url) throws NotificationDaoException {
		if (url == null) {
			return null;
		}
		try {
			return restConnection.getFromAbsoluteUrl(modelClass, url);
		} catch (ResourceDoesNotExistException | URISyntaxException | IOException | BDRestException e) {
			throw new NotificationDaoException("Error getting resource from " + url + ": " + e.getMessage());
		}
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
	public List<NotificationItem> getNotifications(final String startDate, final String endDate, final int limit)
			throws NotificationDaoException {

		final List<String> urlSegments = new ArrayList<>();
		urlSegments.add("api");
		urlSegments.add("notifications");

		final Set<AbstractMap.SimpleEntry<String, String>> queryParameters = new HashSet<>();
		queryParameters.add(new AbstractMap.SimpleEntry<String, String>("startDate", startDate));
		queryParameters.add(new AbstractMap.SimpleEntry<String, String>("endDate", endDate));
		queryParameters.add(new AbstractMap.SimpleEntry<String, String>("limit", String.valueOf(limit)));
		try {
			return hubItemListParser.httpGetItemList(urlSegments, queryParameters);
		} catch (IOException | URISyntaxException | ResourceDoesNotExistException | BDRestException e) {
			throw new NotificationDaoException("Error parsing NotificationItemList: " + e.getMessage(), e);
		}
	}

}
