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

final class PatternFormatter extends AbstractAccessLogFormatter implements AccessLogFormatter {
    static final String NO_VALUE = "-";
    static final String FIELD_DELIMITER = " ";

    private static final ZoneId zoneId = ZoneId.systemDefault();

    PatternFormatter(final String name) {
        super(name);
    }

    PatternFormatter(final String name, final String pattern) {
        super(name, pattern);
    }

    @Override
    public String format(final AccessLogService.Entry entry) {
        final StringBuilder sb = new StringBuilder();
        for (String k : keys) {
            appendField(sb, k, entry);
        }
        return sb.substring(1);
    }

    @Override
    ZoneId getZoneId() {
        return zoneId;
    }

    @Override
    void append(final StringBuilder sb, final String key, final String text) {
        sb.append(FIELD_DELIMITER);
        sb.append(text == null || text.isEmpty() ? NO_VALUE : text);
    }
}
