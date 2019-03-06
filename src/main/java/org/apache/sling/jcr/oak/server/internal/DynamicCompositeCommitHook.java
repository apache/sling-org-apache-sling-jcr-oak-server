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
import org.apache.jackrabbit.oak.spi.commit.CommitHook;
import org.apache.jackrabbit.oak.spi.commit.CommitInfo;
import org.apache.jackrabbit.oak.spi.commit.CompositeHook;
import org.apache.jackrabbit.oak.spi.state.NodeState;
import org.apache.jackrabbit.oak.spi.whiteboard.Tracker;
import org.apache.jackrabbit.oak.spi.whiteboard.Whiteboard;

/**
 * An aggregator {@link CommitHook} which combines all the {@link CommitHook} components registered in the
 * {@link Whiteboard}. This is used automatically by {@link OakSlingRepositoryManager} whenever
 * {@link OakSlingRepositoryManagerConfiguration#dynamic_components()} is {@code true}.
 *
 * @version $Id$
 */
final class DynamicCompositeCommitHook implements CommitHook
{
    /**
     * Provides access to the live list of registered {@link CommitHook} components.
     */
    private final Tracker<CommitHook> hooks;

    /**
     * Constructor, needs the {@link Whiteboard} that provides access to registered components.
     *
     * @param whiteboard the {@link Whiteboard} in use, must not be {@code null}
     */
    public DynamicCompositeCommitHook(final Whiteboard whiteboard)
    {
        this.hooks = whiteboard.track(CommitHook.class);
    }

    @Override
    public NodeState processCommit(NodeState before, NodeState after, CommitInfo info) throws CommitFailedException
    {
        return CompositeHook.compose(
            // All registered CommitHook components
            this.hooks.getServices().stream()
                // Excluding composite hooks, such as this one
                .filter(i -> !(i instanceof CompositeHook)).collect(Collectors.toList()))
            // Forward the call
            .processCommit(before, after, info);
    }
}
