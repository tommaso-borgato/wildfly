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
import org.jboss.as.controller.AttributeMarshallers;
import org.jboss.as.controller.PathElement;
import org.jboss.as.controller.SimpleAttributeDefinition;
import org.jboss.as.controller.SimpleAttributeDefinitionBuilder;
import org.jboss.as.controller.SimpleMapAttributeDefinition;
import org.jboss.as.controller.SimpleResourceDefinition;
import org.jboss.as.controller.operations.global.WriteAttributeHandler;
import org.jboss.as.controller.registry.ManagementResourceRegistration;
import org.jboss.as.ejb3.subsystem.EJB3SubsystemModel;
import org.jboss.dmr.ModelNode;
import org.jboss.dmr.ModelType;
import org.jboss.logmanager.formatters.JsonFormatter;

public class JsonFormatterResourceDefinition extends SimpleResourceDefinition {
    public static final String NAME = "json-formatter";
    private static final PathElement PATH = PathElement.pathElement(NAME);

    public static final JsonFormatterResourceDefinition INSTANCE = new JsonFormatterResourceDefinition();

    private JsonFormatterResourceDefinition() {
        super(PATH, NAME, JsonFormatter.class);
    }

    public static final SimpleAttributeDefinition DATE_FORMAT = new SimpleAttributeDefinitionBuilder(EJB3SubsystemModel.DATE_FORMAT, ModelType.STRING, true)
            .setAllowExpression(true)
            .setRestartAllServices()
            .build();

    public static final SimpleMapAttributeDefinition META_DATA = new SimpleMapAttributeDefinition.Builder("meta-data", ModelType.STRING, true)
            .setAttributeMarshaller(new AttributeMarshallers.PropertiesAttributeMarshaller("meta-data", "property", true))
            .build();

    public static final SimpleAttributeDefinition PRETTY_PRINT = new SimpleAttributeDefinitionBuilder("pretty-print", ModelType.BOOLEAN, true)
            .setAllowExpression(true)
            .setDefaultValue(new ModelNode(false))
            .build();

    public static final SimpleAttributeDefinition RECORD_DELIMITER = new SimpleAttributeDefinitionBuilder("record-delimiter", ModelType.STRING, true)
            .setAllowExpression(true)
            .setDefaultValue(new ModelNode("\n"))
            .build();
    public static final SimpleAttributeDefinition ZONE_ID = new SimpleAttributeDefinitionBuilder(EJB3SubsystemModel.ZONE_ID, ModelType.STRING, true)
            .setAllowExpression(true)
            .setRestartAllServices()
            .build();

    private static final AttributeDefinition[] ATTRIBUTES = {
            // included in super class: PATTERN
            RECORD_DELIMITER, META_DATA, DATE_FORMAT, PRETTY_PRINT, ZONE_ID
    };

    @Override
    public void registerAttributes(final ManagementResourceRegistration resourceRegistration) {
        super.registerAttributes(resourceRegistration);
        for (AttributeDefinition def : ATTRIBUTES) {
            resourceRegistration.registerReadWriteAttribute(def, null, WriteAttributeHandler.INSTANCE);
        }

    }

}
