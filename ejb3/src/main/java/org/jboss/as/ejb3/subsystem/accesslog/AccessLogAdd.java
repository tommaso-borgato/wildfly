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
import org.jboss.as.controller.AttributeDefinition;
import org.jboss.as.controller.OperationContext;
import org.jboss.as.controller.OperationFailedException;
import org.jboss.dmr.ModelNode;

public class AccessLogAdd extends AbstractAddStepHandler {

    private AccessLogAdd() {
    }

    AccessLogAdd(AttributeDefinition... attributes) {
        super(attributes);
    }

    static final AccessLogAdd INSTANCE = new AccessLogAdd();

    @Override
    protected void performRuntime(OperationContext context, ModelNode operation, ModelNode model) throws OperationFailedException {

//        final PathAddress address = context.getCurrentAddress();
//        final PathAddress hostAddress = address.getParent();
//        final PathAddress serverAddress = hostAddress.getParent();
//        final String worker = AccessLogResourceDefinition.WORKER.resolveModelAttribute(context, model).asString();
//        final String pattern = AccessLogResourceDefinition.PATTERN.resolveModelAttribute(context, model).asString();
//        final String directory = AccessLogResourceDefinition.DIRECTORY.resolveModelAttribute(context, model).asString();
//        final String filePrefix = AccessLogResourceDefinition.PREFIX.resolveModelAttribute(context, model).asString();
//        final String fileSuffix = AccessLogResourceDefinition.SUFFIX.resolveModelAttribute(context, model).asString();
//        final boolean useServerLog = AccessLogResourceDefinition.USE_SERVER_LOG.resolveModelAttribute(context, model).asBoolean();
//        final boolean rotate = AccessLogResourceDefinition.ROTATE.resolveModelAttribute(context, model).asBoolean();
//        final boolean extended = AccessLogResourceDefinition.EXTENDED.resolveModelAttribute(context, model).asBoolean();
//        final ModelNode relativeToNode = AccessLogResourceDefinition.RELATIVE_TO.resolveModelAttribute(context, model);
//        final String relativeTo = relativeToNode.isDefined() ? relativeToNode.asString() : null;
//
//        Predicate predicate = null;
//        ModelNode predicateNode = AccessLogResourceDefinition.PREDICATE.resolveModelAttribute(context, model);
//        if(predicateNode.isDefined()) {
//            predicate = Predicates.parse(predicateNode.asString(), getClass().getClassLoader());
//        }
//
//        final AccessLogService service;
//        if (useServerLog) {
//            service = new AccessLogService(pattern, extended, predicate);
//        } else {
//            service = new AccessLogService(pattern, directory, relativeTo, filePrefix, fileSuffix, rotate, extended, predicate);
//        }
//
//        final String serverName = serverAddress.getLastElement().getValue();
//        final String hostName = hostAddress.getLastElement().getValue();
//
//        final CapabilityServiceBuilder<AccessLogService> builder = context.getCapabilityServiceTarget().addCapability(AccessLogResourceDefinition.ACCESS_LOG_CAPABILITY, service)
//                .addCapabilityRequirement(Capabilities.REF_IO_WORKER, XnioWorker.class, service.getWorker(), worker)
//                .addDependency(PathManagerService.SERVICE_NAME, PathManager.class, service.getPathManager())
//                .addCapabilityRequirement(Capabilities.CAPABILITY_HOST, Host.class, service.getHost(), serverName, hostName);
//        //only for backward compatibility
//        builder.addAliases(UndertowService.accessLogServiceName(serverName, hostName));
//
//        builder.setInitialMode(ServiceController.Mode.ACTIVE)
//                .install();
    }
}
