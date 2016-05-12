package com.blackducksoftware.tools.testhubclient.dao;

public class NotificationDaoException extends Exception {

    private static final long serialVersionUID = 5024865272635265211L;

    public NotificationDaoException() {
	super();
    }

    public NotificationDaoException(String message) {
	super(message);
    }

    public NotificationDaoException(Throwable cause) {
	super(cause);
    }

    public NotificationDaoException(String message, Throwable cause) {
	super(message, cause);
    }

    public NotificationDaoException(String message, Throwable cause,
	    boolean enableSuppression, boolean writableStackTrace) {
	super(message, cause, enableSuppression, writableStackTrace);
    }

}
