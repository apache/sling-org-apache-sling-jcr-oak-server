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

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.apache.jackrabbit.oak.plugins.index.lucene.util.LuceneIndexHelper;
import org.apache.jackrabbit.oak.spi.lifecycle.RepositoryInitializer;
import org.apache.jackrabbit.oak.spi.state.NodeBuilder;
import org.jetbrains.annotations.NotNull;
import org.osgi.framework.Constants;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.ConfigurationPolicy;
import org.osgi.service.metatype.annotations.Designate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.apache.jackrabbit.oak.plugins.index.IndexConstants.INDEX_DEFINITIONS_NAME;

@Component(
    property = {
        Constants.SERVICE_DESCRIPTION + "=Apache Sling JCR Oak Repository – Lucene Index Definition"
    },
    configurationPolicy = ConfigurationPolicy.REQUIRE
)
@Designate(
    ocd = LuceneIndexRepositoryInitializerConfiguration.class
)
public class LuceneIndexRepositoryInitializer implements RepositoryInitializer {

    private LuceneIndexRepositoryInitializerConfiguration configuration;

    private static final Set<String> LUCENE_INDEX_EXCLUDES = new HashSet<>(
        Arrays.asList(
            "jcr:createdBy",
            "jcr:lastModifiedBy",
            "sling:alias",
            "sling:resourceType",
            "sling:vanityPath"
        )
    );

    private final Logger logger = LoggerFactory.getLogger(LuceneIndexRepositoryInitializer.class);

    @Activate
    private void activate(final LuceneIndexRepositoryInitializerConfiguration configuration) {
        logger.debug("activating");
        this.configuration = configuration;
    }

    @Override
    public void initialize(@NotNull NodeBuilder root) {
        logger.debug("initializing");
        if (root.hasChildNode(INDEX_DEFINITIONS_NAME)) {
            final NodeBuilder index = root.child(INDEX_DEFINITIONS_NAME);
            // lucene full-text index
            if (!index.hasChildNode("lucene")) {
                logger.debug("adding new Lucene index definition");
                LuceneIndexHelper.newLuceneIndexDefinition(
                    index,
                    configuration.name(),
                    LuceneIndexHelper.JR_PROPERTY_INCLUDES,
                    LUCENE_INDEX_EXCLUDES,
                    "async"
                );
            }
        }
    }

}
