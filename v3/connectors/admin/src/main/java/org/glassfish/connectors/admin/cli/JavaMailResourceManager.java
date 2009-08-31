/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2008 Sun Microsystems, Inc. All rights reserved.
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

package org.glassfish.connectors.admin.cli;

import org.jvnet.hk2.annotations.Service;
import org.jvnet.hk2.config.ConfigSupport;
import org.jvnet.hk2.config.SingleConfigCode;
import org.jvnet.hk2.config.TransactionFailure;
import org.jvnet.hk2.config.types.Property;

import org.glassfish.api.I18n;
import org.glassfish.admin.cli.resources.ResourceManager;
import org.glassfish.resource.common.ResourceStatus;
import static org.glassfish.resource.common.ResourceConstants.*;

import com.sun.enterprise.config.serverbeans.*;
import com.sun.enterprise.util.LocalStringManagerImpl;

import java.util.HashMap;
import java.util.Properties;
import java.beans.PropertyVetoException;


@Service(name= ServerTags.MAIL_RESOURCE)
@I18n("add.resources")
public class JavaMailResourceManager implements ResourceManager {

    final private static LocalStringManagerImpl localStrings =
            new LocalStringManagerImpl(JavaMailResourceManager.class);
    private static final String DESCRIPTION = ServerTags.DESCRIPTION;

    String mailHost = null;
    String mailUser = null;
    String fromAddress = null;
    String jndiName = null;
    String storeProtocol = null;
    String storeProtocolClass = null;
    String transportProtocol = null;
    String transportProtocolClass = null;
    String enabled = null;
    String debug = null;
    String description = null;

    public String getResourceType() {
        return ServerTags.MAIL_RESOURCE;
    }

    public ResourceStatus create(Resources resources, HashMap attrList, final Properties props, Server targetServer)
            throws Exception {
        setParams(attrList);
        
        if (mailHost == null) {
            String msg = localStrings.getLocalString("create.mail.resource.noHostName",
                            "No host name defined for Mail Resource.");
            ResourceStatus status = new ResourceStatus(ResourceStatus.FAILURE, msg, true);
            return status;
        }

        if (mailUser == null) {
            String msg = localStrings.getLocalString("create.mail.resource.noUserName",
                            "No user name defined for Mail Resource.");
            ResourceStatus status = new ResourceStatus(ResourceStatus.FAILURE, msg, true);
            return status;
        }

        if (fromAddress == null) {
            String msg = localStrings.getLocalString("create.mail.resource.noFrom",
                            "From not defined for Mail Resource.");
            ResourceStatus status = new ResourceStatus(ResourceStatus.FAILURE, msg, true);
            return status;
        }


        // ensure we don't already have one of this name
        for (Resource resource : resources.getResources()) {
            if (resource instanceof BindableResource) {
                if (((BindableResource) resource).getJndiName().equals(jndiName)) {
                   String msg = localStrings.getLocalString(
                            "create.mail.resource.duplicate.1",
                            "A Mail Resource named {0} already exists.",
                            jndiName);
                    ResourceStatus status = new ResourceStatus(ResourceStatus.FAILURE, msg, true);
                    return status;
                }
            }
        }

        try {
            ConfigSupport.apply(new SingleConfigCode<Resources>() {

                public Object run(Resources param) throws PropertyVetoException,
                        TransactionFailure {

                    MailResource newResource = param.createChild(
                            MailResource.class);
                    newResource.setJndiName(jndiName);
                    newResource.setFrom(fromAddress);
                    newResource.setUser(mailUser);
                    newResource.setHost(mailHost);
                    newResource.setEnabled(enabled.toString());
                    newResource.setStoreProtocol(storeProtocol);
                    newResource.setStoreProtocolClass(storeProtocolClass);
                    newResource.setTransportProtocol(transportProtocol);
                    newResource.setTransportProtocolClass(
                            transportProtocolClass);
                    newResource.setDebug(debug.toString());
                    if (description != null) {
                        newResource.setDescription(description);
                    }
                    if (props != null) {
                        for ( java.util.Map.Entry e : props.entrySet()) {
                            Property prop = newResource.createChild(
                                    Property.class);
                            prop.setName((String)e.getKey());
                            prop.setValue((String)e.getValue());
                            newResource.getProperty().add(prop);
                        }
                    }
                    param.getResources().add(newResource);
                    return newResource;
                }
            }, resources);

            if (!targetServer.isResourceRefExists( jndiName)) {
                targetServer.createResourceRef( enabled.toString(), jndiName);
            }
            String msg = localStrings.getLocalString(
                    "create.mail.resource.success",
                    "Mail Resource {0} created.", jndiName);
            ResourceStatus status = new ResourceStatus(ResourceStatus.SUCCESS, msg, true);
            return status;
        } catch(TransactionFailure tfe) {
            String msg = localStrings.getLocalString("" +
                    "create.mail.resource.fail",
                    "Unable to create Mail Resource {0}.", jndiName) +
                    " " + tfe.getLocalizedMessage();
            ResourceStatus status = new ResourceStatus(ResourceStatus.FAILURE, msg, true);
            return status;
        }


    }

    private void setParams(HashMap attrList) {
        jndiName = (String) attrList.get(JNDI_NAME);
        mailHost = (String) attrList.get(MAIL_HOST);
        mailUser = (String) attrList.get(MAIL_USER);
        fromAddress = (String) attrList.get(MAIL_FROM_ADDRESS);
        storeProtocol = (String) attrList.get(MAIL_STORE_PROTO);
        storeProtocolClass = (String) attrList.get(MAIL_STORE_PROTO_CLASS);
        transportProtocol = (String) attrList.get(MAIL_TRANS_PROTO);
        transportProtocolClass = (String) attrList.get(MAIL_TRANS_PROTO_CLASS);
        debug = (String) attrList.get(MAIL_DEBUG);
        enabled = (String) attrList.get(ENABLED);
        description = (String) attrList.get(DESCRIPTION);
    }
}
