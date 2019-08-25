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

import org.jboss.as.ejb3.logging.EjbLogger;

class ServerLogHandler implements AccessLogHandler {
    final String name;
    AccessLogFormatter formatter;

    public ServerLogHandler(final String name) {
        this.name = name;
    }

    public ServerLogHandler(final String name, final AccessLogFormatter formatter) {
        this.name = name;
        this.formatter = formatter;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public AccessLogFormatter getFormatter() {
        return formatter;
    }

    @Override
    public void setFormatter(final AccessLogFormatter formatter) {
        this.formatter = formatter;
    }

    @Override
    public void log(final AccessLogService.Entry entry) {
        final String formattedText = formatter.format(entry);
        EjbLogger.EJB3_ACCESS_LOGGER.access(formattedText);
    }
}
