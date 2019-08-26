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
import java.util.Date;
import java.util.TimeZone;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.jboss.msc.service.Service;
import org.jboss.msc.service.StartContext;
import org.jboss.msc.service.StartException;
import org.jboss.msc.service.StopContext;

/**
 * A service that manages ejb access-log.
 */
public class AccessLogService implements Service<AccessLogService> {
    private AccessLogHandler handler;
    private Lock handlerLock = new ReentrantLock();

    public void log(Entry entry) {
        handlerLock.lock();
        try {
            handler.log(entry);
        } finally {
            handlerLock.unlock();
        }
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

    AccessLogHandler getHandler() {
        handlerLock.lock();
        try {
            return handler;
        } finally {
            handlerLock.unlock();
        }
    }

    void setHandler(final AccessLogHandler handler) {
        handlerLock.lock();
        try {
            this.handler = handler;
        } finally {
            handlerLock.unlock();
        }
    }

    public static final class Entry {
        public enum Status {
            OK,
            FAIL,
            TIMEOUT
        }

        private final Instant instant;
        private final TimeZone timeZone;
        private final String ip;
        private final String user;
        private final String ejb;
        private final String method;
        private final String invocation;
        private final String host;
        private final int port;
        private final String protocol;
        private final String server;
        private final Status status;

        public Instant getInstant() {
            return instant;
        }

        public TimeZone getTimeZone() {
            return timeZone;
        }

        public String getIp() {
            return ip;
        }

        public String getUser() {
            return user;
        }

        public String getEjb() {
            return ejb;
        }

        public String getMethod() {
            return method;
        }

        public String getInvocation() {
            return invocation;
        }

        public String getHost() {
            return host;
        }

        public int getPort() {
            return port;
        }

        public String getProtocol() {
            return protocol;
        }

        public String getServer() {
            return server;
        }

        public Status getStatus() {
            return status;
        }

        private Entry(Builder b) {
            this.instant = b.date == null ? null : b.date.toInstant();
            this.timeZone = b.timeZone;
            this.ip = b.ip;
            this.user = b.user;
            this.ejb = b.ejb;
            this.method = b.method;
            this.invocation = b.invocation;
            this.host = b.host;
            this.port = b.port;
            this.protocol = b.protocol;
            this.server = b.server;
            this.status = b.status;
        }

        public static final class Builder {
            private Date date;
            private TimeZone timeZone;
            private String ip;
            private String user;
            private String ejb;
            private String method;
            private String invocation;
            private String host;
            private int port;
            private String protocol;
            private String server;
            private Status status;

            public Builder() {
            }

            public Entry build() {
                return new Entry(this);
            }

            public Builder date(Date date) {
                this.date = date;
                return this;
            }

            public Builder timeZone(TimeZone timeZone) {
                this.timeZone = timeZone;
                return this;
            }

            public Builder ip(String ip) {
                this.ip = ip;
                return this;
            }

            public Builder user(String user) {
                this.user = user;
                return this;
            }

            public Builder ejb(String ejb) {
                this.ejb = ejb;
                return this;
            }

            public Builder method(String method) {
                this.method = method;
                return this;
            }

            public Builder invocation(String invocation) {
                this.invocation = invocation;
                return this;
            }

            public Builder host(String host) {
                this.host = host;
                return this;
            }

            public Builder port(int port) {
                this.port = port;
                return this;
            }

            public Builder protocol(String protocol) {
                this.protocol = protocol;
                return this;
            }

            public Builder server(String server) {
                this.server = server;
                return this;
            }

            public Builder status(Status status) {
                this.status = status;
                return this;
            }
        }
    }
}
