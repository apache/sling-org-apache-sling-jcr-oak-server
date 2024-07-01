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
package org.apache.sling.jcr.oak.server.it;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.ops4j.pax.exam.junit.PaxExam;

import javax.management.MBeanServer;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;
import java.lang.management.ManagementFactory;
import java.util.Collection;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.Assert.assertEquals;

@RunWith(PaxExam.class)
public class MBeansRegistrationIT extends OakServerTestSupport {

    @Test
    public void testMBeansRegistration() throws MalformedObjectNameException {
        Set<ObjectName> expectedObjectNames = Set.of(
                new ObjectName("org.apache.jackrabbit.oak:name=Oak Query Statistics (Extended),type=QueryStats"),
                new ObjectName("org.apache.jackrabbit.oak:name=Oak Query Statistics,type=QueryStat"),
                new ObjectName("org.apache.jackrabbit.oak:name=Oak Repository Statistics,type=RepositoryStats"),
                new ObjectName("org.apache.jackrabbit.oak:name=async,type=IndexStats"),
                new ObjectName("org.apache.jackrabbit.oak:name=async,type=PropertyIndexAsyncReindex"),
                new ObjectName("org.apache.jackrabbit.oak:name=nodeCounter,type=NodeCounter"),
                new ObjectName("org.apache.jackrabbit.oak:name=repository manager,type=RepositoryManagement"),
                new ObjectName("org.apache.jackrabbit.oak:name=settings,type=QueryEngineSettings")
        );
        MBeanServer mBeanServer = ManagementFactory.getPlatformMBeanServer();
        Set<ObjectName> actualObjectNames = expectedObjectNames.stream()
                .map(expectedObjectName -> mBeanServer.queryNames(expectedObjectName, null))
                .flatMap(Collection::stream)
                .collect(Collectors.toSet());
        assertEquals(expectedObjectNames, actualObjectNames);
    }
}
