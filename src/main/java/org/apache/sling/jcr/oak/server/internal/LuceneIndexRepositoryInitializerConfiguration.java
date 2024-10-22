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

import org.osgi.service.metatype.annotations.AttributeDefinition;
import org.osgi.service.metatype.annotations.ObjectClassDefinition;

@ObjectClassDefinition(
    name = "Apache Sling JCR Oak Repository – Lucene Index Definition",
    description = "Configuration to set up a Lucene Index Definition"
)
@interface LuceneIndexRepositoryInitializerConfiguration {

    @AttributeDefinition(
        name = "Index Name",
        description = "The name of the index."
    )
    String name() default "lucene";

    @AttributeDefinition(
        name = "Include Property Types",
        description = "Property types which should be indexed."
    )
    String[] includePropertyTypes() default {
        "String",
        "Binary"
    };

    @AttributeDefinition(
        name = "Exclude Property Names",
        description = "Properties which should not be indexed."
    )
    String[] excludePropertyNames() default {
        "jcr:createdBy",
        "jcr:lastModifiedBy",
        "sling:alias",
        "sling:resourceType",
        "sling:vanityPath"
    };

}
