
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
 * JBIServiceAssemblyStatusDocument.java
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

/**

 *
 */
public class JBIServiceAssemblyStatusDocument implements Serializable {

    public static final String SERVICE_ASSEMBLY_INFO_LIST_NODE_NAME = "service-assembly-info-list";
    public static final String SERVICE_ASSEMBLY_INFO_NODE_NAME = "service-assembly-info";
    public static final String NAME_NODE_NAME = "name";
    public static final String DESCRIPTION_NODE_NAME = "description";
    public static final String STATUS_NODE_NAME = "status";
    public static final String SERVICE_UNIT_INFO_LIST_NODE_NAME = "service-unit-info-list";
    public static final String SERVICE_UNIT_INFO_NODE_NAME = "service-unit-info";
    public static final String TARGET_NAME_NODE_NAME = "target-name";

    protected List<JBIServiceAssemblyStatus> jbiServiceAssemblyStatusList = new ArrayList<JBIServiceAssemblyStatus>();
    
    private Logger logger = Logger.getLogger(JBIServiceAssemblyStatusDocument.class.getName());

    /**
     *
     */
    public JBIServiceAssemblyStatusDocument() {
    }


    /**
     * @return Returns the jbiServiceAssemblyList.
     */
    public List<JBIServiceAssemblyStatus> getJbiServiceAssemblyStatusList() {
        return this.jbiServiceAssemblyStatusList;
    }

    /**
     * @param jbiServiceAssemblyList The jbiServiceAssemblyList to set.
     */
    public void setJbiServiceAssemblyStatusList(List<JBIServiceAssemblyStatus> jbiServiceAssemblyStatusList) {
        this.jbiServiceAssemblyStatusList = jbiServiceAssemblyStatusList;
    }

    
    public void addJbiServiceAssemblyStatus(JBIServiceAssemblyStatus serviceAssemblyStatus) {
        jbiServiceAssemblyStatusList.add(serviceAssemblyStatus);
    }

    public void addJbiServiceAssemblyStatusList(List<JBIServiceAssemblyStatus> serviceAssemblyStatus) {
        jbiServiceAssemblyStatusList.addAll(serviceAssemblyStatus);
    }
    

    public void dump() {
        Iterator iterator = this.jbiServiceAssemblyStatusList.iterator();
        JBIServiceAssemblyStatus serviceAssemblyStatus = null;
        while((iterator != null) && (iterator.hasNext() == true)) {
            serviceAssemblyStatus = (JBIServiceAssemblyStatus) iterator.next();
            if(serviceAssemblyStatus != null) {
                logger.info("/////////////////////////////////////////////////");
                logger.info("//  -- JBI Component --                        //");
                logger.info("/////////////////////////////////////////////////");
                logger.info("//  name is: "+ serviceAssemblyStatus.serviceAssemblyName);
                logger.info("//  description is: "+ serviceAssemblyStatus.serviceAssemblyDescription);
                logger.info("//  state is: "+ serviceAssemblyStatus.status);
                JBIServiceUnitStatus unitStatus = null;
                Iterator innerIterator = serviceAssemblyStatus.jbiServiceUnitStatusList.iterator();
                while(innerIterator.hasNext() == true) {
                    unitStatus = (JBIServiceUnitStatus)innerIterator.next();
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

    }
    
    

}

