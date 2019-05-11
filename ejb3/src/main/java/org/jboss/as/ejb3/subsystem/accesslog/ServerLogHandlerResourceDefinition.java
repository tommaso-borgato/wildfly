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

import java.util.logging.Handler;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import org.jboss.as.controller.AttributeDefinition;
import org.jboss.as.controller.DefaultAttributeMarshaller;
import org.jboss.as.controller.OperationStepHandler;
import org.jboss.as.controller.PathElement;
import org.jboss.as.controller.ReadResourceNameOperationStepHandler;
import org.jboss.as.controller.SimpleAttributeDefinition;
import org.jboss.as.controller.SimpleAttributeDefinitionBuilder;
import org.jboss.as.controller.SimpleResourceDefinition;
import org.jboss.as.controller.operations.global.WriteAttributeHandler;
import org.jboss.as.controller.operations.validation.EnumValidator;
import org.jboss.as.controller.operations.validation.StringLengthValidator;
import org.jboss.as.controller.registry.ManagementResourceRegistration;
import org.jboss.as.ejb3.subsystem.EJB3SubsystemModel;
import org.jboss.as.logging.CommonAttributes;
import org.jboss.as.logging.ConfigurationProperty;
import org.jboss.as.logging.KnownModelVersion;
import org.jboss.as.logging.LoggingExtension;
import org.jboss.as.logging.LoggingOperations;
import org.jboss.as.logging.PropertyAttributeDefinition;
import org.jboss.as.logging.TransformerResourceDefinition;
import org.jboss.as.logging.capabilities.Capabilities;
import org.jboss.as.logging.formatters.PatternFormatterResourceDefinition;
import org.jboss.as.logging.logmanager.PropertySorter;
import org.jboss.dmr.ModelNode;
import org.jboss.dmr.ModelType;
import org.jboss.logmanager.handlers.ConsoleHandler;

import static org.jboss.as.logging.CommonAttributes.ENABLED;
import static org.jboss.as.logging.CommonAttributes.ENCODING;
import static org.jboss.as.logging.CommonAttributes.FILTER;
import static org.jboss.as.logging.CommonAttributes.FILTER_SPEC;
import static org.jboss.as.logging.CommonAttributes.LEVEL;
import static org.jboss.as.logging.CommonAttributes.NAME;

public class ServerLogHandlerResourceDefinition extends SimpleResourceDefinition {
    public static final ServerLogHandlerResourceDefinition INSTANCE = new ServerLogHandlerResourceDefinition();

    public static final SimpleAttributeDefinition NAME = new SimpleAttributeDefinitionBuilder(EJB3SubsystemModel.NAME, ModelType.STRING, false)
            .setValidator(new StringLengthValidator(1, false))
            .setRestartAllServices()
            .build();


    public static final SimpleAttributeDefinition FORMATTER = new SimpleAttributeDefinitionBuilder(EJB3SubsystemModel.FORMATTER, ModelType.STRING, true)
            .setValidator(new StringLengthValidator(1, true))
            .setRestartAllServices()
            .build();

    private static final AttributeDefinition[] ATTRIBUTES = {FORMATTER};

    public ServerLogHandlerResourceDefinition() {
        super(CONSOLE_HANDLER_PATH, ConsoleHandler.class, ATTRIBUTES);
    }

    protected ServerLogHandlerResourceDefinition(final PathElement path,
                                                 final Class<? extends Handler> type,
                                                 final AttributeDefinition[] attributes) {
        this(createParameters(path, type, PropertySorter.NO_OP, attributes), true, PropertySorter.NO_OP, null, attributes);
    }

    protected ServerLogHandlerResourceDefinition(final PathElement path,
                                                 final boolean registerLegacyOps,
                                                 final Class<? extends Handler> type,
                                                 final PropertySorter propertySorter,
                                                 final AttributeDefinition[] attributes) {
        this(createParameters(path, type, propertySorter, attributes), registerLegacyOps, propertySorter, null, attributes);
    }

    protected ServerLogHandlerResourceDefinition(final PathElement path,
                                                 final Class<? extends Handler> type,
                                                 final AttributeDefinition[] attributes,
                                                 final ConfigurationProperty<?>... constructionProperties) {
        this(createParameters(path, type, PropertySorter.NO_OP, attributes, constructionProperties),
                true, PropertySorter.NO_OP, null, attributes);
    }

    protected ServerLogHandlerResourceDefinition(final SimpleResourceDefinition.Parameters parameters,
                                                 final boolean registerLegacyOps,
                                                 final PropertySorter propertySorter,
                                                 final AttributeDefinition[] readOnlyAttributes,
                                                 final AttributeDefinition[] writableAttributes) {
        super(parameters);
        this.registerLegacyOps = registerLegacyOps;
        this.writableAttributes = writableAttributes;
        writeHandler = new HandlerOperations.LogHandlerWriteAttributeHandler(propertySorter, this.writableAttributes);
        this.readOnlyAttributes = readOnlyAttributes;
        this.propertySorter = propertySorter;
    }

    @Override
    public void registerAttributes(final ManagementResourceRegistration resourceRegistration) {
        for (AttributeDefinition def : ATTRIBUTES) {
            resourceRegistration.registerReadWriteAttribute(def, null, WriteAttributeHandler.INSTANCE);
        }

        // Be careful with this attribute. It needs to show up in the "add" operation param list so ops from legacy
        // scripts will validate. It does because it's registered as an attribute but is not setResourceOnly(true)
        // so DefaultResourceAddDescriptionProvider adds it to the param list
        resourceRegistration.registerReadOnlyAttribute(NAME, ReadResourceNameOperationStepHandler.INSTANCE);
    }

    /**
     * Creates the default {@linkplain org.jboss.as.controller.SimpleResourceDefinition.Parameters parameters} for
     * creating the source.
     *
     * @param path                   the resource path
     * @param type                   the known type of the resource or {@code null} if the type is unknown
     * @param propertySorter         the property sorter
     * @param addAttributes          the attributes for the add operation step handler
     * @param constructionProperties the construction properties required for the handler
     * @return the default parameters
     */
    private static SimpleResourceDefinition.Parameters createParameters(final PathElement path, final Class<? extends Handler> type,
                                                                        final PropertySorter propertySorter, final AttributeDefinition[] addAttributes,
                                                                        final ConfigurationProperty<?>... constructionProperties) {
        return new SimpleResourceDefinition.Parameters(path, LoggingExtension.getResourceDescriptionResolver(path.getKey()))
                .setAddHandler(new HandlerOperations.HandlerAddOperationStepHandler(propertySorter, type, addAttributes, constructionProperties))
                .setRemoveHandler(HandlerOperations.REMOVE_HANDLER)
                .setCapabilities(Capabilities.HANDLER_CAPABILITY);
    }
}
