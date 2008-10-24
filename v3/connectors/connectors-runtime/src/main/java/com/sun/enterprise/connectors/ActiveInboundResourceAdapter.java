/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
 * 
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License. You can obtain
 * a copy of the License at https://glassfish.dev.java.net/public/CDDL+GPL.html
 * or glassfish/bootstrap/legal/LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 * 
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at glassfish/bootstrap/legal/LICENSE.txt.
 * Sun designates this particular file as subject to the "Classpath" exception
 * as provided by Sun in the GPL Version 2 section of the License file that
 * accompanied this code.  If applicable, add the following below the License
 * Header, with the fields enclosed by brackets [] replaced by your own
 * identifying information: "Portions Copyrighted [year]
 * [name of copyright owner]"
 * 
 * Contributor(s):
 * 
 * If you wish your version of this file to be governed by only the CDDL or
 * only the GPL Version 2, indicate your decision by adding "[Contributor]
 * elects to include this software in this distribution under the [CDDL or GPL
 * Version 2] license."  If you don't indicate a single choice of license, a
 * recipient has the option to distribute your version of this file under
 * either the CDDL, the GPL Version 2 or to extend the choice of license to
 * its licensees as provided above.  However, if you add GPL Version 2 code
 * and therefore, elected the GPL Version 2 license, then the option applies
 * only if the new code is made subject to such option by the copyright
 * holder.
 */

package com.sun.enterprise.connectors;

import com.sun.appserv.connectors.internal.api.ConnectorRuntimeException;
import org.glassfish.api.admin.config.Property;
import com.sun.enterprise.config.serverbeans.ResourceAdapterConfig;
import com.sun.enterprise.connectors.util.ConnectorDDTransformUtils;
import com.sun.enterprise.connectors.util.SetMethodAction;
import com.sun.enterprise.deployment.ConnectorDescriptor;
import com.sun.enterprise.util.i18n.StringManager;
import com.sun.logging.LogDomains;

import javax.resource.ResourceException;
import javax.resource.spi.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * This class represents a live inbound resource adapter, i.e.
 * <p/>
 * A resource adapter is considered active after start()
 * and before stop() is called.
 *
 * @author Binod P G, Sivakumar Thyagarajan
 */

public class ActiveInboundResourceAdapter extends ActiveOutboundResourceAdapter {

    protected ResourceAdapter resourceadapter_; //runtime instance

    protected String moduleName_;

    private static Logger _logger = LogDomains.getLogger(ActiveInboundResourceAdapter.class,LogDomains.RSR_LOGGER);

    private StringManager localStrings =
            StringManager.getManager(ActiveInboundResourceAdapter.class);

    private BootstrapContext bootStrapContextImpl;

    /**
     * Creates an active inbound resource adapter. Sets all RA java bean
     * properties and issues a start.
     *
     * @param ra         <code>ResourceAdapter<code> java bean.
     * @param desc       <code>ConnectorDescriptor</code> object.
     * @param moduleName Resource adapter module name.
     * @param jcl        <code>ClassLoader</code> instance.
     * @throws ConnectorRuntimeException If there is a failure in loading
     *                                   or starting the resource adapter.
     */
    public ActiveInboundResourceAdapter(
            ResourceAdapter ra, ConnectorDescriptor desc, String moduleName,
            ClassLoader jcl) throws ConnectorRuntimeException {
        super(desc, moduleName, jcl);
        this.resourceadapter_ = ra;
        this.moduleName_ = moduleName;
        try {
            loadRAConfiguration();
            ConnectorRegistry registry = ConnectorRegistry.getInstance();
            String poolId = null;
            ResourceAdapterConfig raConfig = registry.getResourceAdapterConfig(moduleName_);
            if (raConfig != null) {
                poolId = raConfig.getThreadPoolIds();
            }
            this.bootStrapContextImpl = new BootstrapContextImpl(poolId, moduleName_);

            resourceadapter_.start(bootStrapContextImpl);

        } catch (ResourceAdapterInternalException ex) {
            _logger.log(Level.SEVERE, "rardeployment.start_failed", ex);
            String i18nMsg = localStrings.getString("rardeployment.start_failed", ex.getMessage());
            ConnectorRuntimeException cre = new ConnectorRuntimeException(i18nMsg);
            cre.initCause(ex);
            throw cre;
        } catch (Throwable t) {
            _logger.log(Level.SEVERE, "rardeployment.start_failed", t);
            t.printStackTrace();
            String i18nMsg = localStrings.getString("rardeployment.start_failed", t.getMessage());
            ConnectorRuntimeException cre = new ConnectorRuntimeException(i18nMsg);
            if (t.getCause() != null) {
                cre.initCause(t.getCause());
            } else {
                cre.initCause(t);
            }
            throw cre;
        }
    }

    /**
     * Retrieves the resource adapter java bean.
     *
     * @return <code>ResourceAdapter</code>
     */
    public ResourceAdapter getResourceAdapter() {
        return this.resourceadapter_;
    }

    /**
     * Does the necessary initial setup. Creates the default pool and
     * resource.
     *
     * @throws ConnectorRuntimeException If there is a failure
     */
    public void setup() throws ConnectorRuntimeException {
        //TODO V3 need for this check ?
        if (connectionDefs_ == null || connectionDefs_.length == 0) {
            return;
        }
        super.setup();
    }

    /**
     * Destroys default pools and resources. Stops the Resource adapter
     * java bean.
     */
    public void destroy() {
        super.destroy();
        try {
            _logger.fine("Calling Resource Adapter stop" + this.getModuleName());
            resourceadapter_.stop();
            _logger.fine("Resource Adapter stop call of " + this.getModuleName() + "returned successfully");
            _logger.log(Level.FINE, "rar_stop_call_successful");
        } catch (Throwable t) {
            _logger.log(Level.SEVERE, "rardeployment.stop_warning", t);
        }finally{
            removeProxiesFromRegistry(moduleName_);
        }
    }

    /**
     * Remove all the proxy objects (Work-Manager) from connector registry
     * @param moduleName_ resource-adapter name
     */
    private void removeProxiesFromRegistry(String moduleName_) {
        ConnectorRuntime.getRuntime().removeWorkManagerProxy(moduleName_);
    }


    /**
     * Creates an instance of <code>ManagedConnectionFactory</code>
     * object using the connection pool properties. Also set the
     * <code>ResourceAdapterAssociation</code>
     *
     * @param pool <code>ConnectorConnectionPool</code> properties.
     * @param jcl  <code>ClassLoader</code>
     */
    public ManagedConnectionFactory createManagedConnectionFactory(
            ConnectorConnectionPool pool, ClassLoader jcl) {
        ManagedConnectionFactory mcf;
        mcf = super.createManagedConnectionFactory(pool, jcl);

        if (mcf instanceof ResourceAdapterAssociation) {
            try {
                ((ResourceAdapterAssociation) mcf).setResourceAdapter(this.resourceadapter_);
            } catch (ResourceException ex) {
                _logger.log(Level.SEVERE, "rardeployment.assoc_failed", ex);
            }
        }
        return mcf;
    }

    /**
     * Loads RA javabean. This method is protected, so that any system
     * resource adapter can have specific configuration done during the
     * loading.
     *
     * @throws ConnectorRuntimeException if there is a failure.
     */
    protected void loadRAConfiguration() throws ConnectorRuntimeException {
        try {
            Set mergedProps;
            ConnectorRegistry registry = ConnectorRegistry.getInstance();
            ResourceAdapterConfig raConfig = registry.getResourceAdapterConfig(moduleName_);
            List<Property> raConfigProps = new ArrayList<Property>();
            if (raConfig != null) {
                raConfigProps = raConfig.getProperty();
            }
            mergedProps = ConnectorDDTransformUtils.mergeProps(raConfigProps, getDescriptor().getConfigProperties());

            SetMethodAction setMethodAction = new SetMethodAction(this.resourceadapter_, mergedProps);
            setMethodAction.run();
        } catch (Exception e) {
            String i18nMsg = localStrings.getString("ccp_adm.wrong_params_for_create", e.getMessage());
            ConnectorRuntimeException cre = new ConnectorRuntimeException(i18nMsg);
            cre.initCause(e);
            throw cre;
        }
    }

    public BootstrapContext getBootStrapContext() {
        return this.bootStrapContextImpl;
    }
}
