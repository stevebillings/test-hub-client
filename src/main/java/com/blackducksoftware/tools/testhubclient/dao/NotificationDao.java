package com.blackducksoftware.tools.testhubclient.dao;

import java.util.AbstractMap;
import java.util.List;
import java.util.Set;

import com.blackducksoftware.tools.testhubclient.model.notification.NotificationItem;

/**
 * Gets Notifications and data they point to.
 * 
 * @author sbillings
 *
 */
public interface NotificationDao {

	/**
	 * Get the version string from the data source.
	 * 
	 * @return
	 * @throws Exception
	 */
	String getVersion() throws NotificationDaoException;

	/**
	 * Get a resource from the given relative URL.
	 * 
	 * @param modelClass
	 * @param urlSegments
	 * @param queryParameters
	 * @param metaDeserializer
	 * @return
	 * @throws NotificationDaoException
	 */
	<T> T getFromRelativeUrl(Class<T> modelClass, List<String> urlSegments,
			Set<AbstractMap.SimpleEntry<String, String>> queryParameters) throws NotificationDaoException;

	/**
	 * Get a resource from the given absolutely URL. Use the given deserializer
	 * for the _meta element.
	 * 
	 * @param modelClass
	 * @param url
	 * @param metaDeserializer
	 * @return
	 * @throws NotificationDaoException
	 */
	<T> T getFromAbsoluteUrl(Class<T> modelClass, String url) throws NotificationDaoException;

	/**
	 * Get a list of notifications for the given date range.
	 * 
	 * @param startDate
	 * @param endDate
	 * @param limit
	 * @return
	 * @throws NotificationDaoException
	 */
	public List<NotificationItem> getNotifications(String startDate, String endDate, int limit)
			throws NotificationDaoException;
}
