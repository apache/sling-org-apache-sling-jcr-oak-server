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

import org.apache.jackrabbit.oak.spi.whiteboard.DefaultWhiteboard;
import org.apache.jackrabbit.oak.spi.whiteboard.Registration;
import org.apache.jackrabbit.oak.spi.whiteboard.Whiteboard;
import org.junit.Before;
import org.junit.Test;

import javax.management.MBeanServer;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;
import java.lang.management.ManagementFactory;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.Assert.*;

public class WithMBeanRegistrationTest {

    private static final String RAW_OBJECT_NAME = "com.example:type=TestBean";
    private WithMBeanRegistration withMBeanRegistration;
    private MBeanServer mBeanServer;

    @SuppressWarnings({"PackageVisibleInnerClass", "MarkerInterface"})
    interface NonPublicTestMBean {
    }

    @SuppressWarnings({"PublicInnerClass", "MarkerInterface", "WeakerAccess"})
    public interface TestMBean {
    }

    @SuppressWarnings({"WeakerAccess", "PackageVisibleInnerClass"})
    static class TestMBeanImpl implements NonPublicTestMBean, TestMBean {
    }

    @Before
    public void setUp() {
        withMBeanRegistration = new WithMBeanRegistration(new DefaultWhiteboard());
        mBeanServer = ManagementFactory.getPlatformMBeanServer();
    }

    private int numOfTestMBeans() throws MalformedObjectNameException {
        Set<ObjectName> allONs = mBeanServer.queryNames(new ObjectName(RAW_OBJECT_NAME), null);
        return allONs.size();
    }

    @Test
    public void mustRegisterAndUnregisterMBean() throws MalformedObjectNameException {
        Map<String, String> props = new HashMap<>();
        props.put("jmx.objectname", RAW_OBJECT_NAME);
        TestMBean service = new TestMBeanImpl();
        Class<TestMBean> serviceType = TestMBean.class;
        withMBeanRegistration.register(serviceType, service, props);
        assertEquals(1, numOfTestMBeans());
    }

    @Test
    public void mustRegisterAndUnregisterWithNativeONinProps() throws MalformedObjectNameException {
        Map<String, ObjectName> props = new HashMap<>();
        props.put("jmx.objectname", new ObjectName(RAW_OBJECT_NAME));
        TestMBean service = new TestMBeanImpl();
        Class<TestMBean> serviceType = TestMBean.class;
        withMBeanRegistration.register(serviceType, service, props);
        assertEquals(1, numOfTestMBeans());
    }

    @Test
    public void mustUnregisterMBean() throws MalformedObjectNameException {
        Map<String, String> props = new HashMap<>();
        props.put("jmx.objectname", RAW_OBJECT_NAME);
        TestMBean service = new TestMBeanImpl();
        Class<TestMBean> serviceType = TestMBean.class;
        Registration registration = withMBeanRegistration.register(serviceType, service, props);
        assertEquals(1, numOfTestMBeans());
        registration.unregister();
        assertEquals(0, numOfTestMBeans());
    }

    @Test
    public void mustFailRegistrationForNonPublicMBeanInterface() throws MalformedObjectNameException {
        Map<String, String> props = new HashMap<>();
        props.put("jmx.objectname", RAW_OBJECT_NAME);
        NonPublicTestMBean service = new TestMBeanImpl();
        Class<NonPublicTestMBean> serviceType = NonPublicTestMBean.class;
        withMBeanRegistration.register(serviceType, service, props);
        assertEquals(0, numOfTestMBeans());
    }

    @Test
    public void mustNotRegisterMalformedObjectName() throws MalformedObjectNameException {
        Set<ObjectName> initialONs = mBeanServer.queryNames(new ObjectName("*:*"), null);
        int initialNumOfONs = initialONs.size();
        String rawInvalidObjectName = "===INVALID OBJECT NAME===";
        Map<String, String> props = new HashMap<>();
        props.put("jmx.objectname", rawInvalidObjectName);
        TestMBean service = new TestMBeanImpl();
        Class<TestMBean> serviceType = TestMBean.class;
        withMBeanRegistration.register(serviceType, service, props);
        Set<ObjectName> finalONs = mBeanServer.queryNames(new ObjectName("*:*"), null);
        int finalNumOfONs = finalONs.size();
        assertEquals(initialNumOfONs, finalNumOfONs);
    }

    @Test
    public void mustNotConflictWithWrappedWhiteboard() throws MalformedObjectNameException {
        AtomicBoolean wasRegisteredByWrapper = new AtomicBoolean(false);
        AtomicBoolean wasUnregisteredByWrapper = new AtomicBoolean(false);
        Whiteboard wrappedWhiteboard = new DefaultWhiteboard() {
            @Override
            public <T> Registration register(Class<T> type, T service, Map<?, ?> properties) {
                wasRegisteredByWrapper.set(true);
                return () -> wasUnregisteredByWrapper.set(true);
            }
        };
        Whiteboard wrapperWhiteboard = new WithMBeanRegistration(wrappedWhiteboard);
        Set<ObjectName> initialONs = mBeanServer.queryNames(new ObjectName("*:*"), null);
        int initialNumOfONs = initialONs.size();
        String rawInvalidObjectName = "===INVALID OBJECT NAME===";
        Map<String, String> props = new HashMap<>();
        props.put("jmx.objectname", rawInvalidObjectName);
        TestMBean service = new TestMBeanImpl();
        Class<TestMBean> serviceType = TestMBean.class;
        assertFalse(wasRegisteredByWrapper.get());
        assertFalse(wasUnregisteredByWrapper.get());
        Registration registration = wrapperWhiteboard.register(serviceType, service, props);
        Set<ObjectName> finalONs = mBeanServer.queryNames(new ObjectName("*:*"), null);
        int finalNumOfONs = finalONs.size();
        assertEquals(initialNumOfONs, finalNumOfONs);
        assertTrue(wasRegisteredByWrapper.get());
        assertFalse(wasUnregisteredByWrapper.get());
        registration.unregister();
        assertTrue(wasUnregisteredByWrapper.get());
    }
}
