package com.blackducksoftware.tools.testhubclient.dao.mock;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import com.blackducksoftware.tools.testhubclient.dao.NotificationDao;
import com.blackducksoftware.tools.testhubclient.dao.NotificationDaoException;
import com.blackducksoftware.tools.testhubclient.model.ModelClass;
import com.blackducksoftware.tools.testhubclient.model.NameValuePair;
import com.blackducksoftware.tools.testhubclient.model.notification.NotificationItem;
import com.blackducksoftware.tools.testhubclient.model.notification.NotificationResponse;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public class MockNotificationDao implements NotificationDao {

    private final String notificationsJsonString = "{\n"
	    + "\"totalCount\": 6,\n"
	    + "\"items\": [\n"
	    + "{\n"
	    + "\"content\": {\n"
	    + "\"projectName\": \"HUB-MAR28a\",\n"
	    + "\"projectVersionName\": \"1\",\n"
	    + "\"componentVersionsInViolation\": 1,\n"
	    + "\"componentVersionStatuses\": [\n"
	    + "{\n"
	    + "\"componentName\": \"Apache Camel\",\n"
	    + "\"bomComponentVersionPolicyStatus\": \"http://eng-hub-valid03.dc1.lan/api/projects/0a03fd84-d442-4eb4-829d-fb36abd03b08/versions/cf6c140a-8c91-495a-851f-d7b196e98f46/components/17064005-756d-4313-8472-e0c34e404b00/policy-status\",\n"
	    + "\"component\": \"http://eng-hub-valid03.dc1.lan/api/components/17064005-756d-4313-8472-e0c34e404b00\"\n"
	    + "}\n"
	    + "],\n"
	    + "\"projectVersion\": \"http://eng-hub-valid03.dc1.lan/api/projects/0a03fd84-d442-4eb4-829d-fb36abd03b08/versions/cf6c140a-8c91-495a-851f-d7b196e98f46\"\n"
	    + "},\n"
	    + "\"contentType\": \"application/json\",\n"
	    + "\"type\": \"RULE_VIOLATION\",\n"
	    + "\"createdAt\": \"2016-05-01T11:00:09.830Z\",\n"
	    + "\"_meta\": {\n"
	    + "\"allow\": [\n"
	    + "\"GET\"\n"
	    + "],\n"
	    + "\"href\": \"http://eng-hub-valid03.dc1.lan/api/notifications/e5071453-cbae-457f-b84c-9d60c79d0409\"\n"
	    + "}\n" + "}\n" + "]\n" + "}";

    public MockNotificationDao() {
    }

    @Override
    public String getVersion() throws NotificationDaoException {
	return "Mock DAO version";
    }

    @Override
    public <T extends ModelClass> T getFromRelativeUrl(Class<T> modelClass,
	    List<String> urlSegments, Set<NameValuePair> queryParameters)
	    throws NotificationDaoException {

	if (modelClass == NotificationResponse.class) {
	    NotificationResponse notifResponse = new NotificationResponse();
	    List<NotificationItem> notificationItems = new ArrayList<>();
	    NotificationItem notif = new NotificationItem();
	    notif.setType("VULNERABILITY");
	    notificationItems.add(notif);
	    notifResponse.setItems(notificationItems);

	    JsonParser parser = new JsonParser();
	    JsonObject jsonObject = parser.parse(notificationsJsonString)
		    .getAsJsonObject();
	    notifResponse.setJsonObject(jsonObject);
	    return (T) (notifResponse);
	} else if (modelClass == NotificationItem.class) {
	    NotificationItem notif = new NotificationItem();
	    notif.setType("VULNERABILITY");
	    return (T) (notif);
	} else {
	    throw new UnsupportedOperationException(
		    "getFromRelativeUrl() not implemented for class "
			    + modelClass.getName());
	}
    }

    @Override
    public <T extends ModelClass> T getFromAbsoluteUrl(Class<T> modelClass,
	    String url) throws NotificationDaoException {
	throw new UnsupportedOperationException(
		"getFromAbsoluteUrl() not implemented");
    }

}
