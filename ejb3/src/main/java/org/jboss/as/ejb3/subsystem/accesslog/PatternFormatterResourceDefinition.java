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

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import org.jboss.as.controller.AttributeDefinition;
import org.jboss.as.controller.DefaultAttributeMarshaller;
import org.jboss.as.controller.ObjectTypeAttributeDefinition;
import org.jboss.as.controller.OperationContext;
import org.jboss.as.controller.OperationFailedException;
import org.jboss.as.controller.OperationStepHandler;
import org.jboss.as.controller.PathElement;
import org.jboss.as.controller.ReadResourceNameOperationStepHandler;
import org.jboss.as.controller.SimpleAttributeDefinition;
import org.jboss.as.controller.SimpleAttributeDefinitionBuilder;
import org.jboss.as.controller.SimpleResourceDefinition;
import org.jboss.as.controller.operations.global.WriteAttributeHandler;
import org.jboss.as.controller.operations.validation.StringLengthValidator;
import org.jboss.as.controller.registry.ManagementResourceRegistration;
import org.jboss.as.controller.transform.description.ResourceTransformationDescriptionBuilder;
import org.jboss.as.ejb3.subsystem.EJB3SubsystemModel;
import org.jboss.dmr.ModelNode;
import org.jboss.dmr.ModelType;
import org.jboss.logmanager.config.FormatterConfiguration;
import org.jboss.logmanager.config.LogContextConfiguration;
import org.jboss.logmanager.formatters.PatternFormatter;

import static org.jboss.as.logging.Logging.createOperationFailure;

public class PatternFormatterResourceDefinition extends SimpleResourceDefinition {
    public static final SimpleAttributeDefinition NAME = new SimpleAttributeDefinitionBuilder(EJB3SubsystemModel.NAME, ModelType.STRING, false)
            .setValidator(new StringLengthValidator(1, false))
            .setRestartAllServices()
            .build();

    public static final SimpleAttributeDefinition PATTERN = new SimpleAttributeDefinitionBuilder(EJB3SubsystemModel.PATTERN, ModelType.STRING, true)
            .setDefaultValue(new ModelNode("common"))
            .setValidator(new StringLengthValidator(1, true))
            .setRestartAllServices()
            .build();

    private static final PathElement PATH = PathElement.pathElement(EJB3SubsystemModel.PATTERN_FORMATTER);

    private static final AttributeDefinition[] ATTRIBUTES = {PATTERN};


    /**
     * A step handler to add a pattern formatter
     */
    private static final OperationStepHandler ADD = new LoggingOperations.LoggingAddOperationStepHandler(ATTRIBUTES) {

        @Override
        public void performRuntime(final OperationContext context, final ModelNode operation, final ModelNode model, final LogContextConfiguration logContextConfiguration) throws OperationFailedException {
            final String name = context.getCurrentAddressValue();
            if (name.endsWith(DEFAULT_FORMATTER_SUFFIX)) {
                throw LoggingLogger.ROOT_LOGGER.illegalFormatterName();
            }
            FormatterConfiguration configuration = logContextConfiguration.getFormatterConfiguration(name);
            if (configuration == null) {
                LoggingLogger.ROOT_LOGGER.tracef("Adding formatter '%s' at '%s'", name, context.getCurrentAddress());
                configuration = logContextConfiguration.addFormatterConfiguration(null, PatternFormatter.class.getName(), name);
            }

            for (PropertyAttributeDefinition attribute : ATTRIBUTES) {
                attribute.setPropertyValue(context, model, configuration);
            }
        }
    };

    private static final OperationStepHandler WRITE = new LoggingWriteAttributeHandler(ATTRIBUTES) {

        @Override
        protected boolean applyUpdate(final OperationContext context, final String attributeName, final String addressName, final ModelNode value, final LogContextConfiguration logContextConfiguration) {
            final FormatterConfiguration configuration = logContextConfiguration.getFormatterConfiguration(addressName);
            for (PropertyAttributeDefinition attribute : ATTRIBUTES) {
                if (attribute.getName().equals(attributeName)) {
                    configuration.setPropertyValueString(attribute.getPropertyName(), value.asString());
                    break;
                }
            }
            return false;
        }
    };

    /**
     * A step handler to remove
     */
    private static final OperationStepHandler REMOVE = new LoggingOperations.LoggingRemoveOperationStepHandler() {

        @Override
        public void performRuntime(final OperationContext context, final ModelNode operation, final ModelNode model, final LogContextConfiguration logContextConfiguration) throws OperationFailedException {
            final String name = context.getCurrentAddressValue();
            final FormatterConfiguration configuration = logContextConfiguration.getFormatterConfiguration(name);
            if (configuration == null) {
                throw createOperationFailure(LoggingLogger.ROOT_LOGGER.formatterNotFound(name));
            }
            logContextConfiguration.removeFormatterConfiguration(name);
        }
    };

    public static final PatternFormatterResourceDefinition INSTANCE = new PatternFormatterResourceDefinition();

    public PatternFormatterResourceDefinition() {
        super(new SimpleResourceDefinition.Parameters(PATH, LoggingExtension.getResourceDescriptionResolver(NAME))
                .setAddHandler(ADD)
                .setRemoveHandler(REMOVE)
                .setCapabilities(Capabilities.FORMATTER_CAPABILITY));
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


    @Override
    public void registerTransformers(final KnownModelVersion modelVersion, final ResourceTransformationDescriptionBuilder resourceBuilder, final ResourceTransformationDescriptionBuilder loggingProfileBuilder) {
        // do nothing by default
    }
}
