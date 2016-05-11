package com.blackducksoftware.tools.testhubclient;

public class Statistics {
    private final int notificationCount;
    private final int ticketCount;
    private final int duplicateCount;

    public Statistics(int notificationCount, int ticketCount, int duplicateCount) {
	super();
	this.notificationCount = notificationCount;
	this.ticketCount = ticketCount;
	this.duplicateCount = duplicateCount;
    }

    public int getNotificationCount() {
	return notificationCount;
    }

    public int getTicketCount() {
	return ticketCount;
    }

    public int getDuplicateCount() {
	return duplicateCount;
    }

    @Override
    public String toString() {
	return "Statistics [notificationCount=" + notificationCount
		+ ", ticketCount=" + ticketCount + ", duplicateCount="
		+ duplicateCount + "]";
    }

}
