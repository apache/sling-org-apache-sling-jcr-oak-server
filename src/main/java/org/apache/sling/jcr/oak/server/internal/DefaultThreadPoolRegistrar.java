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

import org.apache.sling.commons.threads.ThreadPool;
import org.apache.sling.commons.threads.ThreadPoolManager;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;

@Component(
    immediate = true
)
public class DefaultThreadPoolRegistrar {

    @Reference
    private ThreadPoolManager threadPoolManager;

    private ThreadPool threadPool;

    private ServiceRegistration<ThreadPool> serviceRegistration;

    public DefaultThreadPoolRegistrar() {
    }

    @Activate
    private void activate(final BundleContext bundleContext) {
        threadPool = threadPoolManager.get(null);
        serviceRegistration = bundleContext.registerService(ThreadPool.class, threadPool, null);
    }

    @Deactivate
    private void deactivate() {
        if (serviceRegistration != null) {
            serviceRegistration.unregister();
            serviceRegistration = null;
        }
        threadPoolManager.release(threadPool);
        threadPool = null;
    }

}
