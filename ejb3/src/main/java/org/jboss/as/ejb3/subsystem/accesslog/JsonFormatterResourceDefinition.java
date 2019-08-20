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

import org.jboss.as.controller.AbstractAddStepHandler;
import org.jboss.as.controller.AbstractRemoveStepHandler;
import org.jboss.as.controller.AttributeDefinition;
import org.jboss.as.controller.AttributeMarshallers;
import org.jboss.as.controller.PathElement;
import org.jboss.as.controller.SimpleAttributeDefinition;
import org.jboss.as.controller.SimpleAttributeDefinitionBuilder;
import org.jboss.as.controller.SimpleMapAttributeDefinition;
import org.jboss.as.ejb3.subsystem.EJB3Extension;
import org.jboss.as.ejb3.subsystem.EJB3SubsystemModel;
import org.jboss.as.ejb3.subsystem.EJB3SubsystemXMLElement;
import org.jboss.dmr.ModelNode;
import org.jboss.dmr.ModelType;

public class JsonFormatterResourceDefinition extends PatternFormatterResourceDefinition {
    private static final PathElement PATH = PathElement.pathElement(EJB3SubsystemModel.JSON_FORMATTER);

    private JsonFormatterResourceDefinition() {
        super(PATH, EJB3Extension.getResourceDescriptionResolver(EJB3SubsystemModel.JSON_FORMATTER),
                new JsonFormatterAdd(ATTRIBUTES), JsonFormatterRemove.INSTANCE);
    }

    public static final SimpleAttributeDefinition DATE_FORMAT = new SimpleAttributeDefinitionBuilder(EJB3SubsystemModel.DATE_FORMAT, ModelType.STRING, true)
            .setAllowExpression(true)
            .setRestartAllServices()
            .build();

    public static final SimpleMapAttributeDefinition META_DATA = new SimpleMapAttributeDefinition.Builder(EJB3SubsystemModel.META_DATA, ModelType.STRING, true)
            .setAttributeMarshaller(new AttributeMarshallers.PropertiesAttributeMarshaller(
                    EJB3SubsystemXMLElement.META_DATA.getLocalName(), EJB3SubsystemXMLElement.PROPERTY.getLocalName(), true))
            .build();

    public static final SimpleAttributeDefinition PRETTY_PRINT = new SimpleAttributeDefinitionBuilder(EJB3SubsystemModel.PRETTY_PRINT, ModelType.BOOLEAN, true)
            .setAllowExpression(true)
            .setDefaultValue(new ModelNode(false))
            .build();

    public static final SimpleAttributeDefinition RECORD_DELIMITER = new SimpleAttributeDefinitionBuilder(EJB3SubsystemModel.RECORD_DELIMITER, ModelType.STRING, true)
            .setAllowExpression(true)
            .setDefaultValue(new ModelNode("\n"))
            .build();
    public static final SimpleAttributeDefinition ZONE_ID = new SimpleAttributeDefinitionBuilder(EJB3SubsystemModel.ZONE_ID, ModelType.STRING, true)
            .setAllowExpression(true)
            .setRestartAllServices()
            .build();

    private static final AttributeDefinition[] ATTRIBUTES = {
            PATTERN,    // included in super class
            RECORD_DELIMITER, META_DATA, DATE_FORMAT, PRETTY_PRINT, ZONE_ID
    };

    public static final JsonFormatterResourceDefinition INSTANCE = new JsonFormatterResourceDefinition();

    @Override
    AttributeDefinition[] getAttributes() {
        return ATTRIBUTES;
    }

    private static class JsonFormatterAdd extends AbstractAddStepHandler {
        private JsonFormatterAdd(AttributeDefinition[] attributes) {
            super(attributes);
        }
    }

    private static class JsonFormatterRemove extends AbstractRemoveStepHandler {
        private static JsonFormatterRemove INSTANCE = new JsonFormatterRemove();

        private JsonFormatterRemove() {

        }
    }
}
