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

package com.sun.enterprise.config.serverbeans.validation.tests;

import java.util.Set;
import java.util.Vector;
import java.util.logging.Level;

import com.sun.enterprise.config.ConfigContextEvent;
import com.sun.enterprise.config.ConfigException;
import com.sun.enterprise.config.serverbeans.AdminObjectResource;
import com.sun.enterprise.config.serverbeans.AppclientModule;
import com.sun.enterprise.config.serverbeans.ApplicationRef;
import com.sun.enterprise.config.serverbeans.Applications;
import com.sun.enterprise.config.serverbeans.Config;
import com.sun.enterprise.config.serverbeans.Configs;
import com.sun.enterprise.config.serverbeans.ConnectorConnectionPool;
import com.sun.enterprise.config.serverbeans.ConnectorModule;
import com.sun.enterprise.config.serverbeans.ConnectorResource;
import com.sun.enterprise.config.serverbeans.CustomResource;
import com.sun.enterprise.config.serverbeans.Domain;
import com.sun.enterprise.config.serverbeans.EjbModule;
import com.sun.enterprise.config.serverbeans.ExternalJndiResource;
import com.sun.enterprise.config.serverbeans.J2eeApplication;
import com.sun.enterprise.config.serverbeans.JdbcConnectionPool;
import com.sun.enterprise.config.serverbeans.JdbcResource;
import com.sun.enterprise.config.serverbeans.LifecycleModule;
import com.sun.enterprise.config.serverbeans.MailResource;
import com.sun.enterprise.config.serverbeans.PersistenceManagerFactoryResource;
import com.sun.enterprise.config.serverbeans.ResourceAdapterConfig;
import com.sun.enterprise.config.serverbeans.ResourceRef;
import com.sun.enterprise.config.serverbeans.Resources;
import com.sun.enterprise.config.serverbeans.Server;
import com.sun.enterprise.config.serverbeans.ServerTags;
import com.sun.enterprise.config.serverbeans.ServerBeansFactory;
import com.sun.enterprise.config.serverbeans.Servers;
import com.sun.enterprise.config.serverbeans.WebModule;
import com.sun.enterprise.config.serverbeans.validation.GenericValidator;
import com.sun.enterprise.config.serverbeans.validation.Result;
import com.sun.enterprise.config.serverbeans.validation.ValidationDescriptor;

/**
   Custom Test for Server Element which calls the Generic Validation before performing custom tests

   @author Srinivas Krishnan
   @version 2.0
*/

public class ServerTest extends GenericValidator {
    
    public ServerTest(ValidationDescriptor desc) {
        super(desc);
    } 

    void validateAdd(final ConfigContextEvent cce, final Result result) throws ConfigException {
        checkNameNotAgent(cce, result);
        checkConfigRefValidity(cce, result);
    }

    void validateUpdate(final ConfigContextEvent cce, final Result result) throws ConfigException {
        validateAttributeChange(cce, result);
    }


    protected final String getName(final ConfigContextEvent cce) throws ConfigException {
        return getServer(cce).getName();

    }
    protected final boolean isReferenced(final ConfigContextEvent cce) throws ConfigException {
        return ServerBeansFactory.isReferenced(getServer(cce), cce.getConfigContext());
    }
    
    protected final Set getReferers(final ConfigContextEvent cce) throws ConfigException{
        return ServerBeansFactory.getReferers(getServer(cce), cce.getConfigContext());
    }

    private void checkNameNotAgent(final ConfigContextEvent cce, final Result result) throws ConfigException {
        final Server s = getServer(cce);
        if ("agent".equals(s.getName())){
            result.failed(smh.getLocalString(getClass().getName() + ".illegalServerName", 
                                             "Illegal Server Name {0}", new Object[]{s.getName()}));
        }
    }

   private Server getServer(final ConfigContextEvent cce) throws ConfigException {
       return (Server) cce.getValidationTarget();
   }
    private void preventChangeToNodeAgentRef(final ConfigContextEvent cce, final Result result){
        if (cce.getName().equals(ServerTags.NODE_AGENT_REF)){
            result.failed(smh.getLocalString(getClass().getName()+".noChangeToNodeAgentRef",
                                             "Cannot change a node agent ref"));
        }
    }
    
    

    public Result validate(ConfigContextEvent cce) {
        Result result = super.validate(cce); // Before doing custom validation do basic validation
        boolean flag = false;
        String choice = cce.getChoice();
        try {
            if (choice.equals(StaticTest.SET)){
                validateSet(cce, result);
            } else if (choice.equals(StaticTest.UPDATE)){
                validateUpdate(cce, result);
            } else if (choice.equals(StaticTest.ADD)){
                validateAdd(cce, result);
            } else if(choice.equals(StaticTest.VALIDATE)) {
                Domain domain = (Domain)cce.getConfigContext().getRootConfigBean();
                Servers servers = domain.getServers();
                Server[] server = servers.getServer();
                    String svrName = server[0].getName();
                    if(!server[0].getName().equals("server")) 
                        result.failed(smh.getLocalString(getClass().getName() + ".invalidserverName", 
                                                         "Invalid Server Name {0}: Required 'server'", new Object[]{svrName}));
            }
            
            
            } catch(Exception e) {
                _logger.log(Level.FINE, "domainxmlverifier.exception", e);
            }
            return result;            
    }

    private void validateSet(final ConfigContextEvent cce, final Result result) throws ConfigException {
        validateAttributeChange(cce, result);
    }

    private void validateAttributeChange(final ConfigContextEvent cce, final Result result) throws ConfigException {
        checkNameNotAgent(cce, result);
        preventChangeToNodeAgentRef(cce, result);
        if (this.isReferenced(cce)){
            preventChangeToConfigRef(cce, result);
        } else {
            preventInvalidConfigRef(cce, result);
        }
    }

    private final void preventInvalidConfigRef(final ConfigContextEvent cce, final Result result) throws ConfigException {
        if (cce.getName().equals(ServerTags.CONFIG_REF)){
            checkConfigRefValidity((String) cce.getObject(), result);
        }
    }
    
    private final void checkConfigRefValidity(final ConfigContextEvent cce, final Result result) throws ConfigException {
        checkConfigRefValidity(getServer(cce).getConfigRef(), result);
    }
    
            
    private void checkConfigRefValidity(final String config_ref, final Result result){
        if (config_ref.equals(StaticTest.DAS_CONFIG_NAME)){
            result.failed(smh.getLocalString(getClass().getName()+".cannotHaveDASasConfig",
                                             "The configuration of the Domain Administration Server (named {0}) cannot be referenced by a server",
                                             new Object[]{StaticTest.DAS_CONFIG_NAME}));
        } else if (config_ref.equals(StaticTest.CONFIG_TEMPLATE_NAME)){
            result.failed(smh.getLocalString(getClass().getName()+".cannotHaveTemplateConfig",
                                             "The default configuration template (named {0}) cannot be referenced by a server",
                                             new Object[]{StaticTest.CONFIG_TEMPLATE_NAME}));

        }
    }
        

    private void preventChangeToConfigRef(final ConfigContextEvent cce, final Result result){
        if (cce.getName().equals(ServerTags.CONFIG_REF)){
            result.failed(smh.getLocalString(getClass().getName()+".noChangeToConfigRefInCluster",
                                             "Cannot change a config-ref when the instance is part of a cluster"));
        }
    }
    

}
