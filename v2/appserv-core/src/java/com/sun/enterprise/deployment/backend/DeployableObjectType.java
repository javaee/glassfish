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
 * DeployableObjectType.java
 *
 * Created on December 11, 2001, 10:05 AM
 */

package com.sun.enterprise.deployment.backend;

import javax.enterprise.deploy.shared.ModuleType;
import com.sun.enterprise.deployment.util.XModuleType;
import java.io.*;
import java.util.*;
import java.util.jar.*;
import java.util.jar.Manifest;
import java.util.jar.Attributes;
import java.util.jar.Attributes.Name;
import java.util.zip.ZipEntry;
import com.sun.enterprise.util.io.FileUtils;
import com.sun.enterprise.util.i18n.StringManager;
import com.sun.enterprise.deployment.util.DeploymentProperties;

/**
 *
 * @author  bnevins
 * @version 
 */

public class DeployableObjectType 
{
	public boolean isEJB()
	{
		return this == EJB;
	}
	
	///////////////////////////////////////////////////////////////////////////
	
	public boolean isWEB()
	{
		return this == WEB;
	}
	
	///////////////////////////////////////////////////////////////////////////
	
	public boolean isAPP()
	{
		return this == APP;
	}
	
	///////////////////////////////////////////////////////////////////////////
	
	public boolean isCONN()
	{
		return this == CONN;
	}

	///////////////////////////////////////////////////////////////////////////	

	public boolean isCAR()
	{
		return this == CAR;
	}	
	
	public boolean isLCM()
	{
		return this == LCM;
	}	
	
	public boolean isCMB()
	{
		return this == CMB;
	}	
	
	///////////////////////////////////////////////////////////////////////////	
	
	public String toString()
	{
		return name;
	}	
	
	///////////////////////////////////////////////////////////////////////////	
	
	public ModuleType getModuleType()
	{
		return jsr88Type;
	}
	
	///////////////////////////////////////////////////////////////////////////	

    private DeployableObjectType(String theName, 
        String theDDName, String theRuntimeDD, String ext, ModuleType type) {

        name        = theName;
        ddName      = theDDName;
        runtimeDD   = theRuntimeDD;
        extension   = ext;
        jsr88Type   = type;
        allTypes.add(this);
   }
	
	///////////////////////////////////////////////////////////////////////////

	public	static final	DeployableObjectType	APP;
	public	static final	DeployableObjectType	EJB;
	public	static final	DeployableObjectType	WEB;
	public	static final	DeployableObjectType	CONN;
	public  static final    DeployableObjectType    CAR;
	public  static final    DeployableObjectType    LCM;
	public  static final    DeployableObjectType    CMB;
	private final			String					name;
	private final			String					ddName;
    private final           String                  runtimeDD;
    private final           String                  extension;
	private final			ModuleType				jsr88Type;
    private static			StringManager			localStrings = StringManager.getManager(DeployableObjectType.class);
	private static			List					allTypes;
	
    static {
        allTypes = new ArrayList(7);
        APP  = new DeployableObjectType("Application",
                                        "META-INF/application.xml", 
                                        "META-INF/sun-application.xml",
                                        ".ear", ModuleType.EAR);
 
         WEB  = new DeployableObjectType("Web Module",
                                         "WEB-INF/web.xml",
                                         "WEB-INF/sun-web.xml",
                                         ".war", ModuleType.WAR);
 
         CONN = new DeployableObjectType("Connector Module",   
                                         "META-INF/ra.xml",
                                         "META-INF/sun-ra.xml",
                                         ".rar", ModuleType.RAR);
 
         CAR  = new DeployableObjectType("AppClient Module",   
                                         "META-INF/application-client.xml",  
                                         "META-INF/sun-application-client.xml",
                                         ".jar", ModuleType.CAR);

         EJB  = new DeployableObjectType("EJB Module",
                                         "META-INF/ejb-jar.xml",
                                         "META-INF/sun-ejb-jar.xml",
                                         ".jar", ModuleType.EJB);
 
         LCM  = new DeployableObjectType("Lifecycle Module",
                                         null,
                                         null,
                                         ".jar", XModuleType.LCM);
 
         CMB  = new DeployableObjectType("Custom MBean Module",
                                         null,
                                         null,
                                         ".jar", XModuleType.CMB);
 
   }
    
	
	///////////////////////////////////////////////////////////////////////////

	public static void main(String[] args)
	{
		String[] files = new String[] { "pkgingEApp.ear", "foof.rar", "hello.war", "foo.jar", "nothere", "junk.ear",
			"ear", "ejb", "rar", "war"};
		
		for(int i = 0; i < files.length; i++)
		{
			File f = new File("C:/ias8samples/" + files[i]);
			
			try 
			{
				// used to display (f.getName() + " is a: " + valueOf(f));
			} 
			catch (Exception ex) 
			{
				ex.printStackTrace();
			}	
		}
	}
}
