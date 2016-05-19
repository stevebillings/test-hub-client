package com.blackducksoftware.tools.testhubclient.dao;

import java.util.AbstractMap;
import java.util.List;
import java.util.Set;

import com.blackducksoftware.tools.testhubclient.model.Item;

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
	    Set<AbstractMap.SimpleEntry<String, String>> queryParameters)
	    throws NotificationDaoException;

    /**
     * Get a resource consisting of a list of items (that will be cached) from
     * the given relative URL.
     * 
     * Caches the underlying data for each item so it can be re-requested as a
     * different type. This is useful when the list of items contains items of
     * various types (each a subclass of a single generic type). The requester
     * can request the list as a list of the generic type, but later re-request
     * each item as the more specific type (without incurring a round-trip to
     * the server).
     * 
     * @param modelClass
     * @param urlSegments
     * @param queryParameters
     * @param metaDeserializer
     * @return
     * @throws NotificationDaoException
     */
    <T> T getAndCacheItemsFromRelativeUrl(Class<T> modelClass,
	    List<String> urlSegments,
	    Set<AbstractMap.SimpleEntry<String, String>> queryParameters)
	    throws NotificationDaoException;

    /**
     * Reload an item from the item cache as the given type.
     * 
     * TODO: write more here...
     * 
     * @param modelClass
     * @param itemUrl
     * @return
     * @throws NotificationDaoException
     */
    <T extends Item> T getItemFromCache(Class<T> itemClass, String itemUrl)
	    throws NotificationDaoException;

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
    <T> T getFromAbsoluteUrl(Class<T> modelClass, String url)
	    throws NotificationDaoException;

}
