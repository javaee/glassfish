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

package com.sun.enterprise.management.model;

import javax.management.ObjectName;
import java.util.Set;

public abstract class EJBMdl extends J2EEManagedObjectMdl {
    
    private String ejbModuleName = null;
    private String applicationName = null;
    private String ejbName = null;
    
    EJBMdl(String name, String moduleName, String appName) {
        super(name,false, false, false);
        this.ejbModuleName = moduleName;
        this.applicationName = appName;
        if(J2EEModuleMdl.isStandAloneModule(applicationName))
           this.applicationName = "null";
        this.ejbName = name;
    }

    EJBMdl(String name, String moduleName, String appName, String serverName) {
        super(name, serverName, false, false, false);
        this.ejbModuleName = moduleName;
        this.applicationName = appName;
        if(J2EEModuleMdl.isStandAloneModule(applicationName))
           this.applicationName = "null";
        this.ejbName = name;
    }

    /**
     * Accessor method for the parent key
     */
    public String getEJBModule(){
       return this.ejbModuleName;
    }
    /**
     * Accessor method for the parent key
     */
    public String getJ2EEApplication(){
       return this.applicationName;
    }
    
    /**
     * The name of the J2EEManagedObject. All managed objects must have a unique name within the context of the management
     * domain. The name must not be null.
     */
    public String getobjectName() {
        Set s = findNames("j2eeType="+getj2eeType()+",name="+this.ejbName+",EJBModule="+this.getEJBModule()+",J2EEApplication="+this.getJ2EEApplication()+",J2EEServer="+this.getJ2EEServer());
        Object [] objs = s.toArray();
        if (objs.length > 0) {
        	String name = ((ObjectName)objs[0]).toString();
        	return name;
        } else {
            return null;
        }
    }
}
