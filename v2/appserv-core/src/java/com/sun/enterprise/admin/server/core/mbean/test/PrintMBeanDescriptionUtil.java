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

package com.sun.enterprise.admin.server.core.mbean.test;

import javax.management.*;
//import com.sun.enterprise.admin.server.core.mbean.meta.*;
import com.sun.enterprise.admin.util.*;
import com.sun.enterprise.admin.server.core.mbean.config.*;
import com.sun.enterprise.admin.server.core.jmx.*;

public class PrintMBeanDescriptionUtil
{
    
    /**
     * Creates new MBeanInfoTester
     */
    
    public PrintMBeanDescriptionUtil()
    {
    }
    
    /**
     * @param args the command line arguments
     */
    
    public static void main(String args[])
    {
        new PrintMBeanDescriptionUtil().test(args);
    }
    
    private void printPropertyEntries(DynamicMBean mbean)
    {
        print("\n#**************************************************************");
        String name = mbean.getClass().getName();
        name = name.substring(name.lastIndexOf('.')+1);
        print("# Text Descriptions for MBean: "+name);
        print("#**************************************************************");
        printDescr(mbean.getMBeanInfo());
    }
    public void test(String args[])
    {
        try
        {
            MBeanServer	mbs	= SunoneInterceptor.getMBeanServerInstance();
            printPropertyEntries(new GenericConfigurator());
//            printPropertyEntries(new JCAAdminHandler());
            printPropertyEntries(new JMSAdminHandler("ias1"));
            printPropertyEntries(new ManagedCustomResource("ias1","any"));
            printPropertyEntries(new ManagedEjbContainer("ias1"));
//            printPropertyEntries(new ManagedHTTPConnectionGroup("ias1","any","any"));
            printPropertyEntries(new ManagedHTTPListener("ias1","any"));
            printPropertyEntries(new ManagedHTTPService("ias1"));
            printPropertyEntries(new ManagedJDBCConnectionPool("ias1","any"));
            printPropertyEntries(new ManagedJDBCResource("ias1","any"));
            printPropertyEntries(new ManagedJMSResource("ias1","any"));
            printPropertyEntries(new ManagedJNDIResource("ias1","any"));
            printPropertyEntries(new ManagedJVM("ias1"));
            printPropertyEntries(new ManagedJavaMailResource("ias1","any"));
            printPropertyEntries(new ManagedLogService("ias1"));
            printPropertyEntries(new ManagedMdbContainer("ias1"));
            printPropertyEntries(new ManagedORBComponent("ias1"));
            printPropertyEntries(new ManagedORBListener("ias1","any"));
            printPropertyEntries(new ManagedPMFactoryResource("ias1","any"));
            printPropertyEntries(new ManagedSecurityService("ias1"));
            printPropertyEntries(new ManagedServerInstance("ias1",new HostAndPort("any",1), false));
            printPropertyEntries(new ManagedStandaloneConnectorModule("ias1","any"));
            printPropertyEntries(new ManagedStandaloneJ2EEEjbJarModule("ias1","any"));
            printPropertyEntries(new ManagedStandaloneJ2EEWebModule("ias1","any"));
            printPropertyEntries(new ManagedTransactionService("ias1"));
            printPropertyEntries(new ManagedWebContainer("ias1"));
            printPropertyEntries(new ServerController());
        }
        catch(Throwable e)
        {
            print(e.getMessage());
            e.printStackTrace();
        }
    }

    private void printDescr(MBeanInfo info)
    {
        printDescr(info.getDescription());
        printDescr(info.getAttributes());
        printDescr(info.getConstructors());
        printDescr(info.getNotifications());
        printDescr(info.getOperations());
    }
    
    //*****************************************************************************************
    private void printDescr(MBeanAttributeInfo info)
    {
        printDescr(info.getDescription());
    }
    
    //*****************************************************************************************
    private void printDescr(MBeanConstructorInfo info)
    {
        printDescr(info.getDescription());
        printDescr(info.getSignature());
    }
    
    //*****************************************************************************************
    private void printDescr(MBeanNotificationInfo info)
    {
        printDescr(info.getDescription());
        printDescr(info.getNotifTypes());
    }
    //*****************************************************************************************
    private void printDescr(MBeanOperationInfo info)
    {
        printDescr(info.getDescription());
        printDescr(info.getSignature());
    }
    //*****************************************************************************************
    private void printDescr(MBeanParameterInfo info)
    {
        printDescr(info.getDescription());
    }
    
    //*****************************************************************************************
    private void printDescr(String descr)
    {
        if(descr!=null && descr.length()>0)
            print(descr+" = "+descr);
    }
    
    //*****************************************************************************************
    private void printDescr(Object[] infos)
    {
        for(int i=0; i<infos.length; i++)
            if(infos[i] instanceof MBeanAttributeInfo)
                printDescr((MBeanAttributeInfo)infos[i]);
            else
                if(infos[i] instanceof MBeanConstructorInfo)
                    printDescr((MBeanConstructorInfo)infos[i]);
                else
                    if(infos[i] instanceof MBeanNotificationInfo)
                        printDescr((MBeanNotificationInfo)infos[i]);
                    else
                        if(infos[i] instanceof MBeanOperationInfo)
                            printDescr((MBeanOperationInfo)infos[i]);
                        else
                            if(infos[i] instanceof MBeanParameterInfo)
                                printDescr((MBeanParameterInfo)infos[i]);
    }
    
    //*****************************************************************************************
    private void print(String str)
    {
        System.out.println(str);
    }
}
