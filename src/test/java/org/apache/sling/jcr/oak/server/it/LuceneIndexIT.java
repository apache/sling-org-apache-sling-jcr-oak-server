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

import javax.inject.Inject;

import org.apache.jackrabbit.oak.plugins.index.IndexPathService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.ops4j.pax.exam.Configuration;
import org.ops4j.pax.exam.Option;
import org.ops4j.pax.exam.junit.PaxExam;
import org.ops4j.pax.exam.spi.reactors.ExamReactorStrategy;
import org.ops4j.pax.exam.spi.reactors.PerClass;

import static org.hamcrest.CoreMatchers.hasItem;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.ops4j.pax.exam.CoreOptions.mavenBundle;
import static org.ops4j.pax.exam.OptionUtils.combine;
import static org.ops4j.pax.exam.cm.ConfigurationAdminOptions.newConfiguration;

@RunWith(PaxExam.class)
@ExamReactorStrategy(PerClass.class)
public class LuceneIndexIT extends OakServerTestSupport {

    @Inject
    private IndexPathService indexPathService;

    @Override
    @Configuration
    public Option[] configuration() {
        // we should only add oak-lucene but oak-lucene has a hard dependency on oak-store-document (OAK-7263)
        return combine(
            super.configuration(),
            mavenBundle().groupId("org.apache.jackrabbit").artifactId("oak-lucene").version(OAK_VERSION),
            mavenBundle().groupId("org.apache.jackrabbit").artifactId("oak-store-document").version(OAK_VERSION),
            newConfiguration("org.apache.sling.jcr.oak.server.internal.index.LuceneIndexRepositoryInitializer")
                .put("name", "lucy")
                .asOption()
        );
    }

    @Test
    public void ensureLuceneIndexIsCreated() {
        assertThat(indexPathService, notNullValue());
        assertThat(indexPathService.getIndexPaths(), hasItem("/oak:index/lucy"));
    }

}
