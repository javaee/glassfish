/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 1997-2017 Oracle and/or its affiliates. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License.  You can
 * obtain a copy of the License at
 * https://oss.oracle.com/licenses/CDDL+GPL-1.1
 * or LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at LICENSE.txt.
 *
 * GPL Classpath Exception:
 * Oracle designates this particular file as subject to the "Classpath"
 * exception as provided by Oracle in the GPL Version 2 section of the License
 * file that accompanied this code.
 *
 * Modifications:
 * If applicable, add the following below the License Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyright [year] [name of copyright owner]"
 *
 * Contributor(s):
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

package com.sun.enterprise.deployment.util.webservice;

/**
 * This class is used by the deployment tool to set the required information for jaxrpc-config.xml
 * that is passed as an argument to the wscompile tool. This class is to be used when the developer is
 * using the deploytool to generate WSDL given an SEI
 */

public class SEIConfig {

    private String webServiceName;
    private String nameSpace;
    private String packageName;
    private String interfaceName;
    private String servantName;

    /**
     * Constructor takes all required arguments and sets them appropriately
     * @param svcName Name of the webservice
     * @param space   namespace to be used for the webservice
     * @param pkg     the package name where the SEI and its implementations are present
     * @param svcIntf the name of the SEI 
     * @param svcImpl the name of SEI implementation
     */
    
    public SEIConfig(String svcName, String space, String pkg, String svcIntf, String svcImpl) {
        this.webServiceName = svcName;
        this.nameSpace = space;
        this.packageName = pkg;
        this.interfaceName = svcIntf;
        this.servantName = svcImpl;
    }
    
    public String getWebServiceName() { return this.webServiceName; }
    
    public String getNameSpace() { return this.nameSpace; }
    
    public String getPackageName() { return this.packageName; }
    
    public String getInterface() { return this.interfaceName; }
    
    public String getServant() { return this.servantName; }
}
