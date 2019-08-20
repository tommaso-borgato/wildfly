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

import java.nio.file.Path;

import org.jboss.as.controller.services.path.PathManager;
import org.jboss.msc.service.Service;
import org.jboss.msc.service.StartContext;
import org.jboss.msc.service.StartException;
import org.jboss.msc.service.StopContext;
import org.jboss.msc.value.InjectedValue;

/**
 * A service that manages ejb access-log.
 */
public class AccessLogService implements Service<AccessLogService> {
    private final String pattern;
    private final String path;
    private final String pathRelativeTo;
    private final String filePrefix;
    private final String fileSuffix;

    private PathManager.Callback.Handle callbackHandle;

    private Path directory;

    private final InjectedValue<PathManager> pathManager = new InjectedValue<PathManager>();

    AccessLogService(String pattern) {
        this.pattern = pattern;
        this.path = null;
        this.pathRelativeTo = null;
        this.filePrefix = null;
        this.fileSuffix = null;
    }

    AccessLogService(String pattern, String path, String pathRelativeTo, String filePrefix, String fileSuffix, boolean rotate) {
        this.pattern = pattern;
        this.path = path;
        this.pathRelativeTo = pathRelativeTo;
        this.filePrefix = filePrefix;
        this.fileSuffix = fileSuffix;
    }

    @Override
    public void start(StartContext context) throws StartException {
    }

    @Override
    public void stop(StopContext context) {
    }

    @Override
    public AccessLogService getValue() throws IllegalStateException, IllegalArgumentException {
        return this;
    }

    InjectedValue<PathManager> getPathManager() {
        return pathManager;
    }

    String getPath() {
        return path;
    }
}
