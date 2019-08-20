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
import org.jboss.as.controller.OperationContext;
import org.jboss.as.controller.OperationFailedException;
import org.jboss.as.controller.OperationStepHandler;
import org.jboss.as.controller.PathElement;
import org.jboss.as.controller.ReadResourceNameOperationStepHandler;
import org.jboss.as.controller.ReloadRequiredWriteAttributeHandler;
import org.jboss.as.controller.SimpleAttributeDefinition;
import org.jboss.as.controller.SimpleAttributeDefinitionBuilder;
import org.jboss.as.controller.SimpleResourceDefinition;
import org.jboss.as.controller.descriptions.ResourceDescriptionResolver;
import org.jboss.as.controller.operations.validation.StringLengthValidator;
import org.jboss.as.controller.registry.ManagementResourceRegistration;
import org.jboss.as.ejb3.subsystem.EJB3Extension;
import org.jboss.as.ejb3.subsystem.EJB3SubsystemModel;
import org.jboss.dmr.ModelNode;
import org.jboss.dmr.ModelType;

public class ServerLogHandlerResourceDefinition extends SimpleResourceDefinition {
    private static final PathElement SERVER_LOG_HANDLER_PATH = PathElement.pathElement(EJB3SubsystemModel.SERVER_LOG_HANDLER);

    public static final SimpleAttributeDefinition NAME = new SimpleAttributeDefinitionBuilder(EJB3SubsystemModel.NAME, ModelType.STRING, false)
            .setValidator(new StringLengthValidator(1, false))
            .setRestartAllServices()
            .build();


    public static final SimpleAttributeDefinition FORMATTER = new SimpleAttributeDefinitionBuilder(EJB3SubsystemModel.FORMATTER, ModelType.STRING, true)
            .setValidator(new StringLengthValidator(1, true))
            .setRestartAllServices()
            .build();

    private static final AttributeDefinition[] ATTRIBUTES = {FORMATTER};

    public static final ServerLogHandlerResourceDefinition INSTANCE = new ServerLogHandlerResourceDefinition();

    public ServerLogHandlerResourceDefinition() {
        this(SERVER_LOG_HANDLER_PATH, EJB3Extension.getResourceDescriptionResolver(EJB3SubsystemModel.SERVER_LOG_HANDLER),
                new ServerLogHandlerAdd(ATTRIBUTES), ServerLogHandlerRemove.INSTANCE);
    }

    public ServerLogHandlerResourceDefinition(final PathElement pathElement, final ResourceDescriptionResolver descriptionResolver,
                                              final OperationStepHandler addHandler, final OperationStepHandler removeHandler) {
        super(pathElement, descriptionResolver, addHandler, removeHandler);
    }

    AttributeDefinition[] getAttributes() {
        return ATTRIBUTES;
    }

    @Override
    public void registerAttributes(final ManagementResourceRegistration resourceRegistration) {
        for (AttributeDefinition attr : getAttributes()) {
            resourceRegistration.registerReadWriteAttribute(attr, null, new ReloadRequiredWriteAttributeHandler(attr));
        }

        // Be careful with this attribute. It needs to show up in the "add" operation param list so ops from legacy
        // scripts will validate. It does because it's registered as an attribute but is not setResourceOnly(true)
        // so DefaultResourceAddDescriptionProvider adds it to the param list
        resourceRegistration.registerReadOnlyAttribute(NAME, ReadResourceNameOperationStepHandler.INSTANCE);
    }

    private static class ServerLogHandlerAdd extends AbstractAddStepHandler {
        private ServerLogHandlerAdd(AttributeDefinition[] attributes) {
            super(attributes);
        }

        @Override
        protected void performRuntime(final OperationContext context, final ModelNode operation, final ModelNode model) throws OperationFailedException {

        }
    }

    private static class ServerLogHandlerRemove extends AbstractRemoveStepHandler {
        private static ServerLogHandlerRemove INSTANCE = new ServerLogHandlerRemove();

        private ServerLogHandlerRemove() {

        }
    }
}
