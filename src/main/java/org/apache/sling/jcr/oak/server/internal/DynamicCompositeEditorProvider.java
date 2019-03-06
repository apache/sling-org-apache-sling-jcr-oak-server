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

import java.util.stream.Collectors;

import org.apache.jackrabbit.oak.api.CommitFailedException;
import org.apache.jackrabbit.oak.spi.commit.CommitInfo;
import org.apache.jackrabbit.oak.spi.commit.CompositeEditorProvider;
import org.apache.jackrabbit.oak.spi.commit.Editor;
import org.apache.jackrabbit.oak.spi.commit.EditorProvider;
import org.apache.jackrabbit.oak.spi.state.NodeBuilder;
import org.apache.jackrabbit.oak.spi.state.NodeState;
import org.apache.jackrabbit.oak.spi.whiteboard.Tracker;
import org.apache.jackrabbit.oak.spi.whiteboard.Whiteboard;

/**
 * An aggregator {@link EditorProvider} which combines all the {@link EditorProvider} components registered in the
 * {@link Whiteboard}. This is used automatically by {@link OakSlingRepositoryManager} whenever
 * {@link OakSlingRepositoryManagerConfiguration#dynamic_components()} is {@code true}.
 *
 * @version $Id$
 */
final class DynamicCompositeEditorProvider implements EditorProvider
{
    /**
     * Provides access to the live list of registered {@link EditorProvider} components.
     */
    private final Tracker<EditorProvider> editorProviders;

    /**
     * Constructor, needs the {@link Whiteboard} that provides access to registered components.
     *
     * @param whiteboard the {@link Whiteboard} in use, must not be {@code null}
     */
    public DynamicCompositeEditorProvider(final Whiteboard whiteboard)
    {
        this.editorProviders = whiteboard.track(EditorProvider.class);
    }

    @Override
    public Editor getRootEditor(NodeState before, NodeState after, NodeBuilder builder, CommitInfo info)
        throws CommitFailedException
    {
        return CompositeEditorProvider.compose(
            // All registered EditorProvider components
            this.editorProviders.getServices().stream()
                // Excluding composites, such as this one
                .filter(i -> !(i instanceof CompositeEditorProvider)).collect(Collectors.toList()))
            // Forward the call
            .getRootEditor(before, after, builder, info);
    }
}
