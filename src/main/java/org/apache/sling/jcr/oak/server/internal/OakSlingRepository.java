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

import java.security.PrivilegedActionException;
import java.security.PrivilegedExceptionAction;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.security.auth.Subject;

import org.apache.jackrabbit.api.JackrabbitRepository;
import org.apache.jackrabbit.oak.api.AuthInfo;
import org.apache.jackrabbit.oak.spi.security.authentication.AuthInfoImpl;
import org.apache.jackrabbit.oak.spi.security.principal.AdminPrincipal;
import org.apache.jackrabbit.oak.spi.security.principal.SystemUserPrincipal;
import org.apache.sling.jcr.base.AbstractSlingRepository2;
import org.apache.sling.jcr.base.AbstractSlingRepositoryManager;
import org.osgi.framework.Bundle;

import static java.util.Collections.emptyMap;
import static java.util.Collections.emptySet;
import static java.util.Collections.singleton;

/**
 * A Sling repository implementation that wraps the Oak OSGi repository
 * implementation from the Oak project.
 */
public class OakSlingRepository extends AbstractSlingRepository2 {

    private static String first(Iterable<String> servicePrincipalNames) {
        Iterator<String> iterator = servicePrincipalNames.iterator();
        return iterator.hasNext() ? iterator.next() : null;
    }
    
    private final String adminId;

    protected OakSlingRepository(final AbstractSlingRepositoryManager manager, final Bundle usingBundle, final String adminId) {
        super(manager, usingBundle);
        this.adminId = adminId;
    }

    @Override
    protected Session createAdministrativeSession(String workspace) throws RepositoryException {
        // TODO: use principal provider to retrieve admin principal
        Set<AdminPrincipal> principals = singleton(() -> OakSlingRepository.this.adminId);
        AuthInfo authInfo = new AuthInfoImpl(this.adminId, emptyMap(), principals);
        Subject subject = new Subject(true, principals, singleton(authInfo), emptySet());
        try {
            return Subject.doAsPrivileged(subject, new PrivilegedExceptionAction<Session>() {
                @Override
                public Session run() throws Exception {
                    Map<String, Object> attrs = new HashMap<String, Object>();
                    attrs.put("oak.refresh-interval", 0);
                    // TODO OAK-803: Backwards compatibility of long-lived sessions
                    return getJackrabbitRepository().login(null, null, attrs);
                }
            }, null);
        } catch (PrivilegedActionException e) {
            throw new RepositoryException("failed to retrieve admin session.", e);
        }
    }

    @Override
    protected Session createServiceSession(Iterable<String> servicePrincipalNames, String workspaceName)
            throws RepositoryException {
        Set<SystemUserPrincipal> principals = new HashSet<>();
        for (final String pName : servicePrincipalNames)
            principals.add(() -> pName);

        // make sure to retain the first user id from the passed in servicePrincipalNames, for consistency
        AuthInfo authInfo = new AuthInfoImpl(first(servicePrincipalNames), emptyMap(), principals);
        Subject subject = new Subject(true, principals, singleton(authInfo), emptySet());
        try {
            return Subject.doAsPrivileged(subject, new PrivilegedExceptionAction<Session>() {
                @Override
                public Session run() throws Exception {
                    return getRepository().login(null, workspaceName);
                }
            }, null);
        } catch (PrivilegedActionException e) {
            throw new RepositoryException("failed to retrieve service session.", e);
        }
    }

    private JackrabbitRepository getJackrabbitRepository() {
        return (JackrabbitRepository) super.getRepository();
    }
}
