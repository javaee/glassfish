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
package com.sun.ejb.codegen;

import java.io.File;
import com.sun.enterprise.deployment.EjbDescriptor;

/**
 * Helper class for ejbc generated class names.
 *
 * @author  Nazrul Islam
 * @since   JDK 1.4
 */
public class GeneratedNames {

    /**
     * Initializes the generated names based on the given 
     * deployment descriptor.
     * 
     * @param    desc    deployment descriptor
     */
    public GeneratedNames(EjbDescriptor desc) {
        // Set the file names for generated Home/Remote impl/tie/stubs

        // set EJBOBject Impl 
        ejbObjectImplClass = 
            desc.getEJBObjectImplClassName().replace('.', 
                                File.separatorChar) + ".class";
       
        // set EJBHome Impl 
        homeImplClass = 
            desc.getRemoteHomeImplClassName().replace('.', 
                                    File.separatorChar) + ".class";

        // Set EJBHome/EJBObject stub filenames
        homeStubClass = 
            getStubName(desc.getHomeClassName()).replace('.', 
                                    File.separatorChar) + ".class";
        remoteStubClass = 
            getStubName(desc.getRemoteClassName()).replace('.', 
                                    File.separatorChar) + ".class"; 
    }
     
    /**
     * Returns the stub classname for the given interface name
     *
     * @param    fullName    fully qualified name of the ejb home and obj impl
     */
    public static String getStubName(String fullName) {

        String className = fullName;
        String packageName = "";

        int lastDot = fullName.lastIndexOf('.');
        if (lastDot != -1) {
            className   = fullName.substring(lastDot+1, fullName.length());
            packageName = fullName.substring(0, lastDot+1);
        }

        String stubName = packageName + "_" + className + "_Stub";

		if(isSpecialPackage(fullName))
            stubName = ORG_OMG_STUB_PREFIX + stubName;

        return stubName;
    }

    public String getEjbObjectImplClass() {
        return ejbObjectImplClass;
    }

    public String getHomeImplClass() {
        return homeImplClass;
    }

    public String getHomeStubClass() {
        return homeStubClass;
    }

    public String getRemoteStubClass() {
        return remoteStubClass;
    }

	private static boolean isSpecialPackage(String name)
	{
		// these package names are magic.  RMIC puts any home/remote stubs
		// into a different directory in these cases.
		// 4845896  bnevins, April 2003
		
		// this is really an error.  But we have enough errors. Let's be forgiving
		// and not allow a NPE out of here...
		if(name == null)	
			return false;
		
		// Licensee bug 4959550 
		// if(name.startsWith("com.sun.") || name.startsWith("javax."))
		if(name.startsWith("javax."))
			return true;
		
		return false;
	}

	// ---- INSTANCE VARIABLES - PRIVATE ---------------------------------
    private String homeImplClass;  
    private String homeStubClass;
    private String remoteStubClass;
    private String ejbObjectImplClass;

    private static final String ORG_OMG_STUB_PREFIX  = "org.omg.stub.";
}
