/*
 * The contents of this file are subject to the terms
 * of the Common Development and Distribution License
 * (the License).  You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the license at
 * https://glassfish.dev.java.net/public/CDDLv1.0.html or
 * glassfish/bootstrap/legal/CDDLv1.0.txt.
 * See the License for the specific language governing
 * permissions and limitations under the License.
 *
 * When distributing Covered Code, include this CDDL
 * Header Notice in each file and include the License file
 * at glassfish/bootstrap/legal/CDDLv1.0.txt.
 * If applicable, add the following below the CDDL Header,
 * with the fields enclosed by brackets [] replaced by
 * you own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Copyright 2007 Sun Microsystems, Inc. All rights reserved.
 */
package org.glassfish.internal.grizzly;

import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.sun.grizzly.tcp.Adapter;
import com.sun.grizzly.util.http.mapper.Mapper;
import org.jvnet.hk2.annotations.ContractProvided;
import org.jvnet.hk2.annotations.Service;

/**
 * Extended that {@link Mapper} that prevent the WebContainer to unregister the current {@link Mapper} configuration.
 *
 * @author Jeanfrancois Arcand
 */
@Service
@ContractProvided(Mapper.class)
public class ContextMapper extends Mapper {
    private final Logger logger;
    private Adapter adapter;
    // The id of the associated network-listener
    protected String id;

    public ContextMapper() {
        this(Logger.getAnonymousLogger());
    }

    public ContextMapper(final Logger logger) {
        this.logger = logger;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void addWrapper(final String hostName, final String contextPath, final String path,
        final Object wrapper, final boolean jspWildCard, final String servletName,
        final boolean isEmptyPathSpecial) {
        super.addWrapper(hostName, contextPath, path, wrapper, jspWildCard,
                servletName, isEmptyPathSpecial);
        if (logger.isLoggable(Level.FINE)) {
            logger.fine("Wrapper-Host: " + hostName + " contextPath " + contextPath
                + " wrapper " + wrapper + " path " + path + " jspWildcard " + jspWildCard +
                " servletName " + servletName + " isEmptyPathSpecial " + isEmptyPathSpecial);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public synchronized void addHost(final String name, final String[] aliases,
        final Object host) {

        super.addHost(name, aliases, host);
        if (logger.isLoggable(Level.FINE)) {
            logger.fine("Host-Host: " + name + " aliases " + Arrays.toString(aliases) + " host " + host);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void addContext(final String hostName, final String path, final Object context,
        final String[] welcomeResources, final javax.naming.Context resources) {
        if (logger.isLoggable(Level.FINE)) {
            logger.fine("Context-Host: " + hostName + " path " + path + " context " + context +
                " port " + getPort());
        }
        // The WebContainer is registering new Context. In that case, we must
        // clean all the previously added information, specially the
        // MappingData.wrapper info as this information cannot apply
        // to this Container.
        if (adapter != null && "org.apache.catalina.connector.CoyoteAdapter".equals(adapter.getClass().getName())) {
            removeContext(hostName, path);
        }
        super.addContext(hostName, path, context, welcomeResources, resources);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public synchronized void removeHost(final String name) {
        // Do let the WebContainer deconfigire us.
        if (logger.isLoggable(Level.FINE)) {
            logger.fine("Faking removal of host: " + name);
        }
    }

    public void setAdapter(final Adapter adapter) {
        this.adapter = adapter;
    }

    public Adapter getAdapter() {
        return adapter;
    }

    /**
     * Sets the id of the associated http-listener on this mapper.
     */
    public void setId(final String id) {
        this.id = id;
    }
}