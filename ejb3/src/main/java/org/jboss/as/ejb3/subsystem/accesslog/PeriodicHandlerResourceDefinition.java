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
import org.jboss.as.controller.PathElement;
import org.jboss.as.controller.SimpleAttributeDefinition;
import org.jboss.as.controller.SimpleAttributeDefinitionBuilder;
import org.jboss.as.controller.services.path.PathManager;
import org.jboss.as.controller.services.path.PathResourceDefinition;
import org.jboss.as.ejb3.subsystem.EJB3Extension;
import org.jboss.as.ejb3.subsystem.EJB3SubsystemModel;
import org.jboss.dmr.ModelNode;
import org.jboss.dmr.ModelType;

public class PeriodicHandlerResourceDefinition extends FileHandlerResourceDefinition {
    private static final PathElement PERIODIC_HANDLER_PATH = PathElement.pathElement(EJB3SubsystemModel.PERIODIC_ROTATING_FILE_HANDLER);

    public static final SimpleAttributeDefinition SUFFIX = new SimpleAttributeDefinitionBuilder(EJB3SubsystemModel.SUFFIX, ModelType.STRING, true)
            .setAllowExpression(true)
            .setRestartAllServices()
            .build();

    private static final AttributeDefinition[] ATTRIBUTES = {
            // declared in super class:
            FORMATTER, AUTOFLUSH, ENCODING, APPEND, PathResourceDefinition.PATH, PathResourceDefinition.RELATIVE_TO,
            SUFFIX
    };

    public PeriodicHandlerResourceDefinition(final PathManager pathManager) {
        super(PERIODIC_HANDLER_PATH, EJB3Extension.getResourceDescriptionResolver(EJB3SubsystemModel.PERIODIC_ROTATING_FILE_HANDLER),
                PeriodicHandlerAdd.INSTANCE, PeriodicHandlerRemove.INSTANCE, pathManager);
    }

    @Override
    AttributeDefinition[] getAttributes() {
        return ATTRIBUTES;
    }

    private static class PeriodicHandlerAdd extends AbstractAddStepHandler {
        static PeriodicHandlerAdd INSTANCE = new PeriodicHandlerAdd();

        private PeriodicHandlerAdd() {}

        @Override
        protected void populateModel(ModelNode operation, ModelNode model) throws OperationFailedException {
            for (AttributeDefinition attr : PeriodicHandlerResourceDefinition.ATTRIBUTES) {
                attr.validateAndSet(operation, model);
            }
        }

        @Override
        protected void performRuntime(final OperationContext context, final ModelNode operation, final ModelNode model) throws OperationFailedException {

        }
    }

    private static class PeriodicHandlerRemove extends AbstractRemoveStepHandler {
        private static PeriodicHandlerRemove INSTANCE = new PeriodicHandlerRemove();

        private PeriodicHandlerRemove() {

        }
    }

}
