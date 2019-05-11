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

import org.jboss.as.controller.AttributeDefinition;
import org.jboss.as.controller.PathElement;
import org.jboss.as.controller.SimpleAttributeDefinition;
import org.jboss.as.controller.SimpleAttributeDefinitionBuilder;
import org.jboss.as.controller.operations.global.WriteAttributeHandler;
import org.jboss.as.controller.registry.ManagementResourceRegistration;
import org.jboss.as.controller.services.path.PathInfoHandler;
import org.jboss.as.controller.services.path.PathResourceDefinition;
import org.jboss.as.controller.services.path.ResolvePathHandler;
import org.jboss.as.ejb3.subsystem.EJB3SubsystemModel;
import org.jboss.as.logging.Logging;
import org.jboss.dmr.ModelNode;
import org.jboss.dmr.ModelType;
import org.jboss.logmanager.handlers.FileHandler;

public class FileHandlerResourceDefinition extends ConsoleHandlerResourceDefinition {

    public static final String NAME = "file-handler";

    public static final SimpleAttributeDefinition APPEND = new SimpleAttributeDefinitionBuilder(EJB3SubsystemModel.APPEND, ModelType.BOOLEAN, true)
            .setDefaultValue(new ModelNode(true))
            .setAllowExpression(true)
            .setRestartAllServices()
            .build();

    private static final PathElement FILE_HANDLER_PATH = PathElement.pathElement(NAME);

    private static final AttributeDefinition[] ATTRIBUTES = {
                    // included in super class: FORMATTER, AUTOFLUSH, ENCODING,
                    APPEND, PathResourceDefinition.PATH, PathResourceDefinition.RELATIVE_TO};

    public static final FileHandlerResourceDefinition INSTANCE = new FileHandlerResourceDefinition();

    public FileHandlerResourceDefinition() {
        super(path, type, resolvePathHandler, diskUsagePathHandler, attributes);
    }

    public FileHandlerResourceDefinition(final ResolvePathHandler resolvePathHandler, final boolean includeLegacyAttributes) {
        super(FILE_HANDLER_PATH, FileHandler.class, resolvePathHandler, null, (
                includeLegacyAttributes ? Logging.join(ATTRIBUTES, LEGACY_ATTRIBUTES) : ATTRIBUTES));
    }

    public FileHandlerResourceDefinition(final ResolvePathHandler resolvePathHandler, final PathInfoHandler diskUsagePathHandler, final boolean includeLegacyAttributes) {
        super(FILE_HANDLER_PATH, FileHandler.class, resolvePathHandler, diskUsagePathHandler, (
                includeLegacyAttributes ? Logging.join(ATTRIBUTES, LEGACY_ATTRIBUTES) : ATTRIBUTES));
    }

    @Override
    public void registerAttributes(final ManagementResourceRegistration resourceRegistration) {
        super.registerAttributes(resourceRegistration);
        for (AttributeDefinition def : ATTRIBUTES) {
            resourceRegistration.registerReadWriteAttribute(def, null, WriteAttributeHandler.INSTANCE);
        }
    }
}
