/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2019, Red Hat, Inc., and individual contributors
 * as indicated by the @author tags. See the copyright.txt file in the
 * distribution for a full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */

package org.jboss.as.ejb3.subsystem.accesslog;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

abstract class AbstractAccessLogFormatter implements AccessLogFormatter {
    static final String DEFAULT_PATTERN = "ip user date time ejb method";
    static final String DEFAULT_PATTERN_ALIAS = "common";
    private static final String SPLIT_PATTERN = " * ";

    final String name;
    String pattern;
    final String[] keys;

    private static final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("hh:mm:ss,S");
    private static final DateTimeFormatter offsetFormatter = DateTimeFormatter.ofPattern("Z");

    AbstractAccessLogFormatter(final String name) {
        this(name, DEFAULT_PATTERN);
    }

    AbstractAccessLogFormatter(final String name, final String pattern) {
        this.name = name;
        this.pattern = pattern != null ? pattern : DEFAULT_PATTERN;
        keys = pattern.split(SPLIT_PATTERN);
    }

    abstract ZoneId getZoneId();

    abstract void append(final StringBuilder sb, final String key, final String text);

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getPattern() {
        return pattern;
    }

    @Override
    public void setPattern(final String pattern) {
        this.pattern = pattern;
    }

    void appendField(final StringBuilder sb, final String key, final AccessLogService.Entry entry) {
        if (key.equalsIgnoreCase("date")) {
            final Instant val = entry.getInstant();
            append(sb, key, val == null ? null : val.atZone(getZoneId()).format(dateFormatter));
        } else if (key.equalsIgnoreCase("time")) {
            final Instant val = entry.getInstant();
            append(sb, key, val == null ? null : val.atZone(getZoneId()).format(timeFormatter));
        } else if (key.equalsIgnoreCase("timeZone")) {
            final Instant val = entry.getInstant();
            append(sb, key, val == null ? null : val.atZone(getZoneId()).format(offsetFormatter));
        } else if (key.equalsIgnoreCase("ip")) {
            append(sb, key, entry.getIp());
        } else if (key.equalsIgnoreCase("user")) {
            append(sb, key, entry.getUser());
        } else if (key.equalsIgnoreCase("ejb")) {
            append(sb, key, entry.getEjb());
        } else if (key.equalsIgnoreCase("method")) {
            append(sb, key, entry.getMethod());
        } else if (key.equalsIgnoreCase("invocation")) {
            append(sb, key, entry.getInvocation());
        } else if (key.equalsIgnoreCase("host")) {
            append(sb, key, entry.getHost());
        } else if (key.equalsIgnoreCase("port")) {
            append(sb, key, String.valueOf(entry.getPort()));
        } else if (key.equalsIgnoreCase("protocol")) {
            append(sb, key, entry.getProtocol());
        } else if (key.equalsIgnoreCase("server")) {
            append(sb, key, entry.getServer());
        } else if (key.equalsIgnoreCase("status")) {
            final AccessLogService.Entry.Status val = entry.getStatus();
            append(sb, key, val == null ? null : val.toString());
        }
    }

}
