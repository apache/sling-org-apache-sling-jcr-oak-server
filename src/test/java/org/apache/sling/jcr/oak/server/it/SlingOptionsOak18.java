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

import static org.ops4j.pax.exam.CoreOptions.composite;
import static org.ops4j.pax.exam.CoreOptions.mavenBundle;
import static org.ops4j.pax.exam.cm.ConfigurationAdminOptions.factoryConfiguration;

import org.apache.sling.testing.paxexam.SlingOptions;
import org.ops4j.pax.exam.Option;

public class SlingOptionsOak18  extends SlingOptions {
    private static final String OAK_VERSION = "1.8.0";
    private static final String JACKRABBIT_VERSION = "2.16.0";
    
    public static void overrideVersions() {
        versionResolver.setVersion("org.apache.jackrabbit", "jackrabbit-api", JACKRABBIT_VERSION);
        versionResolver.setVersion("org.apache.jackrabbit", "jackrabbit-jcr-commons", JACKRABBIT_VERSION);
        versionResolver.setVersion("org.apache.jackrabbit", "jackrabbit-jcr-rmi", JACKRABBIT_VERSION);
        versionResolver.setVersion("org.apache.jackrabbit", "jackrabbit-data", JACKRABBIT_VERSION);
        versionResolver.setVersion("org.apache.jackrabbit", "jackrabbit-spi", JACKRABBIT_VERSION);
        versionResolver.setVersion("org.apache.jackrabbit", "jackrabbit-spi-commons", JACKRABBIT_VERSION);
        versionResolver.setVersion("org.apache.jackrabbit", "jackrabbit-webdav", JACKRABBIT_VERSION);

        versionResolver.setVersion("io.dropwizard.metrics", "metrics-core", "3.1.0");
        versionResolver.setVersion("org.apache.jackrabbit", "oak-commons", OAK_VERSION);
        versionResolver.setVersion("org.apache.jackrabbit", "oak-core-spi", OAK_VERSION);
        versionResolver.setVersion("org.apache.jackrabbit", "oak-store-spi", OAK_VERSION);
        versionResolver.setVersion("org.apache.jackrabbit", "oak-query-spi", OAK_VERSION);
        versionResolver.setVersion("org.apache.jackrabbit", "oak-security-spi", OAK_VERSION);
        versionResolver.setVersion("org.apache.jackrabbit", "oak-api", OAK_VERSION);
        versionResolver.setVersion("org.apache.jackrabbit", "oak-core", OAK_VERSION);
        versionResolver.setVersion("org.apache.jackrabbit", "oak-segment-tar", OAK_VERSION);
        versionResolver.setVersion("org.apache.jackrabbit", "oak-blob", OAK_VERSION);
        versionResolver.setVersion("org.apache.jackrabbit", "oak-blob-plugins", OAK_VERSION);
        versionResolver.setVersion("org.apache.jackrabbit", "oak-jcr", OAK_VERSION);
        versionResolver.setVersion("org.apache.jackrabbit", "oak-store-composite", OAK_VERSION);
        
        // This version makes the changed imports optional so it will work in the test
        versionResolver.setVersion("org.apache.sling", "org.apache.sling.jcr.registration", "1.0.4");
    }

    public static Option slingJcr() {
        return composite(
            webconsole(),
            sling(),
            jackrabbitSling(),
            jackrabbitOakSling(),
            mavenBundle().groupId("org.apache.sling").artifactId("org.apache.sling.jcr.api").version(versionResolver),
            mavenBundle().groupId("org.apache.sling").artifactId("org.apache.sling.jcr.base").version(versionResolver),
            mavenBundle().groupId("org.apache.sling").artifactId("org.apache.sling.jcr.classloader").version(versionResolver),
            mavenBundle().groupId("org.apache.sling").artifactId("org.apache.sling.jcr.contentloader").version(versionResolver),
            mavenBundle().groupId("org.apache.sling").artifactId("org.apache.sling.jcr.davex").version(versionResolver),
            
            mavenBundle().groupId("org.apache.sling").artifactId("org.apache.sling.jcr.registration").version(versionResolver),
            mavenBundle().groupId("org.apache.sling").artifactId("org.apache.sling.jcr.resource").version(versionResolver),
            mavenBundle().groupId("org.apache.sling").artifactId("org.apache.sling.jcr.webconsole").version(versionResolver),
            mavenBundle().groupId("org.apache.sling").artifactId("org.apache.sling.jcr.webdav").version(versionResolver),
            
            factoryConfiguration("org.apache.sling.jcr.base.internal.LoginAdminWhitelist.fragment")
                .put("whitelist.bundles", new String[]{"org.apache.sling.discovery.commons", "org.apache.sling.discovery.base", "org.apache.sling.discovery.oak", "org.apache.sling.extensions.webconsolesecurityprovider", "org.apache.sling.jcr.base", "org.apache.sling.jcr.classloader", "org.apache.sling.jcr.contentloader", "org.apache.sling.jcr.davex", "org.apache.sling.jcr.jackrabbit.usermanager", "org.apache.sling.jcr.oak.server", "org.apache.sling.jcr.repoinit", "org.apache.sling.jcr.resource", "org.apache.sling.jcr.webconsole"})
                .put("whitelist.name", "sling")
                .asOption(),
            factoryConfiguration("org.apache.sling.serviceusermapping.impl.ServiceUserMapperImpl.amended")
                .put("user.mapping", new String[]{"org.apache.sling.jcr.resource:observation=sling-readall"})
                .asOption()
        );
    }
    
    public static Option jackrabbitSling() {
        return composite(
            mavenBundle().groupId("org.apache.jackrabbit").artifactId("jackrabbit-api").version(versionResolver),
            mavenBundle().groupId("org.apache.jackrabbit").artifactId("jackrabbit-data").version(versionResolver),
            mavenBundle().groupId("org.apache.jackrabbit").artifactId("jackrabbit-jcr-commons").version(versionResolver),
            mavenBundle().groupId("org.apache.jackrabbit").artifactId("jackrabbit-jcr-rmi").version(versionResolver),
            mavenBundle().groupId("org.apache.jackrabbit").artifactId("jackrabbit-spi").version(versionResolver),
            mavenBundle().groupId("org.apache.jackrabbit").artifactId("jackrabbit-spi-commons").version(versionResolver),
            mavenBundle().groupId("org.apache.jackrabbit").artifactId("jackrabbit-webdav").version(versionResolver),
            mavenBundle().groupId("javax.jcr").artifactId("jcr").version(versionResolver),
            mavenBundle().groupId("javax.servlet").artifactId("javax.servlet-api").version(versionResolver),
            mavenBundle().groupId("commons-codec").artifactId("commons-codec").version(versionResolver),
            mavenBundle().groupId("commons-collections").artifactId("commons-collections").version(versionResolver),
            mavenBundle().groupId("commons-fileupload").artifactId("commons-fileupload").version(versionResolver),
            mavenBundle().groupId("commons-io").artifactId("commons-io").version(versionResolver),
            mavenBundle().groupId("commons-lang").artifactId("commons-lang").version(versionResolver),
            mavenBundle().groupId("com.google.guava").artifactId("guava").version(versionResolver),
            mavenBundle().groupId("org.apache.geronimo.bundles").artifactId("commons-httpclient").version(versionResolver),
            mavenBundle().groupId("org.apache.geronimo.specs").artifactId("geronimo-atinject_1.0_spec").version(versionResolver),
            mavenBundle().groupId("org.apache.geronimo.specs").artifactId("geronimo-el_2.2_spec").version(versionResolver),
            mavenBundle().groupId("org.apache.geronimo.specs").artifactId("geronimo-interceptor_1.1_spec").version(versionResolver),
            mavenBundle().groupId("org.apache.geronimo.specs").artifactId("geronimo-jcdi_1.0_spec").version(versionResolver),
            mavenBundle().groupId("org.apache.geronimo.specs").artifactId("geronimo-jta_1.1_spec").version(versionResolver),
            mavenBundle().groupId("org.apache.httpcomponents").artifactId("httpcore-osgi").version(versionResolver),
            mavenBundle().groupId("org.apache.httpcomponents").artifactId("httpclient-osgi").version(versionResolver)
        );
    }
    
    public static Option jackrabbitOakSling() {
        return composite(
            scr(),
            jackrabbitSling(),
            mavenBundle().groupId("io.dropwizard.metrics").artifactId("metrics-core").version(versionResolver),
            mavenBundle().groupId("org.apache.jackrabbit").artifactId("oak-commons").version(versionResolver),
            mavenBundle().groupId("org.apache.jackrabbit").artifactId("oak-core-spi").version(versionResolver),
            mavenBundle().groupId("org.apache.jackrabbit").artifactId("oak-store-spi").version(versionResolver),
            mavenBundle().groupId("org.apache.jackrabbit").artifactId("oak-query-spi").version(versionResolver),
            mavenBundle().groupId("org.apache.jackrabbit").artifactId("oak-security-spi").version(versionResolver),
            mavenBundle().groupId("org.apache.jackrabbit").artifactId("oak-api").version(versionResolver),
            mavenBundle().groupId("org.apache.jackrabbit").artifactId("oak-core").version(versionResolver),
            mavenBundle().groupId("org.apache.jackrabbit").artifactId("oak-segment-tar").version(versionResolver),
            mavenBundle().groupId("org.apache.jackrabbit").artifactId("oak-blob").version(versionResolver),
            mavenBundle().groupId("org.apache.jackrabbit").artifactId("oak-blob-plugins").version(versionResolver),
            mavenBundle().groupId("org.apache.jackrabbit").artifactId("oak-jcr").version(versionResolver),
            mavenBundle().groupId("org.apache.jackrabbit").artifactId("oak-store-composite").version(versionResolver),
            mavenBundle().groupId("com.google.guava").artifactId("guava").version(versionResolver),
            mavenBundle().groupId("org.apache.felix").artifactId("org.apache.felix.jaas").version(versionResolver)
        );
    }
    
    // Below here the options are just copied over to avoid that the old bundles are loaded from SlingOptions 
    
    public static Option slingJcrRepoinit() {
        return composite(
            slingJcrJackrabbitSecurity(),
            paxUrlClasspath(),
            mavenBundle().groupId("org.apache.sling").artifactId("org.apache.sling.jcr.repoinit").version(versionResolver),
            mavenBundle().groupId("org.apache.sling").artifactId("org.apache.sling.repoinit.parser").version(versionResolver),
            mavenBundle().groupId("org.apache.sling").artifactId("org.apache.sling.provisioning.model").version(versionResolver)
        );
    }
    
    public static Option slingJcrJackrabbitSecurity() {
        return composite(
            slingServlets(),
            mavenBundle().groupId("org.apache.sling").artifactId("org.apache.sling.jcr.jackrabbit.accessmanager").version(versionResolver),
            mavenBundle().groupId("org.apache.sling").artifactId("org.apache.sling.jcr.jackrabbit.usermanager").version(versionResolver)
        );
    }
    
    public static Option slingServlets() {
        return composite(
            mavenBundle().groupId("org.apache.sling").artifactId("org.apache.sling.servlets.get").version(versionResolver),
            mavenBundle().groupId("org.apache.sling").artifactId("org.apache.sling.servlets.post").version(versionResolver),
            mavenBundle().groupId("org.apache.sling").artifactId("org.apache.sling.servlets.resolver").version(versionResolver),
            factoryConfiguration("org.apache.sling.serviceusermapping.impl.ServiceUserMapperImpl.amended")
                .put("user.mapping", new String[]{"org.apache.sling.servlets.resolver:scripts=sling-scripting"})
                .asOption()
        );
    }
}
