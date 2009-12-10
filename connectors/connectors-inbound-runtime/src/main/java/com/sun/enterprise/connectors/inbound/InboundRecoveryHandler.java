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
package com.sun.enterprise.connectors.inbound;

import com.sun.enterprise.transaction.spi.RecoveryResourceHandler;
import com.sun.enterprise.connectors.util.RARUtils;
import com.sun.enterprise.connectors.util.SetMethodAction;
import com.sun.enterprise.connectors.ConnectorRegistry;
import com.sun.enterprise.connectors.service.ConnectorAdminServiceUtils;
import com.sun.enterprise.deployment.EjbDescriptor;
import com.sun.enterprise.deployment.EjbMessageBeanDescriptor;
import com.sun.enterprise.deployment.BundleDescriptor;
import com.sun.enterprise.deployment.EjbBundleDescriptor;
import com.sun.enterprise.config.serverbeans.*;
import com.sun.logging.LogDomains;
import com.sun.appserv.connectors.internal.api.ConnectorConstants;
import com.sun.appserv.connectors.internal.api.ConnectorRuntimeException;
import com.sun.appserv.connectors.internal.api.ConnectorsUtil;
import com.sun.appserv.connectors.internal.api.ConnectorRuntime;

import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.jvnet.hk2.annotations.Service;
import org.jvnet.hk2.annotations.Inject;
import org.jvnet.hk2.component.Habitat;
import org.glassfish.internal.data.ApplicationRegistry;
import org.glassfish.internal.data.ApplicationInfo;

import javax.resource.spi.ActivationSpec;
import javax.transaction.xa.XAResource;


/**
 * Recovery handler for Inbound transactions
 *
 * @author Jagadish Ramu
 */
@Service
public class InboundRecoveryHandler implements RecoveryResourceHandler {

    @Inject
    private Applications deployedApplications;

    @Inject
    private ApplicationRegistry appsRegistry;

    @Inject
    private Habitat connectorRuntimeHabitat;
    

    private static Logger _logger = LogDomains.getLogger(InboundRecoveryHandler.class, LogDomains.RSR_LOGGER);

    /**
     * {@inheritDoc}
     */
    public void loadXAResourcesAndItsConnections(List xaresList, List connList) {
        Vector<XAResource> xaResources = new Vector<XAResource>();
        recoverInboundTransactions(xaResources);
    }

    /**
     * {@inheritDoc}
     */
    public void closeConnections(List connList) {
        // do nothing
    }

    private void recoverInboundTransactions(List<XAResource> xaresList) {

        List<Application> applications = deployedApplications.getApplications();

        try {
            _logger.log(Level.INFO, "Recovery of Inbound Transactions started.");

            if (applications.size() == 0) {
                _logger.log(Level.FINE, "No applications deployed.");
                return;
            }
            // List of CMT enabled MDB descriptors on the application server instance.
            ArrayList<EjbDescriptor> xaEnabledMDBList = new ArrayList<EjbDescriptor>();

            for (Application application : applications) {
                Vector ejbDescVec = getEjbDescriptors(application, appsRegistry);
                for (int j = 0; j < ejbDescVec.size(); j++) {
                    EjbDescriptor desc = (EjbDescriptor) ejbDescVec.elementAt(j);
                    // If EjbDescriptor is an instance of a CMT enabled MDB descriptor,
                    // add it to the list of xaEnabledMDBList.
                    if (desc instanceof EjbMessageBeanDescriptor &&
                            desc.getTransactionType().
                                    equals(EjbDescriptor.CONTAINER_TRANSACTION_TYPE)) {
                        xaEnabledMDBList.add(desc);
                        _logger.log(Level.FINE, "Found a CMT MDB: "
                                + desc.getEjbClassName());
                    }
                }
            }

            if (xaEnabledMDBList.size() == 0) {
                _logger.log(Level.FINE, "Found no CMT MDBs in all applications");
                return;
            }

            //TODO V3 done so as to initialize connectors-runtime before loading inbound active RA. need a better way ?
            ConnectorRuntime cr = connectorRuntimeHabitat.getComponent(ConnectorRuntime.class);
            ConnectorRegistry creg = ConnectorRegistry.getInstance();

            // for each RA (key in the hashtable) get the list (value) of MDB Descriptors
            Hashtable mappings = createRAEjbMapping(xaEnabledMDBList);
            // To iterate through the keys(ramid), get the key Set from Hashtable.
            Set raMidSet = mappings.keySet();

            Iterator iter = raMidSet.iterator();

            //For each RA
            while (iter.hasNext()) {

                String raMid = (String) iter.next();
                ArrayList respectiveDesc = (ArrayList) mappings.get(raMid);

                try {
                    createActiveResourceAdapter(raMid);
                } catch (Exception ex) {
                    _logger.log(Level.SEVERE, "error.loading.connector.resources.during.recovery", raMid);
                    if (_logger.isLoggable(Level.FINE)) {
                        _logger.log(Level.FINE, ex.toString(), ex);
                    }
                }

                ActiveInboundResourceAdapter activeInboundRA = (ActiveInboundResourceAdapter) creg
                        .getActiveResourceAdapter(raMid);

                assert activeInboundRA instanceof ActiveInboundResourceAdapter;

                boolean isSystemJmsRA = false;
                if (ConnectorsUtil.isJMSRA(activeInboundRA.getModuleName())) {
                    isSystemJmsRA = true;
                }

                javax.resource.spi.ResourceAdapter resourceAdapter = activeInboundRA
                        .getResourceAdapter();
                // activationSpecList represents the ActivationSpec[] that would be
                // sent to the getXAResources() method.
                ArrayList<ActivationSpec> activationSpecList = new ArrayList<ActivationSpec>();

                try {
                    for (int i = 0; i < respectiveDesc.size(); i++) {
                        try {
                            // Get a MessageBeanDescriptor from respectiveDesc ArrayList
                            EjbMessageBeanDescriptor descriptor =
                                    (EjbMessageBeanDescriptor) respectiveDesc.get(i);
                            // A descriptor using 1.3 System JMS RA style properties needs
                            // to be updated J2EE 1.4 style props.
                            if (isSystemJmsRA) {
                                //XXX: Find out the pool descriptor corres to MDB and update
                                //MDBRuntimeInfo with that.
                                activeInboundRA.updateMDBRuntimeInfo(descriptor, null);
                            }

                            // Get the ActivationConfig Properties from the MDB Descriptor
                            Set activationConfigProps =
                                    RARUtils.getMergedActivationConfigProperties(descriptor);
                            // get message listener type
                            String msgListenerType = descriptor.getMessageListenerType();

                            // start resource adapter and get ActivationSpec class for
                            // the given message listener type from the ConnectorRuntime

                            ActivationSpec aspec = (ActivationSpec) (Class.forName(
                                    cr.getActivationSpecClass(raMid,
                                            msgListenerType), false,
                                    resourceAdapter.getClass().getClassLoader()).newInstance());
                            aspec.setResourceAdapter(resourceAdapter);

                            // Populate ActivationSpec class with ActivationConfig properties
                            SetMethodAction sma =
                                    new SetMethodAction(aspec, activationConfigProps);
                            sma.run();
                            activationSpecList.add(aspec);
                        } catch (Exception e) {
                            _logger.log(Level.WARNING, "error.creating.activationspec", e.getMessage());
                            if (_logger.isLoggable(Level.FINE)) {
                                _logger.log(Level.FINE, e.toString(), e);
                            }
                        }
                    }

                    // Get XA resources from RA.

                    ActivationSpec[] activationSpecArray = (ActivationSpec[]) activationSpecList.toArray(new ActivationSpec[]{});
                    XAResource[] xar = resourceAdapter.getXAResources(activationSpecArray);

                    // Add the resources to the xaresList which is used by the RecoveryManager
                    for (int p = 0; p < xar.length; p++) {
                        xaresList.add(xar[p]);

                    }
                    // Catch UnsupportedOperationException if a RA does not support XA
                    // which is fine.
                } catch (UnsupportedOperationException uoex) {
                    _logger.log(Level.FINE, uoex.getMessage());
                    // otherwise catch the unexpected exception
                } catch (Exception e) {
                    _logger.log(Level.SEVERE, "exception.during.inbound.resource.acqusition", e);
                }
            }
        } catch (Exception e) {
            _logger.log(Level.SEVERE,"exception.during.inbound.recovery", e);
        }

    }

    private Vector getEjbDescriptors(Application application, ApplicationRegistry appsRegistry) {
        Vector ejbDescriptors = new Vector();

        ApplicationInfo appInfo = appsRegistry.get(application.getName());

        com.sun.enterprise.deployment.Application app = appInfo.getMetaData(com.sun.enterprise.deployment.Application.class);
        Set<BundleDescriptor> descriptors = app.getBundleDescriptors();
        for (BundleDescriptor descriptor : descriptors) {
            if (descriptor instanceof EjbBundleDescriptor) {
                EjbBundleDescriptor ejbBundleDescriptor = (EjbBundleDescriptor) descriptor;
                Set<EjbDescriptor> ejbDescriptorsSet = ejbBundleDescriptor.getEjbs();
                for (EjbDescriptor ejbDescriptor : ejbDescriptorsSet) {
                    ejbDescriptors.add(ejbDescriptor);
                }
            }
        }
        return ejbDescriptors;
    }

    private Hashtable createRAEjbMapping(ArrayList<EjbDescriptor> list) {

        Hashtable ht = new Hashtable();

        for (Object aList : list) {
            ArrayList ejbmdbd = new ArrayList();
            String ramid =
                    ((EjbMessageBeanDescriptor) aList).getResourceAdapterMid();
            if ((ramid == null) || (ramid.equalsIgnoreCase(""))) {
                ramid = ConnectorConstants.DEFAULT_JMS_ADAPTER;
            }

            // If Hashtable contains the RAMid key, get the list of MDB descriptors
            // and add the current MDB Descriptor (list[i]) to the list and put the
            // pair back into hashtable.
            // Otherwise, add the RAMid and the current MDB Descriptor to the hashtable
            if (ht.containsKey(ramid)) {
                ejbmdbd = (ArrayList) ht.get(ramid);
                ht.remove(ramid);
            }

            ejbmdbd.add(aList);
            ht.put(ramid, ejbmdbd);
        }
        return ht;
    }

    private void createActiveResourceAdapter(String rarModuleName) throws ConnectorRuntimeException {

        ConnectorRuntime cr = connectorRuntimeHabitat.getComponent(ConnectorRuntime.class);
        ConnectorRegistry creg = ConnectorRegistry.getInstance();

        if (creg.isRegistered(rarModuleName))
            return;

        if (ConnectorAdminServiceUtils.isEmbeddedConnectorModule(rarModuleName)) {
            cr.createActiveResourceAdapterForEmbeddedRar(rarModuleName);
        } else {
            String moduleDir = ConfigBeansUtilities.getLocation(rarModuleName);
            ClassLoader loader = cr.createConnectorClassLoader(moduleDir, null, rarModuleName);
            cr.createActiveResourceAdapter(moduleDir, rarModuleName, loader);
        }
    }

}
