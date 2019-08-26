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

import java.time.ZoneId;

final class JsonFormatter extends AbstractAccessLogFormatter implements AccessLogFormatter {
    private ZoneId zoneId;

    public JsonFormatter(final String name) {
        super(name);
    }

    public JsonFormatter(final String name, final String pattern) {
        super(name, pattern);
    }

    public JsonFormatter(final String name, final String pattern, final ZoneId zoneId) {
        super(name, pattern);
        this.zoneId = zoneId;
    }

    @Override
    public String format(final AccessLogService.Entry entry) {
        return null;
    }

    @Override
    ZoneId getZoneId() {
        return zoneId;
    }

    void setZoneId(final ZoneId zoneId) {
        this.zoneId = zoneId;
    }

    @Override
    void append(final StringBuilder sb, final String key, final String text) {
        //TODO
    }
}
