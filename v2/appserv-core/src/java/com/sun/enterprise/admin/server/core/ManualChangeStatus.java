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

package com.sun.enterprise.admin.server.core;

//Tomcat
import com.sun.enterprise.instance.InstanceEnvironment;

/**
    This class holds the result of Manual change check for 1 instance.
    It is a simple bean with getter and setter of all files that are used for checking
    Note: the list of files should be in sync with InstanceEnvironment.java
	@author  Sridatta
	@version 1.1
*/

public final class ManualChangeStatus {
    private boolean objectFileChanged = false;
    private boolean initFileChanged = false;
    private boolean realmsKeyFileChanged = false;
    private boolean serverXmlFileChanged = false;
    private boolean aclFileChanged = false;
    private boolean mimeFileChanged = false;
    private boolean virtualServerConfFilesChanged = false;
    
    public boolean isObjectFileChanged() {
        return objectFileChanged;
    }
    
    public boolean isInitFileChanged() {
        return initFileChanged;
    }
        
    public boolean isRealmsKeyFileChanged() {
        return realmsKeyFileChanged;
    }
            
    public boolean isServerXmlFileChanged() {
        return serverXmlFileChanged;
    }
                
    public boolean isAclFileChanged() {
        return aclFileChanged;
    }
    public boolean isMimeFileChanged() {
        return mimeFileChanged;
    }

    public boolean isVirtualServerConfFilesChanged() {
        return virtualServerConfFilesChanged;
    }

    public void setObjectFileChanged(boolean value) {
        objectFileChanged = value;
    }
    
    public void setInitFileChanged(boolean value) {
        initFileChanged = value;
    }
        
    public void setRealmsKeyFileChanged(boolean value) {
        realmsKeyFileChanged = value;
    }
            
    public void setServerXmlFileChanged(boolean value) {
        serverXmlFileChanged = value;
    }
                
    public void setAclFileChanged(boolean value) {
        aclFileChanged = value;
    }
    public void setMimeFileChanged(boolean value) {
        mimeFileChanged = value;
    }
    
    public void setVirtualServerConfFilesChanged(boolean value) {
        virtualServerConfFilesChanged = value;
    }

    public boolean isChanged() {
        /* TOMCAT_BEGIN Ramakanth */
        return (isServerXmlFileChanged() || 
            isRealmsKeyFileChanged());
        /* TOMCAT_END Ramakanth */
    }

    public String toString() {
        /* TOMCAT_BEGIN Ramakanth */
        return "ManualChangeStatus:\n" + 
            "\tserverXmlFileChanged = " + serverXmlFileChanged + "\n" + 
            "\trealmsKeyFileChanged = " + realmsKeyFileChanged + "\n";
        /* TOMCAT_END Ramakanth */
    }
    
}
