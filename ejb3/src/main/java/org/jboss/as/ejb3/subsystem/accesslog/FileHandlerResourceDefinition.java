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
import org.jboss.as.controller.registry.ManagementResourceRegistration;
import org.jboss.as.controller.services.path.PathManager;
import org.jboss.as.controller.services.path.PathResourceDefinition;
import org.jboss.as.controller.services.path.ResolvePathHandler;
import org.jboss.as.ejb3.subsystem.EJB3Extension;
import org.jboss.as.ejb3.subsystem.EJB3SubsystemModel;
import org.jboss.dmr.ModelNode;
import org.jboss.dmr.ModelType;

public class FileHandlerResourceDefinition extends ConsoleHandlerResourceDefinition {
    private static final PathElement FILE_HANDLER_PATH = PathElement.pathElement(EJB3SubsystemModel.FILE_HANDLER);

    public static final SimpleAttributeDefinition APPEND = new SimpleAttributeDefinitionBuilder(EJB3SubsystemModel.APPEND, ModelType.BOOLEAN, true)
            .setDefaultValue(new ModelNode(true))
            .setAllowExpression(true)
            .setRestartAllServices()
            .build();

    private static final AttributeDefinition[] ATTRIBUTES = {
            FORMATTER, AUTOFLUSH, ENCODING,      // declared in super class
            APPEND, PathResourceDefinition.PATH, PathResourceDefinition.RELATIVE_TO};

    final PathManager pathManager;

    public FileHandlerResourceDefinition(final PathManager pathManager) {
        this(FILE_HANDLER_PATH, EJB3Extension.getResourceDescriptionResolver(EJB3SubsystemModel.FILE_HANDLER),
                FileHandlerAdd.INSTANCE, FileHandlerRemove.INSTANCE, pathManager);
    }

    public FileHandlerResourceDefinition(final PathElement pathElement, final ResourceDescriptionResolver descriptionResolver,
                                         final OperationStepHandler addHandler, final OperationStepHandler removeHandler,
                                         final PathManager pathManager) {
        super(pathElement, descriptionResolver, addHandler, removeHandler);
        this.pathManager = pathManager;
    }

    @Override
    AttributeDefinition[] getAttributes() {
        return ATTRIBUTES;
    }

    @Override
    public void registerOperations(final ManagementResourceRegistration resourceRegistration) {
        super.registerOperations(resourceRegistration);
        if (pathManager != null) {
            final ResolvePathHandler resolvePathHandler = ResolvePathHandler.Builder.of(pathManager)
                    .setPathAttribute(PathResourceDefinition.PATH)
                    .setRelativeToAttribute(PathResourceDefinition.RELATIVE_TO)
                    .build();
            resourceRegistration.registerOperationHandler(resolvePathHandler.getOperationDefinition(), resolvePathHandler);
        }
    }

    private static class FileHandlerAdd extends AbstractAddStepHandler {
        static FileHandlerAdd INSTANCE = new FileHandlerAdd();

        private FileHandlerAdd() {}

        @Override
        protected void populateModel(ModelNode operation, ModelNode model) throws OperationFailedException {
            for (AttributeDefinition attr : FileHandlerResourceDefinition.ATTRIBUTES) {
                attr.validateAndSet(operation, model);
            }
        }

        @Override
        protected void performRuntime(final OperationContext context, final ModelNode operation, final ModelNode model) throws OperationFailedException {

        }
    }

    private static class FileHandlerRemove extends AbstractRemoveStepHandler {
        private static FileHandlerRemove INSTANCE = new FileHandlerRemove();

        private FileHandlerRemove() {

        }
    }
}
