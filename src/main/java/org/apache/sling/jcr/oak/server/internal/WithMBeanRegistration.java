/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.sling.jcr.oak.server.internal;

import org.apache.jackrabbit.oak.spi.whiteboard.Registration;
import org.apache.jackrabbit.oak.spi.whiteboard.Tracker;
import org.apache.jackrabbit.oak.spi.whiteboard.Whiteboard;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.management.*;
import java.lang.management.ManagementFactory;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;

/**
 * Wraps a {@link Whiteboard} and adds functionality for registering and unregistering MBeans.
 */
class WithMBeanRegistration implements Whiteboard {

    private static final Logger LOG = LoggerFactory.getLogger(WithMBeanRegistration.class);
    private final Whiteboard wrappedWhiteboard;

    private final Consumer<ObjectName> unregisterMBean = objectName -> {
        try {
            LOG.debug("Will attempt to unregister MBean: {}", objectName);
            MBeanServer mBeanServer = ManagementFactory.getPlatformMBeanServer();
            mBeanServer.unregisterMBean(objectName);
            LOG.debug("MBean {} unregistered", objectName);
        } catch (InstanceNotFoundException | MBeanRegistrationException exception) {
            String errorMessage = String.format(
                    "Failed to unregister MBean with with this ObjectName: [%s]", objectName
            );
            LOG.error(errorMessage, exception);
        }
    };

    WithMBeanRegistration(Whiteboard wrappedWhiteboard) {
        this.wrappedWhiteboard = wrappedWhiteboard;
    }

    @Override
    public <T> Registration register(Class<T> type, T service, Map<?, ?> properties) {
        Registration registration = wrappedWhiteboard.register(type, service, properties);
        Optional<ObjectName> objectNameNullable = registerMBean(type, service, properties);
        return () -> {
            registration.unregister();
            objectNameNullable.ifPresent(unregisterMBean);
        };
    }

    @Override
    public <T> Tracker<T> track(Class<T> type) {
        return wrappedWhiteboard.track(type);
    }

    @Override
    public <T> Tracker<T> track(Class<T> type, Map<String, String> filterProperties) {
        return wrappedWhiteboard.track(type, filterProperties);
    }

    private Optional<ObjectName> extractObjectName(Map<?, ?> objectProperties) {
        return Optional.ofNullable(objectProperties.get("jmx.objectname"))
                       .flatMap(this::asObjectName);
    }

    private Optional<ObjectName> asObjectName(Object rawObjectName) {
        if (rawObjectName instanceof ObjectName) {
            return Optional.of((ObjectName) rawObjectName);
        } else {
            String objectAsString = String.valueOf(rawObjectName);
            return asObjectName(objectAsString);
        }
    }

    private Optional<ObjectName> asObjectName(String rawObjectName) {
        try {
            return Optional.of(new ObjectName(rawObjectName));
        } catch (MalformedObjectNameException exception) {
            String errorMessage = String.format("Failed to convert this object to ObjectName: %s", rawObjectName);
            LOG.error(errorMessage, exception);
            return Optional.empty();
        }
    }

    private <T> void registerMBean(Class<T> expectedServiceType, T service, ObjectName objectName) {
        LOG.trace("Will attempt to register MBean: {}", objectName);
        try {
            String expectedServiceTypeName = expectedServiceType.getName();
            Class<?> actualServiceType = service.getClass();
            String actualServiceTypeName = actualServiceType.getName();
            String serviceMBeanName = actualServiceTypeName + "MBean";
            boolean doesMatchMBeanExpectedType = expectedServiceTypeName.equals(serviceMBeanName);
            boolean isMBeanInstance = service instanceof StandardMBean;
            MBeanServer mBeanServer = ManagementFactory.getPlatformMBeanServer();
            if (doesMatchMBeanExpectedType || isMBeanInstance) {
                mBeanServer.registerMBean(service, objectName);
            } else {
                mBeanServer.registerMBean(new StandardMBean(service, expectedServiceType), objectName);
            }
            LOG.info("Registered MBean: {}", objectName);
        } catch (InstanceAlreadyExistsException | MBeanRegistrationException | NotCompliantMBeanException exception) {
            String errorMessage = String.format(
                    "Failed to register MBean with interface [%s] for this ObjectName: [%s]",
                    expectedServiceType, objectName
            );
            LOG.error(errorMessage, exception);
        }
    }

    private <T> Optional<ObjectName> registerMBean(Class<T> expectedServiceType, T service, Map<?, ?> serviceProps) {
        Optional<ObjectName> objectNameNullable = extractObjectName(serviceProps);
        objectNameNullable.ifPresent(objectName -> registerMBean(expectedServiceType, service, objectName));
        return objectNameNullable;
    }
}
