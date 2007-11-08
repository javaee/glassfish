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
 *   $Id: MBeanRegistryFactory.java,v 1.10 2007/04/03 01:13:40 llc Exp $
 *   @author: alexkrav
 *
 *   $Log: MBeanRegistryFactory.java,v $
 *   Revision 1.10  2007/04/03 01:13:40  llc
 *   Issue number:  2752
 *   Obtained from:
 *   Submitted by:  Lloyd Chambers
 *   Reviewed by:   3 day timeout expired
 *
 *   Revision 1.9  2007/03/29 06:08:38  pa100654
 *   Issue number: 2583
 *   Reviewed by:   Kedar Mhaswade
 *   Added a check to see if adminContext is null.
 *
 *   Revision 1.8  2007/03/05 19:25:42  pa100654
 *   Issue number:  6506926
 *   Reviewed by:   Sreeni Munnangi
 *   The admin related mbeans are loaded during the startup of instance in the mbeanserver of the instance which shouldn't be done. Provided a check to do this only when the server is DAS.
 *   Before fix - number of mbeans in the instance were 497
 *   After fix - number of mbeans in the instance are 168
 *
 *   Revision 1.7  2007/02/02 17:42:04  llc
 *   Issue number:  2311
 *   Obtained from:
 *   Submitted by:  Lloyd Chambers
 *   Reviewed by:
 *
 *   Revision 1.6  2007/01/18 01:38:04  pa100654
 *   Issue number:  Backing out the changes due to build failure.
 *   Reviewed by:   Self
 *   Description:   This file depends on classes in appserv-core which causes the build to break.
 *
 *   Revision 1.5  2007/01/18 00:14:20  pa100654
 *   Issue number:  6506926,6506932
 *   Reviewed by:   Kedar Mhaswade
 *   Description: The validator and the config beans are loaded for all the instances which is not necessary. Modified the code to not load them.
 *
 *   Revision 1.4  2006/05/08 17:18:53  kravtch
 *   Bug #6423082 (request for admin infrastructure to support the config changes without DAS running (offline))
 *   Added infrastructure for offline execution under Config Validator for:
 *      - dottednames set/get operation
 *      - Add/remove jvm-options
 *   Submitted by: kravtch
 *   Reviewed by: Kedar
 *   Affected modules: admin-core/admin; admin/validator;
 *
 *   Revision 1.3  2005/12/25 03:47:38  tcfujii
 *   Updated copyright text and year.
 *
 *   Revision 1.2  2005/06/27 21:19:44  tcfujii
 *   Issue number: CDDL header updates.
 *
 *   Revision 1.1.1.1  2005/05/27 22:52:02  dpatil
 *   GlassFish first drop
 *
 *   Revision 1.10  2004/11/14 07:04:21  tcfujii
 *   Updated copyright text and/or year.
 *
 *   Revision 1.9  2004/05/22 00:35:07  kravtch
 *   "system-properties" backend support is added
 *   Reviewer: Sridatta
 *   Tests passed: QLT/CTS PE
 *
 *   Revision 1.8  2004/03/02 18:26:33  kravtch
 *   MBean's Descriptor field ElementChangeEvent support added (Constant, get method).
 *   MBeanRegistryFactory.setAdminMBeanRegistry() added for tester
 *
 *   Revision 1.7  2004/02/20 03:56:14  qouyang
 *
 *
 *   First pass at code merge.
 *
 *   Details for the merge will be published at:
 *   http://javaweb.sfbay.sun.com/~qouyang/workspace/PE8FCSMerge/02202004/
 *
 *   Revision 1.6.4.1  2004/02/02 07:25:19  tcfujii
 *   Copyright updates notices; reviewer: Tony Ng
 *
 *   Revision 1.6  2003/09/04 05:53:49  kravtch
 *   bugs #4896268 and #4913653
 *   Reviewer: Sridatta
 *      -AuthRealmMbean's getFielRealm is chaged from creating of the new FileRealm object to gettting it from security pool - Realm.getInstance(name) with casting result to FileRealm.
 *   This approach will work only for PE because DAS and instance have the same auth-realms.
 *      -AdminContext expanded by two new methods getAdminMBeanResourcUrl() and getRuntimeMBeanResourcUrl() which used by MBeanRegistryFactory for initialization admin and runtime registries. So, they are become pluggable.
 *      -AdminContext also notifies MBeanRegistryFactory during its construction. So, AdminContext become "visible" to admin-core/admin classes.
 *      -Hardcoded output changed to appropriate logger calls.
 *
 *   Revision 1.5  2003/07/29 18:59:35  kravtch
 *   MBeanRegistryEntry:
 *   	- support for toFormatString();
 *   	- instantiateMBean() method modified to instantiate runtime MBeans as well;
 *   MBeanRegistryFactory:
 *   	- fixed bug in getRuntimeRegistry();
 *   MBeanNamingInfo:
 *   	- less strict requirements for parm_list_array size in constructor (can be more then needed);
 *   BaseRuntimeMBean:
 *   	- exception ClassCastException("Managed resource is not a Jsr77ModelBean") handling;
 *   ManagedJsr77MdlBean:
 *   	- call managed bean bug fixed ( getDeclaredMethod()->getMethod())
 *   admin/dtds/runtime-mbeans-descriptors.xml - modified to represent new runtime mbeans;
 *
 *   Revision 1.4  2003/07/18 20:14:44  kravtch
 *   1. ALL config mbeans are now covered by descriptors.xml
 *   2. new infrastructure for runtime mbeans is added
 *   3. generic constructors added to jsr77Mdl beans (String[])
 *   4. new test cases are added to admintest
 *   5. MBeanRegistryFactory has now different methods to obtain admin/runtime registries
 *   6. runtime-descriptors xml-file is added to build
 *
 *   Revision 1.3  2003/06/25 20:03:40  kravtch
 *   1. java file headers modified
 *   2. properties handling api is added
 *   3. fixed bug for xpathes containing special symbols;
 *   4. new testcases added for jdbc-resource
 *   5. introspector modified by not including base classes operations;
 *
 *
*/

package com.sun.enterprise.admin.meta;

import java.io.InputStream;
import java.io.FileInputStream;
import java.io.PrintWriter;
import java.io.BufferedWriter;
import java.io.FileWriter;import java.net.URL;

import java.util.logging.Level;
import java.util.logging.Logger;
import com.sun.logging.LogDomains;

import com.sun.enterprise.admin.meta.naming.MBeansNaming;
import com.sun.enterprise.admin.AdminContext;

import com.sun.enterprise.config.serverbeans.ServerHelper;
import com.sun.enterprise.config.ConfigContext;

/**
 * Provides naming support for Mbeans
 */
public class MBeanRegistryFactory
{
    private static MBeanRegistry adminRegistry = null;
    private static MBeanRegistry runtimeRegistry = null;
    private static volatile AdminContext  adminContext = null;

    static protected final Logger _sLogger = LogDomains.getLogger(LogDomains.ADMIN_LOGGER);
    
    static public void setAdminContext(AdminContext ctx)
    {
        adminContext = ctx;
    }
    
    static public AdminContext getAdminContext()
    {
        return adminContext;
    }
    
    static public synchronized void setAdminMBeanRegistry(MBeanRegistry registry)
    { //for tester purposes
        adminRegistry = registry;
    }
    
    static synchronized public MBeanRegistry getAdminMBeanRegistry()
    {
        if(adminRegistry != null)
            return adminRegistry;
        try {
            //Dont need to load the static MBeans for non DAS instances
            if ((adminContext != null) && 
                    (!ServerHelper.isDAS(adminContext.getRuntimeConfigContext(), 
                                            adminContext.getServerName())))
            {
	        //Return an empty MBeanRegistry
                adminRegistry = new MBeanRegistry();
                return adminRegistry;
            }
            URL url = null;
            if(adminContext!=null)
                url = adminContext.getAdminMBeanRegistryURL();
            if(url==null)
               url = MBeanRegistry.class.getResource("/admin-mbeans-descriptors.xml"); //standard for pe
            InputStream stream = url.openStream();
            adminRegistry = new MBeanRegistry();
            adminRegistry.loadMBeanRegistry(stream);
            stream.close();
            //printMBeanREgistry
            try {
                String fileName = System.getProperty("adminmbeanregistry.printfile");
                if(fileName!=null)
                {
                    PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter(fileName)));
                    out.println("MBean REGISTRY (in XPath value order)");
                    adminRegistry.sortRegistryEntries(MBeanRegistry.SORT_BY_XPATH);
                    out.print(adminRegistry.toFormatString());
                    out.println("\n\n\nMBean REGISTRY (in Type value order)");
                    adminRegistry.sortRegistryEntries(MBeanRegistry.SORT_BY_NAME);
                    out.print(adminRegistry.toFormatString());
                    out.close();
                }

            }
            catch (Throwable t)
            {}
             return adminRegistry;
        } catch (Throwable t) {
            _sLogger.log(Level.WARNING, "core.registryfactory_adminregistry_not_found",
                       t);
        }
        return null;
    }

    static synchronized public MBeanRegistry getRuntimeMBeanRegistry()
    {
        if(runtimeRegistry != null)
            return runtimeRegistry;
        try {
            URL url = null;
            if(adminContext!=null)
                url = adminContext.getRuntimeMBeanRegistryURL();
            if(url==null)
                url = MBeanRegistry.class.getResource("/runtime-mbeans-descriptors.xml");  //standard for pe
            InputStream stream = url.openStream();
            runtimeRegistry = new MBeanRegistry();
            runtimeRegistry.loadMBeanRegistry(stream);
            stream.close();
            return runtimeRegistry;
        } catch (Throwable t) {
            _sLogger.log(Level.WARNING, "core.registryfactory_adminregistry_not_found",
                       t);
        }
        return null;
    }
    //for testing purposes only
    static public MBeanRegistry getMBeanRegistry(String fileName)
    {   
        return getMBeanRegistry(fileName, true);
    }
    //for testing purposes only
    static synchronized public MBeanRegistry getMBeanRegistry(String fileName, boolean bIntrospectMBeans)
    {
        try {
            FileInputStream stream = new FileInputStream(fileName);
            MBeanRegistry myRegistry = new MBeanRegistry();
            myRegistry.loadMBeanRegistry((InputStream)stream, bIntrospectMBeans);
            stream.close();
            return myRegistry;
        } catch (Throwable t) {
            _sLogger.log(Level.WARNING, "core.registryfactory_registry_not_found",
                       t);
        }
        return null;
    }

    static public synchronized MBeanRegistry getOfflineAdminMBeanRegistry() throws Exception
    {
        MBeanRegistry myRegistry;
        URL url = null;
        url = MBeanRegistry.class.getResource("/admin-mbeans-descriptors-ee.xml"); //standard for ee
        InputStream stream = null;
        try {
        stream = url.openStream();
        } catch (Exception e)
        {
        }
        if (stream==null)  
        {
           url = MBeanRegistry.class.getResource("/admin-mbeans-descriptors.xml"); //standard for pe
           stream = url.openStream();
        }
        if (stream==null)  
            return null;
        myRegistry = new MBeanRegistry();
        myRegistry.loadMBeanRegistry(stream, false);
        stream.close();
        return myRegistry;
    }
    
}
