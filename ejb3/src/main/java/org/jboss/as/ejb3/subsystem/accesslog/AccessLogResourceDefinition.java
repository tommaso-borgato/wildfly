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

import java.util.List;

import org.jboss.as.controller.SimpleAttributeDefinition;
import org.jboss.as.controller.SimpleAttributeDefinitionBuilder;
import org.jboss.as.controller.SimpleResourceDefinition;
import org.jboss.as.controller.access.constraint.SensitivityClassification;
import org.jboss.as.controller.access.management.AccessConstraintDefinition;
import org.jboss.as.controller.access.management.SensitiveTargetAccessConstraintDefinition;
import org.jboss.as.controller.capability.DynamicNameMappers;
import org.jboss.as.controller.capability.RuntimeCapability;
import org.jboss.as.controller.operations.validation.StringLengthValidator;
import org.jboss.as.controller.registry.ManagementResourceRegistration;
import org.jboss.as.ejb3.subsystem.EJB3Extension;
import org.jboss.as.ejb3.subsystem.EJB3SubsystemModel;
import org.jboss.dmr.ModelNode;
import org.jboss.dmr.ModelType;

/**
 * Resource definition for ejb3 subsystem access-log.
 */
public class AccessLogResourceDefinition extends SimpleResourceDefinition {
    public static final String ACCESS_LOG_CAPABILITY = "org.wildfly.ejb3.access-log";

    static final RuntimeCapability<Void> ACCESS_LOG_RUNTIME_CAPABILITY = RuntimeCapability.Builder.of(ACCESS_LOG_CAPABILITY, true, AccessLogService.class)
              .setDynamicNameMapper(DynamicNameMappers.GRAND_PARENT)
              .build();

    public static final AccessLogResourceDefinition INSTANCE = new AccessLogResourceDefinition();
    private final List<AccessConstraintDefinition> accessConstraints;


    private AccessLogResourceDefinition() {
        super(new Parameters(EJB3SubsystemModel.ACCESS_LOG_PATH, EJB3Extension.getResourceDescriptionResolver(EJB3SubsystemModel.ACCESS_LOG))
                .setAddHandler(AccessLogAdd.INSTANCE)
                .setRemoveHandler(AccessLogRemove.INSTANCE)
                .setCapabilities(ACCESS_LOG_RUNTIME_CAPABILITY)
        );
        SensitivityClassification sc = new SensitivityClassification(EJB3Extension.SUBSYSTEM_NAME, "ejb-access-log", false, false, false);
        this.accessConstraints = new SensitiveTargetAccessConstraintDefinition(sc).wrapAsList();
    }

    @Override
    public List<AccessConstraintDefinition> getAccessConstraints() {
        return accessConstraints;
    }

    @Override
    public void registerChildren(final ManagementResourceRegistration resourceRegistration) {
        resourceRegistration.registerSubModel(ConsoleHandlerResourceDefinition.INSTANCE);
        resourceRegistration.registerSubModel(ServerLogHandlerResourceDefinition.INSTANCE);
        resourceRegistration.registerSubModel(FileHandlerResourceDefinition.INSTANCE);
        resourceRegistration.registerSubModel(PeriodicHandlerResourceDefinition.INSTANCE);
        resourceRegistration.registerSubModel(PatternFormatterResourceDefinition.INSTANCE);
        resourceRegistration.registerSubModel(JsonFormatterResourceDefinition.INSTANCE);
    }
}
