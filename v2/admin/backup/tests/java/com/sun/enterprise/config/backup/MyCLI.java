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
 * MyCLI.java
 *
 * Created on February 5, 2004, 3:54 PM
 */

package com.sun.enterprise.config.backup;
import java.util.*;
import java.net.*;
import com.sun.enterprise.cli.framework.CLIMain;
import com.sun.enterprise.cli.framework.InputsAndOutputs;

/**
 *
 * @author  Byron Nevins
 */
public class MyCLI
{

	/** Creates a new instance of MyCLI */
	public MyCLI()
	{
	}

	/**
	 * @param args the command line arguments
	 */
	public static void main(String[] args)
	{
		try
		{
			String drive = "C:";
			String tempDirDomains = drive + "/tmp/domains";
			String iasroot = drive + "/ee";
			System.setProperty("com.sun.aas.instanceRoot", iasroot);
			System.setProperty("com.sun.aas.domainsRoot", iasroot + "/domains");
			System.setProperty("com.sun.aas.instanceName", "server");
			System.setProperty("java.library.path", iasroot + "/bin");
			System.setProperty("com.sun.aas.configRoot", iasroot + "/config" );
			System.setProperty("java.endorsed.dirs", iasroot + "/lib/endorsed");
			//System.setProperty("com.sun.appserv.admin.pluggable.features=com.sun.enterprise.ee.admin.pluggable.EEClientPluggableFeatureImpl

			MyCLI cli = new MyCLI();
			Enumeration urls = MyCLI.class.getClassLoader().getResources("CLIDescriptor.xml");

			if ((urls == null) || (!urls.hasMoreElements()))
			{
				System.out.println("No URLS");
			}
			else
				System.out.println(urls);

			while (urls.hasMoreElements())
			{
				URL url = (URL) urls.nextElement();
				System.out.println(url);
			}

			//String cmdline = "restore-domain --domaindir " + tempDirDomains
			//	 + " domain1 ";
			//String cmdline = "restore-domain --domaindir " + tempDirDomains
				//+ " --filename C:/tmp/domains/domain1/backups/1080108915404.zip " + " domain23 ";
			//String cmdline = "backup-domain --domaindir " + tempDirDomains + " domain1";

			//String cmdline = "list-backups domain1";
			String cmdline = "backup-domain domain1 --verbose --description foobarski";
			//String cmdline = "restore-domain domain1";


			System.out.println(cmdline);
			CLIMain.invokeCLI(cmdline, InputsAndOutputs.getInstance());
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
	}

}
/*
 *
 -Dcom.sun.aas.instanceRoot=$AS_INSTALL
-Dcom.sun.aas.instanceName=server
-Djava.library.path="d:\ias8ee\bin";"d:\ias8ee\bin"
-Dcom.sun.aas.configRoot="d:\ias8ee\config"
-Djava.endorsed.dirs="d:\ias8ee\lib\endorsed"
-Dcom.sun.appserv.admin.pluggable.features=com.sun.enterprise.ee.admin.pluggable.EEClientPluggableFeatureImpl



-cp "d:\ias8ee\lib";"d:\ias8ee\lib\appserv-rt.jar";"d:\ias8ee\lib\appserv-ext.jar";"d:\ias8ee\lib\j2ee.jar";"d:\ias8ee\lib\admin-cli.jar";"d:\ias8ee\lib\appserv-ad
min.jar";"d:\ias8ee\lib\commons-launcher.jar";"d:\ias8ee\lib\ant\lib\ant.jar";"d:\ias8ee\lib\ant\lib\optional.jar";"d:\ias8ee\imq\
lib\imqadmin.jar";"d:\ias8ee\lib\appserv-se.jar"


com.sun.enterprise.cli.framework.CLIMain
 **/