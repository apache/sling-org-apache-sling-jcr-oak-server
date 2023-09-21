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

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import javax.inject.Inject;
import javax.jcr.Item;
import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.observation.EventIterator;
import javax.jcr.observation.EventListener;
import javax.jcr.observation.ObservationManager;

import org.apache.jackrabbit.commons.JcrUtils;
import org.apache.sling.api.resource.ResourceResolverFactory;
import org.apache.sling.jcr.api.SlingRepository;
import org.apache.sling.testing.paxexam.TestSupport;
import org.ops4j.pax.exam.Configuration;
import org.ops4j.pax.exam.Option;
import org.ops4j.pax.exam.util.PathUtils;
import org.osgi.framework.BundleContext;

import static org.apache.sling.testing.paxexam.SlingOptions.scr;
import static org.apache.sling.testing.paxexam.SlingOptions.slingJcr;
import static org.apache.sling.testing.paxexam.SlingOptions.slingJcrRepoinit;
import static org.apache.sling.testing.paxexam.SlingOptions.versionResolver;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.ops4j.pax.exam.CoreOptions.composite;
import static org.ops4j.pax.exam.CoreOptions.mavenBundle;
import static org.ops4j.pax.exam.cm.ConfigurationAdminOptions.factoryConfiguration;
import static org.ops4j.pax.exam.cm.ConfigurationAdminOptions.newConfiguration;

public abstract class OakServerTestSupport extends TestSupport {

    @Inject
    protected SlingRepository slingRepository;

    @Inject
    protected BundleContext bundleContext;

    @Inject
    protected SlingRepository repository;

    @Inject
    protected ResourceResolverFactory resourceResolverFactory;


    protected final List<String> toDelete = new LinkedList<String>();

    private final AtomicInteger uniqueNameCounter = new AtomicInteger();

    protected static final Integer TEST_SCALE = Integer.getInteger("test.scale", 1);

    protected class JcrEventsCounter implements EventListener {
        private final Session s;
        private int jcrEventsCounter;

        public JcrEventsCounter() throws RepositoryException {
            s = repository.loginAdministrative(null);
            final ObservationManager om = s.getWorkspace().getObservationManager();
            final int eventTypes = 255; // not sure if that's a recommended value, but common
            final boolean deep = true;
            final String[] uuid = null;
            final String[] nodeTypeNames = new String[]{"mix:language", "sling:Message"};
            final boolean noLocal = true;
            final String root = "/";
            om.addEventListener(this, eventTypes, root, deep, uuid, nodeTypeNames, noLocal);
        }

        void close() {
            s.logout();
        }

        @Override
        public void onEvent(EventIterator it) {
            while (it.hasNext()) {
                it.nextEvent();
                jcrEventsCounter++;
            }
        }

        int get() {
            return jcrEventsCounter;
        }
    }

    protected <ItemType extends Item> ItemType deleteAfterTests(ItemType it) throws RepositoryException {
        toDelete.add(it.getPath());
        return it;
    }

    /**
     * Verify that admin can create and retrieve a node of the specified type.
     *
     * @return the path of the test node that was created.
     */
    protected String assertCreateRetrieveNode(String nodeType) throws RepositoryException {
        return assertCreateRetrieveNode(nodeType, null);
    }

    protected String assertCreateRetrieveNode(String nodeType, String relParentPath) throws RepositoryException {
        Session session = repository.loginAdministrative(null);
        try {
            final Node root = session.getRootNode();
            final String name = uniqueName("assertCreateRetrieveNode");
            final String propName = "PN_" + name;
            final String propValue = "PV_" + name;
            final Node parent = relParentPath == null ? root : JcrUtils.getOrAddNode(root, relParentPath);
            final Node child = nodeType == null ? parent.addNode(name) : parent.addNode(name, nodeType);
            child.setProperty(propName, propValue);
            child.setProperty("foo", child.getPath());
            session.save();
            session.logout();
            session = repository.loginAdministrative(null);
            final String path = relParentPath == null ? "/" + name : "/" + relParentPath + "/" + name;
            final Node n = session.getNode(path);
            assertThat(n, notNullValue());
            assertThat(propValue, is(n.getProperty(propName).getString()));
            return n.getPath();
        } finally {
            session.logout();
        }
    }

    protected String uniqueName(String hint) {
        return hint + "_" + uniqueNameCounter.incrementAndGet() + "_" + System.currentTimeMillis();
    }

    @Configuration
    public Option[] configuration() {
        // SLING-12035 - bump the oak artifacts to the 1.56.0 version
        //   remove this block after the versionResolver has these versions or later
        versionResolver.setVersion("org.apache.jackrabbit", "oak-api", "1.56.0");
        versionResolver.setVersion("org.apache.jackrabbit", "oak-authorization-principalbased", "1.56.0");
        versionResolver.setVersion("org.apache.jackrabbit", "oak-blob", "1.56.0");
        versionResolver.setVersion("org.apache.jackrabbit", "oak-blob-plugins", "1.56.0");
        versionResolver.setVersion("org.apache.jackrabbit", "oak-commons", "1.56.0");
        versionResolver.setVersion("org.apache.jackrabbit", "oak-core", "1.56.0");
        versionResolver.setVersion("org.apache.jackrabbit", "oak-core-spi", "1.56.0");
        versionResolver.setVersion("org.apache.jackrabbit", "oak-jackrabbit-api", "1.56.0");
        versionResolver.setVersion("org.apache.jackrabbit", "oak-jcr", "1.56.0");
        versionResolver.setVersion("org.apache.jackrabbit", "oak-lucene", "1.56.0");
        versionResolver.setVersion("org.apache.jackrabbit", "oak-query-spi", "1.56.0");
        versionResolver.setVersion("org.apache.jackrabbit", "oak-security-spi", "1.56.0");
        versionResolver.setVersion("org.apache.jackrabbit", "oak-segment-tar", "1.56.0");
        versionResolver.setVersion("org.apache.jackrabbit", "oak-store-composite", "1.56.0");
        versionResolver.setVersion("org.apache.jackrabbit", "oak-store-document", "1.56.0");
        versionResolver.setVersion("org.apache.jackrabbit", "oak-store-spi", "1.56.0");
        versionResolver.setVersion("org.apache.jackrabbit", "oak-shaded-guava", "1.56.0");
        // SLING-12035 - bump the related artifacts to the compatible versions
        //   remove this block after the versionResolver has these versions or later
        versionResolver.setVersion("commons-codec", "commons-codec", "1.16.0");
        versionResolver.setVersion("commons-io", "commons-io", "2.13.0");

        return new Option[]{
            baseConfiguration(),
            quickstart(),
            // Sling JCR Oak Server
            testBundle("bundle.filename"),
        };
    }

    protected Option quickstart() {
        final String repoinit = String.format("raw:file:%s/src/test/resources/repoinit.txt", PathUtils.getBaseDir());
        final String slingHome = String.format("%s/sling", workingDirectory());
        final String repositoryHome = String.format("%s/repository", slingHome);
        final String localIndexDir = String.format("%s/index", repositoryHome);
        return composite(
            scr(),
            slingJcr(),
            slingJcrRepoinit(),
            mavenBundle().groupId("org.apache.jackrabbit").artifactId("oak-segment-tar").version(versionResolver),
            newConfiguration("org.apache.jackrabbit.oak.segment.SegmentNodeStoreService")
                .put("repository.home", repositoryHome)
                .put("name", "Default NodeStore")
                .asOption(),
            newConfiguration("org.apache.jackrabbit.oak.plugins.index.lucene.LuceneIndexProviderService")
                .put("localIndexDir", localIndexDir)
                .asOption(),
            newConfiguration("org.apache.sling.jcr.repoinit.impl.RepositoryInitializer")
                .put("references", new String[]{repoinit})
                .asOption(),
            getWhitelistRegexpOption(),
            // To generate the list of whitelisted bundles after a failed test-run:
            // grep -R 'NOT white' target/failsafe-reports/ | awk -F': Bundle ' '{print substr($2, 1, index($2, " is NOT "))}' | sort -u
            factoryConfiguration("org.apache.sling.jcr.base.internal.LoginAdminWhitelist.fragment")
                .put("whitelist.bundles", new String[]{
                    "org.apache.sling.jcr.oak.server",
                    "org.apache.sling.jcr.contentloader",
                    "org.apache.sling.jcr.resource",
                    "org.apache.sling.resourceresolver"
                })
                .asOption()
        ).add(
            // SLING-12035 - add extra bundle for the shaded version of guava used by the latest oak releases
            //   remove this block after the SlingOptions#jackrabbitOak includes this artifact
            mavenBundle().groupId("org.apache.jackrabbit").artifactId("oak-shaded-guava").version(versionResolver)
        );
    }

    protected Option getWhitelistRegexpOption() {
        return newConfiguration("org.apache.sling.jcr.base.internal.LoginAdminWhitelist")
            .put("whitelist.bundles.regexp", "PAXEXAM-PROBE-.*")
            .asOption();
    }
}
