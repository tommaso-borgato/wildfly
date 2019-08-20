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

package org.jboss.as.ejb3.subsystem;

import java.util.List;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;

import org.jboss.as.controller.PathAddress;
import org.jboss.as.controller.PathElement;
import org.jboss.as.controller.operations.common.Util;
import org.jboss.as.controller.services.path.PathResourceDefinition;
import org.jboss.as.ejb3.subsystem.accesslog.ConsoleHandlerResourceDefinition;
import org.jboss.as.ejb3.subsystem.accesslog.FileHandlerResourceDefinition;
import org.jboss.as.ejb3.subsystem.accesslog.JsonFormatterResourceDefinition;
import org.jboss.as.ejb3.subsystem.accesslog.PatternFormatterResourceDefinition;
import org.jboss.as.ejb3.subsystem.accesslog.PeriodicHandlerResourceDefinition;
import org.jboss.as.ejb3.subsystem.accesslog.ServerLogHandlerResourceDefinition;
import org.jboss.dmr.ModelNode;
import org.jboss.staxmapper.XMLExtendedStreamReader;

import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.OP_ADDR;
import static org.jboss.as.controller.parsing.ParseUtils.missingRequired;
import static org.jboss.as.controller.parsing.ParseUtils.requireNoAttributes;
import static org.jboss.as.controller.parsing.ParseUtils.requireNoContent;
import static org.jboss.as.controller.parsing.ParseUtils.requireNoNamespaceAttribute;
import static org.jboss.as.controller.parsing.ParseUtils.unexpectedAttribute;
import static org.jboss.as.controller.parsing.ParseUtils.unexpectedElement;
import static org.jboss.as.ejb3.subsystem.EJB3SubsystemModel.CLASS;
import static org.jboss.as.ejb3.subsystem.EJB3SubsystemModel.CONSOLE_HANDLER;
import static org.jboss.as.ejb3.subsystem.EJB3SubsystemModel.FILE_HANDLER;
import static org.jboss.as.ejb3.subsystem.EJB3SubsystemModel.JSON_FORMATTER;
import static org.jboss.as.ejb3.subsystem.EJB3SubsystemModel.MODULE;
import static org.jboss.as.ejb3.subsystem.EJB3SubsystemModel.PATTERN_FORMATTER;
import static org.jboss.as.ejb3.subsystem.EJB3SubsystemModel.PERIODIC_ROTATING_FILE_HANDLER;
import static org.jboss.as.ejb3.subsystem.EJB3SubsystemModel.SERVER_INTERCEPTORS;
import static org.jboss.as.ejb3.subsystem.EJB3SubsystemModel.SERVER_LOG_HANDLER;

/**
 * Parser for ejb3:6.0 namespace.
 *
 * @author <a href="mailto:tadamski@redhat.com">Tomasz Adamski</a>
 */
public class EJB3Subsystem60Parser extends EJB3Subsystem50Parser {

    EJB3Subsystem60Parser() {
    }

    @Override
    protected EJB3SubsystemNamespace getExpectedNamespace() {
        return EJB3SubsystemNamespace.EJB3_6_0;
    }

    @Override
    protected void readElement(final XMLExtendedStreamReader reader, final EJB3SubsystemXMLElement element, final List<ModelNode> operations, final ModelNode ejb3SubsystemAddOperation) throws XMLStreamException {
        switch (element) {
            case SERVER_INTERCEPTORS: {
                parseServerInterceptors(reader, ejb3SubsystemAddOperation);
                break;
            }
            case ACCESS_LOG: {
                parseAccessLog(reader, operations, ejb3SubsystemAddOperation);
                break;
            }
            default: {
                super.readElement(reader, element, operations, ejb3SubsystemAddOperation);
            }
        }
    }

    protected void parseServerInterceptors(final XMLExtendedStreamReader reader, final ModelNode ejbSubsystemAddOperation) throws XMLStreamException {
        final ModelNode interceptors = new ModelNode();

        requireNoAttributes(reader);
        while (reader.hasNext() && reader.nextTag() != XMLStreamConstants.END_ELEMENT) {
            switch (EJB3SubsystemXMLElement.forName(reader.getLocalName())) {
                case INTERCEPTOR: {
                    parseInterceptor(reader, interceptors);
                    break;
                }
                default: {
                    throw unexpectedElement(reader);
                }
            }
        }
        requireNoContent(reader);

        ejbSubsystemAddOperation.get(SERVER_INTERCEPTORS).set(interceptors);
    }


    protected void parseInterceptor(final XMLExtendedStreamReader reader, final ModelNode interceptors) throws XMLStreamException {
        final ModelNode interceptor = new ModelNode();
        for (int i = 0; i < reader.getAttributeCount(); i++) {
            requireNoNamespaceAttribute(reader, i);
            final String value = reader.getAttributeValue(i);
            switch (EJB3SubsystemXMLAttribute.forName(reader.getAttributeLocalName(i))) {
                case MODULE: {
                    interceptor.get(MODULE).set(value);
                    break;
                }
                case CLASS: {
                    interceptor.get(CLASS).set(value);
                    break;
                }
                default: {
                    throw unexpectedAttribute(reader, i);
                }
            }
        }
        requireNoContent(reader);

        interceptors.add(interceptor);
    }

    private void parseAccessLog(final XMLExtendedStreamReader reader,
                                final List<ModelNode> operations,
                                final ModelNode ejb3SubsystemAddOperation) throws XMLStreamException {
        requireNoAttributes(reader);

        final PathAddress accessLogAddress = PathAddress.pathAddress(EJB3SubsystemModel.ACCESS_LOG_PATH);
        ModelNode accessLogAddOperation = Util.createAddOperation(accessLogAddress);

        while (reader.hasNext() && reader.nextTag() != XMLStreamConstants.END_ELEMENT) {
            switch (EJB3SubsystemXMLElement.forName(reader.getLocalName())) {
                case CONSOLE_HANDLER: {
                    parseConsoleHandler(reader, operations, accessLogAddress);
                    break;
                }
                case FILE_HANDLER: {
                    parseFileHandler(reader, operations, accessLogAddress);
                    break;
                }
                case PERIODIC_ROTATING_FILE_HANDLER: {
                    parsePeriodicRotatingFileHandler(reader, operations, accessLogAddress);
                    break;
                }
                case SERVER_LOG_HANDLER: {
                    parseServerLogHandler(reader, operations, accessLogAddress);
                    break;
                }
                case FORMATTER: {
                    parseFormatter(reader, operations, accessLogAddress);
                    break;
                }
                default: {
                    throw unexpectedElement(reader);
                }
            }
        }
        operations.add(accessLogAddOperation);
    }

    private void parseFormatter(final XMLExtendedStreamReader reader,
                                final List<ModelNode> operations,
                                final PathAddress accessLogAddress) throws XMLStreamException {
        requireNoAttributes(reader);
        while (reader.hasNext() && reader.nextTag() != XMLStreamConstants.END_ELEMENT) {
            switch (EJB3SubsystemXMLElement.forName(reader.getLocalName())) {
                case PATTERN_FORMATTER:
                    parsePatternFormatter(reader, operations, accessLogAddress);
                    break;
                case JSON_FORMATTER:
                    parseJsonFormatter(reader, operations, accessLogAddress);
                    break;
                default: {
                    throw unexpectedElement(reader);
                }
            }
        }
    }

    private void parsePatternFormatter(final XMLExtendedStreamReader reader,
                                       final List<ModelNode> operations,
                                       final PathAddress accessLogAddress) throws XMLStreamException {
        String formatterName = null;
        ModelNode formatterAddOperation = Util.createAddOperation();
        final int count = reader.getAttributeCount();
        for (int i = 0; i < count; i++) {
            requireNoNamespaceAttribute(reader, i);
            final String attributeValue = reader.getAttributeValue(i);
            final EJB3SubsystemXMLAttribute attribute = EJB3SubsystemXMLAttribute.forName(reader.getAttributeLocalName(i));
            switch (attribute) {
                case NAME:
                    formatterName = attributeValue;
                    break;
                case PATTERN:
                    PatternFormatterResourceDefinition.PATTERN.parseAndSetParameter(attributeValue, formatterAddOperation, reader);
                    break;
                default:
                    throw unexpectedAttribute(reader, i);
            }
        }
        if (formatterName == null) {
            throw missingRequired(reader, EJB3SubsystemXMLAttribute.NAME.getLocalName());
        }

        requireNoContent(reader);

        final PathAddress address = accessLogAddress.append(PathElement.pathElement(PATTERN_FORMATTER, formatterName));
        formatterAddOperation.get(OP_ADDR).set(address.toModelNode());
        operations.add(formatterAddOperation);
    }

    private void parseJsonFormatter(final XMLExtendedStreamReader reader,
                                    final List<ModelNode> operations,
                                    final PathAddress accessLogAddress) throws XMLStreamException {
        String formatterName = null;
        ModelNode formatterAddOperation = Util.createAddOperation();
        final int count = reader.getAttributeCount();
        for (int i = 0; i < count; i++) {
            requireNoNamespaceAttribute(reader, i);
            final String attributeValue = reader.getAttributeValue(i);
            final EJB3SubsystemXMLAttribute attribute = EJB3SubsystemXMLAttribute.forName(reader.getAttributeLocalName(i));
            switch (attribute) {
                case NAME:
                    formatterName = attributeValue;
                    break;
                case PATTERN:
                    PatternFormatterResourceDefinition.PATTERN.parseAndSetParameter(attributeValue, formatterAddOperation, reader);
                    break;
                case RECORD_DELIMITER:
                    JsonFormatterResourceDefinition.RECORD_DELIMITER.parseAndSetParameter(attributeValue, formatterAddOperation, reader);
                case DATE_FORMAT:
                    JsonFormatterResourceDefinition.DATE_FORMAT.parseAndSetParameter(attributeValue, formatterAddOperation, reader);
                    break;
                case PRETTY_PRINT:
                    JsonFormatterResourceDefinition.PRETTY_PRINT.parseAndSetParameter(attributeValue, formatterAddOperation, reader);
                    break;
                case ZONE_ID:
                    JsonFormatterResourceDefinition.ZONE_ID.parseAndSetParameter(attributeValue, formatterAddOperation, reader);
                    break;
                default:
                    throw unexpectedAttribute(reader, i);
            }
        }

        if (formatterName == null) {
            throw missingRequired(reader, EJB3SubsystemXMLAttribute.NAME.getLocalName());
        }

        while (reader.hasNext() && reader.nextTag() != XMLStreamConstants.END_ELEMENT) {
            switch (EJB3SubsystemXMLElement.forName(reader.getLocalName())) {
                case META_DATA:
                    parsePropertyElement(reader, formatterAddOperation, JsonFormatterResourceDefinition.META_DATA.getName());
                    break;
                default: {
                    throw unexpectedElement(reader);
                }
            }
        }

        final PathAddress address = accessLogAddress.append(PathElement.pathElement(JSON_FORMATTER, formatterName));
        formatterAddOperation.get(OP_ADDR).set(address.toModelNode());
        operations.add(formatterAddOperation);
    }


    private void parseServerLogHandler(final XMLExtendedStreamReader reader,
                                       final List<ModelNode> operations,
                                       final PathAddress accessLogAddress) throws XMLStreamException {
        String handlerName = null;
        ModelNode handlerAddOperation = Util.createAddOperation();
        final int count = reader.getAttributeCount();
        for (int i = 0; i < count; i++) {
            requireNoNamespaceAttribute(reader, i);
            final String attributeValue = reader.getAttributeValue(i);
            final EJB3SubsystemXMLAttribute attribute = EJB3SubsystemXMLAttribute.forName(reader.getAttributeLocalName(i));
            switch (attribute) {
                case NAME:
                    handlerName = attributeValue;
                    break;
                default:
                    throw unexpectedAttribute(reader, i);
            }
        }
        if (handlerName == null) {
            throw missingRequired(reader, EJB3SubsystemXMLAttribute.NAME.getLocalName());
        }

        readAndParseHandlerFormatter(reader, handlerAddOperation);

        final PathAddress address = accessLogAddress.append(PathElement.pathElement(SERVER_LOG_HANDLER, handlerName));
        handlerAddOperation.get(OP_ADDR).set(address.toModelNode());
        operations.add(handlerAddOperation);
    }

    private void parsePeriodicRotatingFileHandler(final XMLExtendedStreamReader reader,
                                                  final List<ModelNode> operations,
                                                  final PathAddress accessLogAddress) throws XMLStreamException {
        parseFileHandlerOrPeriodicRotatingFileHandler(reader, operations, accessLogAddress, PERIODIC_ROTATING_FILE_HANDLER);
    }

    private void parseFileHandler(final XMLExtendedStreamReader reader,
                                  final List<ModelNode> operations,
                                  final PathAddress accessLogAddress) throws XMLStreamException {
        parseFileHandlerOrPeriodicRotatingFileHandler(reader, operations, accessLogAddress, FILE_HANDLER);
    }

    private void parseFileHandlerOrPeriodicRotatingFileHandler(final XMLExtendedStreamReader reader,
                                                               final List<ModelNode> operations,
                                                               final PathAddress accessLogAddress,
                                                               final String handlerType) throws XMLStreamException {
        String handlerName = null;
        ModelNode handlerAddOperation = Util.createAddOperation();
        final int count = reader.getAttributeCount();
        for (int i = 0; i < count; i++) {
            requireNoNamespaceAttribute(reader, i);
            final String attributeValue = reader.getAttributeValue(i);
            final EJB3SubsystemXMLAttribute attribute = EJB3SubsystemXMLAttribute.forName(reader.getAttributeLocalName(i));
            switch (attribute) {
                case NAME:
                    handlerName = attributeValue;
                    break;
                case ENCODING:
                    ConsoleHandlerResourceDefinition.ENCODING.parseAndSetParameter(attributeValue, handlerAddOperation, reader);
                    break;
                case AUTOFLUSH:
                    ConsoleHandlerResourceDefinition.AUTOFLUSH.parseAndSetParameter(attributeValue, handlerAddOperation, reader);
                    break;
                case APPEND:
                    FileHandlerResourceDefinition.APPEND.parseAndSetParameter(attributeValue, handlerAddOperation, reader);
                    break;
                case PATH:
                    PathResourceDefinition.PATH.parseAndSetParameter(attributeValue, handlerAddOperation, reader);
                    break;
                case RELATIVE_TO:
                    PathResourceDefinition.RELATIVE_TO.parseAndSetParameter(attributeValue, handlerAddOperation, reader);
                    break;
                case SUFFIX:
                    if (handlerType.equals(PERIODIC_ROTATING_FILE_HANDLER)) {
                        PeriodicHandlerResourceDefinition.SUFFIX.parseAndSetParameter(attributeValue, handlerAddOperation, reader);
                    } else {
                        throw unexpectedAttribute(reader, i);
                    }
                    break;
                default:
                    throw unexpectedAttribute(reader, i);
            }
        }
        if (handlerName == null) {
            throw missingRequired(reader, EJB3SubsystemXMLAttribute.NAME.getLocalName());
        }

        readAndParseHandlerFormatter(reader, handlerAddOperation);

        final PathAddress address = accessLogAddress.append(PathElement.pathElement(handlerType, handlerName));
        handlerAddOperation.get(OP_ADDR).set(address.toModelNode());
        operations.add(handlerAddOperation);
    }

    private void parseConsoleHandler(final XMLExtendedStreamReader reader,
                                     final List<ModelNode> operations,
                                     final PathAddress accessLogAddress) throws XMLStreamException {
        String handlerName = null;
        ModelNode handlerAddOperation = Util.createAddOperation();
        final int count = reader.getAttributeCount();
        for (int i = 0; i < count; i++) {
            requireNoNamespaceAttribute(reader, i);
            final String attributeValue = reader.getAttributeValue(i);
            final EJB3SubsystemXMLAttribute attribute = EJB3SubsystemXMLAttribute.forName(reader.getAttributeLocalName(i));
            switch (attribute) {
                case NAME:
                    handlerName = attributeValue;
                    break;
                case ENCODING:
                    ConsoleHandlerResourceDefinition.ENCODING.parseAndSetParameter(attributeValue, handlerAddOperation, reader);
                    break;
                case AUTOFLUSH:
                    ConsoleHandlerResourceDefinition.AUTOFLUSH.parseAndSetParameter(attributeValue, handlerAddOperation, reader);
                    break;
                default:
                    throw unexpectedAttribute(reader, i);
            }
        }
        if (handlerName == null) {
            throw missingRequired(reader, EJB3SubsystemXMLAttribute.NAME.getLocalName());
        }

        readAndParseHandlerFormatter(reader, handlerAddOperation);

        final PathAddress address = accessLogAddress.append(PathElement.pathElement(CONSOLE_HANDLER, handlerName));
        handlerAddOperation.get(OP_ADDR).set(address.toModelNode());
        operations.add(handlerAddOperation);
    }

    private void readAndParseHandlerFormatter(final XMLExtendedStreamReader reader,
                                              final ModelNode handlerAddOperation) throws XMLStreamException {
        while (reader.hasNext() && reader.nextTag() != XMLStreamConstants.END_ELEMENT) {
            switch (EJB3SubsystemXMLElement.forName(reader.getLocalName())) {
                case FORMATTER:
                    parseHandlerFormatter(reader, handlerAddOperation);
                    break;
                default: {
                    throw unexpectedElement(reader);
                }
            }
        }
    }

    private void parseHandlerFormatter(final XMLExtendedStreamReader reader,
                                       final ModelNode operation) throws XMLStreamException {
        String formatterName = null;
        final int count = reader.getAttributeCount();
        for (int i = 0; i < count; i++) {
            requireNoNamespaceAttribute(reader, i);
            final String attributeValue = reader.getAttributeValue(i);
            final EJB3SubsystemXMLAttribute attribute = EJB3SubsystemXMLAttribute.forName(reader.getAttributeLocalName(i));
            switch (attribute) {
                case NAME:
                    formatterName = attributeValue;
                    ServerLogHandlerResourceDefinition.FORMATTER.parseAndSetParameter(formatterName, operation, reader);
                    break;
                default:
                    throw unexpectedAttribute(reader, i);
            }
        }
        if (formatterName == null) {
            throw missingRequired(reader, EJB3SubsystemXMLAttribute.NAME.getLocalName());
        }
        requireNoContent(reader);
    }

    private void parsePropertyElement(final XMLExtendedStreamReader reader,
                                      final ModelNode operation,
                                      final String wrapperName) throws XMLStreamException {
        while (reader.hasNext() && reader.nextTag() != XMLStreamConstants.END_ELEMENT) {
            final int cnt = reader.getAttributeCount();
            String name = null;
            String value = null;
            for (int i = 0; i < cnt; i++) {
                requireNoNamespaceAttribute(reader, i);
                final String attributeValue = reader.getAttributeValue(i);
                final EJB3SubsystemXMLAttribute attribute = EJB3SubsystemXMLAttribute.forName(reader.getAttributeLocalName(i));
                switch (attribute) {
                    case NAME: {
                        name = attributeValue;
                        break;
                    }
                    case VALUE: {
                        value = attributeValue;
                        break;
                    }
                    default:
                        throw unexpectedAttribute(reader, i);
                }
            }
            if (name == null) {
                throw missingRequired(reader, EJB3SubsystemXMLAttribute.NAME.getLocalName());
            }
            operation.get(wrapperName).add(name, (value == null ? new ModelNode() : new ModelNode(value)));
            requireNoContent(reader);
        }
    }

}
