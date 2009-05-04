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
 * DomainCredentials.java
 *
 *  An appserver domains' login credentials.
 *
 * Created on December 11, 2007, 4:20 PM
 *
 */

package com.sun.enterprise.tools.upgrade.common;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import com.sun.enterprise.util.i18n.StringManager;
import com.sun.enterprise.tools.upgrade.logging.LogService;

/**
 *
 * @author rebeccas
 */
public class DomainCredentials implements Credentials {
	private String adminUserName = null;
    private String adminPassword = null;
    private File passwordFile = null;
    private String masterPassword = CLIConstants.defaultMasterPassword; 
	private StringManager stringManager = 
            StringManager.getManager(DomainCredentials.class);
	
	/** Creates a new instance of DomainCredentials */
	public DomainCredentials() {
	}
	public DomainCredentials(String u, String passwd, String masterPasswd) {
		this(u, passwd);
		masterPassword = masterPasswd;
	}
	public DomainCredentials(String u, String passwd) {
		adminUserName = u;
		adminPassword = passwd;
	}
	public void setAdminUserName(String s){
		adminUserName = s;
	}
	public String getAdminUserName(){
		return adminUserName;
	}
	public void setAdminPassword(String s){
		adminPassword = s;
	}
	public String getAdminPassword(){
		return adminPassword;
	}
	public void setMasterPassword(String s){
		masterPassword = s;
	}
	public String getMasterPassword(){
		return masterPassword;
	}
	
	public String getPasswordFile(){
		if (passwordFile == null) {
			try {
				passwordFile = java.io.File.createTempFile("ugpw", null);
				FileWriter writer = new FileWriter(passwordFile);
                if (getAdminPassword() != null) {
                    writer.write("AS_ADMIN_PASSWORD=" + getAdminPassword() +"\n");
                    writer.write("AS_ADMIN_ADMINPASSWORD=" + getAdminPassword() +"\n");
                }
                if (getMasterPassword() != null){
                    writer.write("AS_ADMIN_MASTERPASSWORD=" + getMasterPassword() + "\n");
                }
				writer.close();
			} catch (IOException ioe) {
				CommonInfoModel.getDefaultLogger().severe(
					stringManager.getString("upgrade.common.general_exception") + " " + ioe.getMessage());
			}
		}
		return passwordFile.getAbsolutePath();
	}
	
	public void deletePasswordFile() {
        if (passwordFile != null) {
            passwordFile.delete();
        }
    }
}
