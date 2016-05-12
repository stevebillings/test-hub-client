package com.blackducksoftware.tools.testhubclient.dao;

import java.util.List;
import java.util.Set;

import com.blackducksoftware.tools.testhubclient.model.ModelClass;
import com.blackducksoftware.tools.testhubclient.model.NameValuePair;

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
    String getVersion() throws Exception;

    /**
     * Get a resource from the given relative URL.
     * 
     * @param modelClass
     * @param urlSegments
     * @param queryParameters
     * @return
     * @throws NotificationDaoException
     */
    <T extends ModelClass> T getFromRelativeUrl(Class<T> modelClass,
	    List<String> urlSegments, Set<NameValuePair> queryParameters)
	    throws NotificationDaoException;

    /**
     * Get a resource from the given absolute URL.
     * 
     * @param modelClass
     * @param url
     * @return
     * @throws NotificationDaoException
     */
    <T extends ModelClass> T getFromAbsoluteUrl(Class<T> modelClass, String url)
	    throws NotificationDaoException;

}
