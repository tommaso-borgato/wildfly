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

import java.io.File;
import java.nio.file.Path;

class FileHandler extends ConsoleHandler implements AccessLogHandler {
    private boolean append;

    private Path path;

    private File relativeTo;

    public FileHandler(final String name) {
        super(name);
    }

    public FileHandler(final String name, final AccessLogFormatter formatter) {
        super(name, formatter);
    }

    public FileHandler(final String name, final AccessLogFormatter formatter, final boolean append, final Path path, final File relativeTo) {
        super(name, formatter);
        this.append = append;
        this.path = path;
        this.relativeTo = relativeTo;
    }

    @Override
    public void log(final AccessLogService.Entry entry) {
        final String formattedText = formatter.format(entry);
        //TODO  output to dest
    }
}
