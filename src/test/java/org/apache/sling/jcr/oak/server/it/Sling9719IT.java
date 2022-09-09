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

import javax.jcr.Node;

import org.apache.jackrabbit.api.JackrabbitSession;
import org.apache.jackrabbit.commons.JcrUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.ops4j.pax.exam.junit.PaxExam;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

@RunWith(PaxExam.class)
public class Sling9719IT extends OakServerTestSupport {

	/**
	 * SLING-9719 - test that the dynamic Atomic Counter EditorProvider was discovered and is 
	 * active and functioning
	 */
    @Test
    public void checkAtomicCounter() throws Exception {
        JackrabbitSession adminSession = (JackrabbitSession)slingRepository.loginAdministrative(null);
                
        Node counter = JcrUtils.getOrCreateByPath("/content/sling9719/counter", "nt:unstructured", adminSession);
        if (!counter.isNodeType("mix:atomicCounter")) {
        	counter.addMixin("mix:atomicCounter");
        }
        if (!counter.isNew()) {
        	// reset the property back to the default value
        	counter.setProperty("oak:counter", 0L);
        }

        adminSession.save();
        
        // counter initial value is 0. the default value
        assertThat(counter.getProperty("oak:counter").getLong(), is(0L));

        // incrementing by 5 the counter
        counter.setProperty("oak:increment", 5);
        adminSession.save();

        // counter value is now 5
        new Retry(5000) {
            @Override
            protected void exec() throws Exception {
                assertThat(counter.getProperty("oak:counter").getLong(), is(5L));
            }
        };

        // decreasing by 1
        counter.setProperty("oak:increment", -1);
        adminSession.save();

        // counter value is now 4
        new Retry(5000) {
            @Override
            protected void exec() throws Exception {
                assertThat(counter.getProperty("oak:counter").getLong(), is(4L));
            }
        };

        adminSession.logout();
    }
}
