/*******************************************************************************
 * Black Duck Software Suite SDK
 * Copyright (C) 2016 Black Duck Software, Inc.
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA
 *******************************************************************************/
package com.blackducksoftware.tools.testhubclient;

import java.io.PrintWriter;
import java.io.Serializable;
import java.io.StringWriter;

import com.blackducksoftware.integration.hub.logging.IntLogger;
import com.blackducksoftware.integration.hub.logging.LogLevel;

public class ClientLogger implements IntLogger, Serializable {

    private static final long serialVersionUID = 6030328026877850956L;
    private LogLevel level = LogLevel.INFO;

    @Override
    public void debug(final String txt) {
	if (LogLevel.isLoggable(level, LogLevel.DEBUG)) {
	    printLog(txt, null);
	}
    }

    @Override
    public void debug(final String txt, final Throwable e) {
	if (LogLevel.isLoggable(level, LogLevel.DEBUG)) {
	    printLog(txt, e);
	}
    }

    @Override
    public void error(final Throwable e) {
	if (LogLevel.isLoggable(level, LogLevel.ERROR)) {
	    printLog(null, e);
	}
    }

    @Override
    public void error(final String txt) {
	if (LogLevel.isLoggable(level, LogLevel.ERROR)) {
	    printLog(txt, null);
	}
    }

    @Override
    public void error(final String txt, final Throwable e) {
	if (LogLevel.isLoggable(level, LogLevel.ERROR)) {
	    printLog(txt, e);
	}
    }

    @Override
    public void info(final String txt) {
	if (LogLevel.isLoggable(level, LogLevel.INFO)) {
	    printLog(txt, null);
	}
    }

    @Override
    public void trace(final String txt) {
	if (LogLevel.isLoggable(level, LogLevel.TRACE)) {
	    printLog(txt, null);
	}
    }

    @Override
    public void trace(final String txt, final Throwable e) {
	if (LogLevel.isLoggable(level, LogLevel.TRACE)) {
	    printLog(txt, e);
	}
    }

    @Override
    public void warn(final String txt) {
	if (LogLevel.isLoggable(level, LogLevel.WARN)) {
	    printLog(txt, null);
	}
    }

    private void printLog(final String txt, final Throwable e) {
	if (txt != null) {

	    System.out.println("[" + level.name() + "] " + txt);

	}
	if (e != null) {
	    final StringWriter sw = new StringWriter();
	    e.printStackTrace(new PrintWriter(sw));

	    System.out.println(sw.toString());

	}
    }

    @Override
    public void setLogLevel(LogLevel logLevel) {
	this.level = logLevel;
    }

    @Override
    public LogLevel getLogLevel() {
	return level;
    }

}
