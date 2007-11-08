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


/*
 * JBIServiceAssemblyStatus.java
 * 
 * @author ylee
 * @author Graj
 *
 */
package com.sun.jbi.jsf.framework.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Logger;


public class JBIServiceAssemblyStatus implements Serializable {

    /** status Deployed. */
    public static final String DEPLOYED_STATUS = "DEPLOYED";

    /** state  Loaded status.  */
    public static final String UNKNOWN_STATUS = "Unknown";
//    public static final String UNKNOWN_STATUS = "UNKNOWN";
    /** status Deployed. */
    public static final String SHUTDOWN_STATUS = "Shutdown";
//    public static final String SHUTDOWN_STATUS = "SHUTDOWN";
    /** Stopped status  */
    public static final String STOP_STATUS = "Stopped";
//    public static final String STOP_STATUS = "STOP";
    /** Started status */
    public static final String START_STATUS = "Started";
//    public static final String START_STATUS = "START";

    protected String serviceAssemblyName; // {01000000-31778EC7020100-0A124913-01}
    protected String serviceAssemblyDescription; // Represents this Assembly Unit
    protected String status; // DEPLOYED

    protected List<JBIServiceUnitStatus> jbiServiceUnitStatusList = new ArrayList<JBIServiceUnitStatus>();
    
    private Logger logger = Logger.getLogger(JBIServiceAssemblyStatus.class.getName()); 

    /**
     *
     */
    public JBIServiceAssemblyStatus() {
    }

    /**
     * @param serviceAssemblyName
     * @param serviceAssemblyDescription
     * @param status
     */
    public JBIServiceAssemblyStatus(String serviceAssemblyName,
            String serviceAssemblyDescription, String status) {
        super();
        this.serviceAssemblyName = serviceAssemblyName;
        this.serviceAssemblyDescription = serviceAssemblyDescription;
        this.status = status;
    }


    /**
     * @param serviceAssemblyName
     * @param serviceAssemblyDescription
     * @param status
     * @param jbiServiceUnitStatusList
     */
    public JBIServiceAssemblyStatus(String serviceAssemblyName,
            String serviceAssemblyDescription, String status,
            List<JBIServiceUnitStatus> jbiServiceUnitStatusList) {
        super();
        this.serviceAssemblyName = serviceAssemblyName;
        this.serviceAssemblyDescription = serviceAssemblyDescription;
        this.status = status;
        this.jbiServiceUnitStatusList = jbiServiceUnitStatusList;
    }
    
    /**
     * @return Returns the jbiServiceUnitList.
     */
    public List<JBIServiceUnitStatus> getJbiServiceUnitStatusList() {
        return this.jbiServiceUnitStatusList;
    }
    
    public void addJbiServiceUnitStatus(JBIServiceUnitStatus suStatus) {
        jbiServiceUnitStatusList.add(suStatus);
    }
    
    /**
     * @param jbiServiceUnitStatusList -    The jbiServiceUnitList to set.
     */
    public void setJbiServiceUnitStatusList(List<JBIServiceUnitStatus> jbiServiceUnitStatusList) {
        this.jbiServiceUnitStatusList = jbiServiceUnitStatusList;
    }
    
    /**
     * @return Returns the serviceAssemblyDescription.
     */
    public String getServiceAssemblyDescription() {
        return this.serviceAssemblyDescription;
    }
    
    /**
     * @param serviceAssemblyDescription The serviceAssemblyDescription to set.
     */
    public void setServiceAssemblyDescription(String serviceAssemblyDescription) {
        this.serviceAssemblyDescription = serviceAssemblyDescription;
    }
    
    /**
     * @return Returns the serviceAssemblyName.
     */
    public String getServiceAssemblyName() {
        return this.serviceAssemblyName;
    }
    
    /**
     * @param serviceAssemblyName The serviceAssemblyName to set.
     */
    public void setServiceAssemblyName(String serviceAssemblyName) {
        this.serviceAssemblyName = serviceAssemblyName;
    }
    
    /**
     * @return Returns the status.
     */
    public String getStatus() {
        return this.status;
    }
    
    /**
     * @param status The status to set.
     */
    public void setStatus(String status) {
        this.status = status;
    }
    
    public void dump() {
        logger.info("/////////////////////////////////////////////////");
        logger.info("//  -- JBI Component --                        //");
        logger.info("/////////////////////////////////////////////////");

        logger.info("//  name is: "+ serviceAssemblyName);
        logger.info("//  description is: "+ serviceAssemblyDescription);
        logger.info("//  state is: "+ status);
        JBIServiceUnitStatus unitStatus = null;
        Iterator iterator = this.jbiServiceUnitStatusList.iterator();
        while(iterator.hasNext() == true) {
            unitStatus = (JBIServiceUnitStatus)iterator.next();
            if(unitStatus != null) {
                logger.info("// --------------------------------------");
                logger.info("// ------ Service Unit ------------------");
                logger.info("// --------------------------------------");
                logger.info("//    name is: "+ unitStatus.getServiceUnitName());
                logger.info("//    description is: "+ unitStatus.getServiceUnitDescription());
                logger.info("//    state is: "+ unitStatus.getStatus());
                logger.info("//    Target Name is: "+ unitStatus.getTargetName());
            }
        }
       logger.info("/////////////////////////////////////////////////");
    }

}

