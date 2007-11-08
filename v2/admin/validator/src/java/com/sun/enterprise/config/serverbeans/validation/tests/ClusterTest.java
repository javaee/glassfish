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

import com.sun.enterprise.config.ConfigContextEvent;
import com.sun.enterprise.config.ConfigException;
import com.sun.enterprise.config.serverbeans.Cluster;
import com.sun.enterprise.config.serverbeans.ServerTags;
import com.sun.enterprise.config.serverbeans.ServerBeansFactory;
import com.sun.enterprise.config.serverbeans.validation.GenericValidator;
import com.sun.enterprise.config.serverbeans.validation.Result;
import com.sun.enterprise.config.serverbeans.validation.ValidationDescriptor;
import java.util.Set;
import java.util.logging.Level;


public class ClusterTest extends GenericValidator {
    
    public ClusterTest(ValidationDescriptor desc) {
        super(desc);
    } 

    void validateAdd(final ConfigContextEvent cce, final Result result)  throws ConfigException{
        checkNameNotDomain(cce, result);
        checkConfigRefValidity(cce, result);
    }

    void validateUpdate(final ConfigContextEvent cce, final Result result) throws ConfigException{
        checkNameNotDomain(cce, result);
        preventInvalidConfigRef(cce, result);
    }



    void checkNameNotDomain(final ConfigContextEvent cce, final Result result)  throws ConfigException{
        final Cluster c = getCluster(cce);
        if ("domain".equals(c.getName())){
            result.failed(smh.getLocalString(getClass().getName() + ".illegalClusterName", 
                                             "Illegal Cluster Name: {0}", new Object[]{c.getName()}));
        }
    }
    
    private Cluster getCluster(final ConfigContextEvent cce)  throws ConfigException{
        return (Cluster) cce.getValidationTarget();
    }
    

    public Result validate(ConfigContextEvent cce) {
        Result result = super.validate(cce); // Before doing custom validation do basic validation
        boolean flag = false;
        String choice = cce.getChoice();
        try {
            if (choice.equals(StaticTest.UPDATE)){
                validateUpdate(cce, result);
            } else if (choice.equals(StaticTest.ADD)){
                validateAdd(cce, result);
            }
        }
        catch (ConfigException ce){
            _logger.log(Level.WARNING, "domainxmlverifier.exception", ce);
        }
            
        return result;
    }

    private final void preventInvalidConfigRef(final ConfigContextEvent cce, final Result result) throws ConfigException {
        if (cce.getName().equals(ServerTags.CONFIG_REF)){
            checkConfigRefValidity((String) cce.getObject(), result);
        }
    }
    
    private final void checkConfigRefValidity(final ConfigContextEvent cce, final Result result) throws ConfigException {
        checkConfigRefValidity(getCluster(cce).getConfigRef(), result);
    }
    
            
    private void checkConfigRefValidity(final String config_ref, final Result result){
        if (config_ref.equals(StaticTest.DAS_CONFIG_NAME)){
            result.failed(smh.getLocalString(getClass().getName()+".cannotHaveDASasConfig",
                                             "The configuration of the Domain Administration Server (named {0}) cannot be referenced by a cluster",
                                             new Object[]{StaticTest.DAS_CONFIG_NAME}));
        } else if (config_ref.equals(StaticTest.CONFIG_TEMPLATE_NAME)){
            result.failed(smh.getLocalString(getClass().getName()+".cannotHaveTemplateConfig",
                                             "The default configuration template (named {0}) cannot be referenced by a cluster",
                                             new Object[]{StaticTest.CONFIG_TEMPLATE_NAME}));

        }
    }

}


