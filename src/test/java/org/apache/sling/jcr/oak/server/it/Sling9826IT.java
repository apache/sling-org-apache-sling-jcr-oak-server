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

import javax.jcr.ItemNotFoundException;
import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.query.InvalidQueryException;
import javax.jcr.query.Query;
import javax.jcr.query.QueryResult;

import org.apache.jackrabbit.api.JackrabbitSession;
import org.apache.jackrabbit.commons.JcrUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.ops4j.pax.exam.junit.PaxExam;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;

@RunWith(PaxExam.class)
public class Sling9826IT extends OakServerTestSupport {
    private Session adminSession = null;
    private String testFolderPath;
    private String temp1Path;
    private String temp2Path;
    
    @Before
    public void setup() throws RepositoryException {
        adminSession = (JackrabbitSession)slingRepository.loginAdministrative(null);
        
        Node testFolder = JcrUtils.getOrCreateByPath("/content/sling9826", true, "sling:Folder", "sling:Folder", adminSession, false);
        testFolderPath = testFolder.getPath();
        Node temp1Node = JcrUtils.getOrCreateByPath(testFolder, "temp1", true, "sling:Folder", "sling:Folder", false);
        temp1Path = temp1Node.getPath();
        Node temp2Node = JcrUtils.getOrCreateByPath(testFolder, "temp2", true, "sling:Folder", "sling:Folder", false);
        temp2Path = temp2Node.getPath();
        if (adminSession.hasPendingChanges()) {
            adminSession.save();
        }
    }
    
    @After
    public void teardown() throws RepositoryException {
        if (adminSession != null) {
            adminSession.refresh(false);

            if (testFolderPath != null && adminSession.itemExists(testFolderPath)) {
                adminSession.getItem(testFolderPath).remove();
            }
            
            if (adminSession.hasPendingChanges()) {
                adminSession.save();
            }
            
            adminSession.logout();
        }
        adminSession = null;
        temp1Path = null;
        temp2Path = null;
        testFolderPath = null;
    }
    
    /**
     * SLING-9826 - test that jcr:uuid index is updated on move
     */
    @Test
    public void checkUuidIndexUpdatedOnMove() throws Exception {
        // create the node to move
        Node parent = adminSession.getNode(temp1Path);
        String childName = "child" + System.currentTimeMillis();
        Node child = parent.addNode(childName, "sling:Folder");
        child.addMixin("mix:referenceable");
        adminSession.save();
        
        // verify the id and lookup by id and query works 
        String id = child.getIdentifier();
        assertThat(adminSession.getNodeByIdentifier(id), notNullValue());
        verifyLookupByIdentifier(id);

        // move it
        adminSession.move(child.getPath(), temp2Path + childName);
        adminSession.save();
        
        // verify the id and lookup by id and query works 
        verifyLookupByIdentifier(id);
    }

    protected void verifyLookupByIdentifier(String id)
            throws ItemNotFoundException, RepositoryException, InvalidQueryException {
        // verify lookup by id
        Node nodeByIdentifier = adminSession.getNodeByIdentifier(id);
        assertThat(nodeByIdentifier, notNullValue());
        assertThat(nodeByIdentifier.getIdentifier(), is(id));

        // verify lookup by query
        Query query = adminSession.getWorkspace().getQueryManager().createQuery(String.format("SELECT * FROM [nt:base] WHERE [jcr:uuid] = '%s'", id), Query.JCR_SQL2);
        QueryResult execute = query.execute();
        NodeIterator nodes = execute.getNodes();
        assertThat(nodes.hasNext(), is(true));
        Node nextNode = nodes.nextNode();
        assertThat(nextNode, notNullValue());
        assertThat(nextNode.getIdentifier(), is(id));
    }
    
}
