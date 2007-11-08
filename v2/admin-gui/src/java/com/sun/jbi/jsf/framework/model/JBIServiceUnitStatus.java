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
 * JBIServiceUnitStatus.java
 * 
 * @author ylee
 * @author Graj
 *
 */
package com.sun.jbi.jsf.framework.model;

import java.io.Serializable;
import java.util.logging.Logger;


public class JBIServiceUnitStatus implements Serializable {

    private static final String SERVICE_UNIT_NAME = "ServiceUnitName";
    private static final String SERVICE_UNIT_DESCRIPTION = "ServiceUnitDescription";
    private static final String STATUS_TYPE = "Status";
    private static final String TARGET_NAME_TYPE = "TargetName";

    protected String serviceUnitName; // 0B000000-DBBABDE9030100-0A12437F-01
    protected String serviceUnitDescription; // This represents the Application Sub-Assembly
    protected String status; // UNKNOWN
    protected String targetName; // xsltserviceengine-9bfbff60-467d-11d9-9669-0800200c9a67

    private Logger logger = Logger.getLogger(JBIServiceUnitStatus.class.getName());
    
    /**
     *
     */
    public JBIServiceUnitStatus() {
        super();
        // TODO Auto-generated constructor stub
    }

    /**
     * @return Returns the serviceUnitDescription.
     */
    public String getServiceUnitDescription() {
        return this.serviceUnitDescription;
    }
    
    /**
     * @param serviceUnitDescription The serviceUnitDescription to set.
     */
    public void setServiceUnitDescription(String serviceUnitDescription) {
        this.serviceUnitDescription = serviceUnitDescription;
    }
    
    /**
     * @return Returns the serviceUnitName.
     */
    public String getServiceUnitName() {
        return this.serviceUnitName;
    }
    
    /**
     * @param serviceUnitName The serviceUnitName to set.
     */
    public void setServiceUnitName(String serviceUnitName) {
        this.serviceUnitName = serviceUnitName;
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
    
    /**
     * @return Returns the targetName.
     */
    public String getTargetName() {
        return this.targetName;
    }
    
    /**
     * @param targetName The targetName to set.
     */
    public void setTargetName(String targetName) {
        this.targetName = targetName;
    }
    

}
