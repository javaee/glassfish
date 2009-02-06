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
 * DomainInfo.java
 *
 * Created on April 27, 2004, 3:46 PM
 */

package com.sun.enterprise.tools.upgrade.common;

/**
 *
 * @author  prakash
 */
import java.util.*;
import java.io.*;

import org.w3c.dom.NodeList;
import javax.xml.parsers.DocumentBuilder;
import org.w3c.dom.Element;
import org.w3c.dom.Document;
import javax.xml.parsers.DocumentBuilderFactory;

public class DomainInfo {
    
    private String domainName;
    private String domainPath;
    private String profile;    
    private List instanceNames;
    private Hashtable instancePathMapping;

    private String domainAdminUser;
    private String domainAdminPassword;

    
    /** Creates a new instance of DomainInfo */
    public DomainInfo(String dName,String dPath, String profile) {
        this.domainName = dName;
        this.domainPath = dPath;
        this.profile = profile;	
    }
    
    /** Creates a new instance of DomainInfo */
    public DomainInfo(String dName,String dPath) {
        this.domainName = dName;
        this.domainPath = dPath;
    }
    
    /** Getter for property domainName.
     * @return Value of property domainName.
     *
     */
    public java.lang.String getDomainName() {
        return domainName;
    }    
    
    /** Setter for property domainName.
     * @param domainName New value of property domainName.
     *
     */
    public void setDomainName(java.lang.String domainName) {
        this.domainName = domainName;
    }
    
    /** Getter for property domainPath.
     * @return Value of property domainPath.
     *
     */
    public java.lang.String getDomainPath() {
        return domainPath;
    }
    
    /** Setter for property domainPath.
     * @param domainPath New value of property domainPath.
     *
     */
    public void setDomainPath(java.lang.String domainPath) {
        this.domainPath = domainPath;
    }

    /** Getter for property profile.
     * @return Value of property profile.
     *
     */
    public java.lang.String getProfile() {
        return profile;
    }
 
    /** Setter for property profile.
     * @param profile New value of property profile.
     *
     */
    public void setProfile(java.lang.String profile) {
        this.profile = profile;
    }
    
    /** Getter for property instanceNames.
     * @return Value of property instanceNames.
     */
    public java.util.List getInstanceNames() {
        if(this.instanceNames == null){
            instanceNames = new ArrayList();
            instancePathMapping = new Hashtable();
            File domainDir = new File(this.domainPath);
            String [] instanceDirs = domainDir.list();
            for (int i=0 ; i<instanceDirs.length ; i++) {
                instanceNames.add(instanceDirs[i]);
                String instancePath= this.domainPath + File.separator + instanceDirs[i];
                instancePathMapping.put(instanceDirs[i],instancePath);            
            }      
        }
        return instanceNames;
    }
    
    public String getInstancePath(String instanceName) {
        if(this.instancePathMapping == null){
            this.getInstanceNames();
        }
        // if instance path is null or "" return the domain path itself.  In case of 8.x PE
        if(instanceName == null || "".equals(instanceName.trim())){
            return this.domainPath;
        }
        return (String)this.instancePathMapping.get(instanceName);
    }
   
    /**
     * Gets the application root of the current domain
     */
    public String getDomainApplicationRoot(UpgradeUtils upgrUtils) {
		String configFileName = domainPath + File.separator + "config" +
			File.separator + "domain.xml";
		String applRoot = null;
		Document adminServerDoc = upgrUtils.getDomainDocumentElement(configFileName);
		try {
			NodeList domainElements = adminServerDoc.getElementsByTagName("domain");
			//There is only one domain element
			Element domainElement = (Element)domainElements.item(0);
			if(domainElement != null) {
				String attrValue = domainElement.getAttribute("application-root");
				StringTokenizer attrTokens = new StringTokenizer(attrValue, "/");
				attrTokens.nextToken();
				applRoot = attrTokens.nextToken();
			}
		}catch (Exception ex){
		}
		return applRoot;
	}
    
    public String getDomainAdminUser() {
        return this.domainAdminUser;
    } 
 
    public void setDomainAdminUser(String dUser) {
        this.domainAdminUser = dUser;
    }

    public String getDomainAdminPassword() {
        return this.domainAdminPassword;
    }

    public void setDomainAdminPassword(String dPassword) {
        this.domainAdminPassword = dPassword;
    }
}
