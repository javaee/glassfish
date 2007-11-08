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

package com.sun.enterprise.config.serverbeans.validation;

import java.util.Vector;
import java.util.logging.Level;

import com.sun.enterprise.config.ConfigContext;
import com.sun.enterprise.config.ConfigException;
import com.sun.enterprise.config.serverbeans.validation.tests.StaticTest;

import com.sun.enterprise.config.serverbeans.Domain;
import com.sun.enterprise.config.serverbeans.Resources;
import com.sun.enterprise.config.serverbeans.AdminObjectResource;
import com.sun.enterprise.config.serverbeans.ConnectorConnectionPool;
import com.sun.enterprise.config.serverbeans.CustomResource;
import com.sun.enterprise.config.serverbeans.ConnectorResource;
import com.sun.enterprise.config.serverbeans.ExternalJndiResource;
import com.sun.enterprise.config.serverbeans.JdbcConnectionPool;
import com.sun.enterprise.config.serverbeans.JdbcResource;
import com.sun.enterprise.config.serverbeans.MailResource;
import com.sun.enterprise.config.serverbeans.PersistenceManagerFactoryResource;
import com.sun.enterprise.config.serverbeans.ResourceAdapterConfig;

/**
    Class which contains Meta data for all types of attributes which is present in Validation Descriptor
 *  XML File
 *
 *  Sample
 *      <attribute name=<Name> type="address" />
 *      <attribute name=<Name> type="integer" range="low,high" />
 *      <attribute name=<Name> type="string"  max-length="length" />
    
    @author Srinivas Krishnan
    @version 2.0
*/

/* Class for attribute type Resource (to check uniqueness of jndiname across resources) */

public class AttrUniqueJNDI extends AttrType {
    
    public AttrUniqueJNDI(String name, String type, boolean optional) {
        super(name,type, optional);
    }
    
    public void validate(Object o, ValidationContext valCtx) {
        super.validate(o, valCtx);  // call to common validator first
        String jndiName = null;
        String choice = valCtx.choice;
        boolean flag = false;
        if(o == null || o.equals(""))
            return;
        jndiName = (String)o;
        if(choice.equals("ADD") && isResourceBeingUsed(valCtx.context, jndiName)){
            valCtx.result.failed(
                valCtx.smh.getLocalString(
                    getClass().getName() + ".jndiNameBeingUsed",
                    "Attribute({0}={1}) :  name already used by some other resource",
                    new Object[]{valCtx.attrName,jndiName}));
        }
            // apostrophes aren't allowed in jndi names - we catch
            // this by hard coding this check because this type
            // (AttrUniqueJNDI) doesn't take a regular expression.
            // This is an implementation limitation and is caused by
            // the way we handle xpath expressions - we use the
            // apostrophe to delimit the primary key names of
            // elements, and we don't escape the names when we get
            // them from the user. Therefore if the name were allowed
            // to contain an apostrophe the name would be incorrectly
            // split into two parts. Bug 6184639 details this problem.
        if (jndiName.indexOf("'") != -1){
            valCtx.result.failed(
                valCtx.smh.getLocalString(
                    getClass().getName() + ".invalidJndiName",
                    "Apostrophe not allowed in jndi name: {0}",
                    new Object[]{jndiName}));
        }
        
    }
    
    public boolean isResourceBeingUsed(ConfigContext context, String jndi) {
        
        int count=0;
        try {
            Resources resources  = ((Domain)context.getRootConfigBean()).getResources();
            
            AdminObjectResource[] admin = resources.getAdminObjectResource();
            ConnectorResource[] connres = resources.getConnectorResource();
            CustomResource[] custom = resources.getCustomResource();
            ExternalJndiResource[] external = resources.getExternalJndiResource();
            JdbcResource[] jdbcres = resources.getJdbcResource();
            MailResource[] mailres = resources.getMailResource();
            PersistenceManagerFactoryResource[] pers = resources.getPersistenceManagerFactoryResource();
            
            for(int i=0;i<admin.length;i++) {
                if(jndi.equals(admin[i].getJndiName())) 
                    return true;
            }
            for(int i=0;i<connres.length;i++) {
                if(jndi.equals(connres[i].getJndiName())) 
                    return true;
            }
            
            for(int i=0;i<custom.length;i++) {
                if(jndi.equals(custom[i].getJndiName())) 
                    return true;
            }
            
            for(int i=0;i<external.length;i++) {
                if(jndi.equals(external[i].getJndiName())) 
                    return true;
            }
            
            for(int i=0;i<jdbcres.length;i++) {
                if(jndi.equals(jdbcres[i].getJndiName()))
                    return true;
            }
            
            for(int i=0;i<mailres.length;i++) {
                if(jndi.equals(mailres[i].getJndiName()))
                    return true;
            }
            
            for(int i=0;i<pers.length;i++) {
                if(jndi.equals(pers[i].getJndiName())) 
                    return true;
            }
            
        } catch(ConfigException e) {
            _logger.log(Level.FINE, "domainxmlverifier_error", e);
        }
        return false;
    }
    
}
