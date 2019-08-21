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
import org.jboss.as.controller.SimpleAttributeDefinition;
import org.jboss.as.controller.SimpleAttributeDefinitionBuilder;
import org.jboss.as.controller.descriptions.ResourceDescriptionResolver;
import org.jboss.as.controller.operations.validation.StringLengthValidator;
import org.jboss.as.ejb3.subsystem.EJB3Extension;
import org.jboss.as.ejb3.subsystem.EJB3SubsystemModel;
import org.jboss.dmr.ModelNode;
import org.jboss.dmr.ModelType;

public class ConsoleHandlerResourceDefinition extends ServerLogHandlerResourceDefinition {
    private static final PathElement CONSOLE_HANDLER_PATH = PathElement.pathElement(EJB3SubsystemModel.CONSOLE_HANDLER);

    public static final SimpleAttributeDefinition ENCODING = new SimpleAttributeDefinitionBuilder(EJB3SubsystemModel.ENCODING, ModelType.STRING, true)
            .setValidator(new StringLengthValidator(1, true))
            .setRestartAllServices()
            .build();
    public static final SimpleAttributeDefinition AUTOFLUSH = new SimpleAttributeDefinitionBuilder(EJB3SubsystemModel.AUTOFLUSH, ModelType.BOOLEAN)
            .setRequired(false)
            .setDefaultValue(new ModelNode(false))
            .setAllowExpression(true)
            .setRestartAllServices()
            .build();

    private static final AttributeDefinition[] ATTRIBUTES = {
            FORMATTER,  // declared in super class
            AUTOFLUSH, ENCODING};

    public static final ConsoleHandlerResourceDefinition INSTANCE = new ConsoleHandlerResourceDefinition();

    public ConsoleHandlerResourceDefinition() {
        this(CONSOLE_HANDLER_PATH, EJB3Extension.getResourceDescriptionResolver(EJB3SubsystemModel.CONSOLE_HANDLER),
                ConsoleHandlerAdd.INSTANCE, ConsoleHandlerRemove.INSTANCE);
    }

    public ConsoleHandlerResourceDefinition(final PathElement pathElement, final ResourceDescriptionResolver descriptionResolver,
                                              final OperationStepHandler addHandler, final OperationStepHandler removeHandler) {
        super(pathElement, descriptionResolver, addHandler, removeHandler);
    }

    @Override
    AttributeDefinition[] getAttributes() {
        return ATTRIBUTES;
    }

    private static class ConsoleHandlerAdd extends AbstractAddStepHandler {
        static ConsoleHandlerAdd INSTANCE = new ConsoleHandlerAdd();

        private ConsoleHandlerAdd() {}

        @Override
        protected void populateModel(ModelNode operation, ModelNode model) throws OperationFailedException {
            for (AttributeDefinition attr : ConsoleHandlerResourceDefinition.ATTRIBUTES) {
                attr.validateAndSet(operation, model);
            }
        }

        @Override
        protected void performRuntime(final OperationContext context, final ModelNode operation, final ModelNode model) throws OperationFailedException {

        }
    }

    private static class ConsoleHandlerRemove extends AbstractRemoveStepHandler {
        private static ConsoleHandlerRemove INSTANCE = new ConsoleHandlerRemove();

        private ConsoleHandlerRemove() {

        }
    }
}
