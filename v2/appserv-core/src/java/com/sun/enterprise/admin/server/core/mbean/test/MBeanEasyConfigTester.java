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

public class MBeanEasyConfigTester
{

	/** 
		 Creates new MBeanInfoTester
	*/
    
	public MBeanEasyConfigTester()
	{
  }

    /**
		@param args the command line arguments
    */
    
	public static void main (String args[])
	{
		new MBeanEasyConfigTester().test();
  }

	public void test()
	{
		try
		{
			printInfo((new EasyConfigTestMBean()).getMBeanInfo());
		}
		catch(Exception e)
		{
			
			e.printStackTrace();
		}
	}
	private void printInfo(MBeanInfo info)
	{
	   print("************** MBeanInfo ******************");
	   print("ClassName="+info.getClassName());
	   print("Description="+info.getDescription());
	   printInfo(info.getAttributes());
	   printInfo(info.getConstructors());
	   printInfo(info.getNotifications());
	   printInfo(info.getOperations());
	}

	//*****************************************************************************************
	private void printInfo(MBeanAttributeInfo info)
	{
	  print("   ************** MBeanAttributeInfo ******************");
	  print("   name="+info.getName());
	  print("   description="+info.getDescription());
	  print("   type="+info.getType());
	  print("   isReadable="+info.isReadable());
	  print("   isWritable="+info.isWritable());
	  print("   isIs="+info.isIs());
	}

	//*****************************************************************************************
	private void printInfo(MBeanConstructorInfo info)
	{
	  print("   ************** MBeanConstructorInfo ******************");
	  print("   name="+info.getName());
	  print("   description="+info.getDescription());
	  printInfo(info.getSignature());
	}

	//*****************************************************************************************
	private void printInfo(MBeanNotificationInfo info)
	{
	  print("   ************** MBeanNotificationInfo ******************");
	  print("   name="+info.getName());
	  print("   description="+info.getDescription());
	  printInfo(info.getNotifTypes());
	}
	//*****************************************************************************************
	private void printInfo(MBeanOperationInfo info)
	{
	  print("   ************** MBeanOperationInfo ******************");
	  print("   name="+info.getName());
	  print("   description="+info.getDescription());
	  String str = "???";
	  switch(info.getImpact())
	    {
	      case MBeanOperationInfo.UNKNOWN:
	         str = "UNKNOWN";
	         break;
	      case MBeanOperationInfo.ACTION:
	         str = "ACTION";
	         break;
	      case MBeanOperationInfo.INFO:
	         str = "INFO";
	         break;
	      case MBeanOperationInfo.ACTION_INFO:
	         str = "ACTION_INFO";
	         break;
	    }
	  print("   returnType="+info.getReturnType());
    print("   impact="+str);
	  printInfo(info.getSignature());
	}
	//*****************************************************************************************
	private void printInfo(MBeanParameterInfo info)
	{
	  print("      ************** MBeanParameterInfo ******************");
	  print("      name="+info.getName());
	  print("      description="+info.getDescription());
	  print("      type="+info.getType());
	}


	//*****************************************************************************************
	private void printInfo(Object[] infos)
	{
	  for(int i=0; i<infos.length; i++)
	    if(infos[i] instanceof MBeanAttributeInfo)
        printInfo((MBeanAttributeInfo)infos[i]);   
	    else 
	      if(infos[i] instanceof MBeanConstructorInfo)
	        printInfo((MBeanConstructorInfo)infos[i]);   
	      else 
	        if(infos[i] instanceof MBeanNotificationInfo)
	          printInfo((MBeanNotificationInfo)infos[i]);   
	        else 
	          if(infos[i] instanceof MBeanOperationInfo)
	            printInfo((MBeanOperationInfo)infos[i]);   
	          else 
              if(infos[i] instanceof MBeanParameterInfo)
                printInfo((MBeanParameterInfo)infos[i]);
              else
                print("      "+infos[i]); //notif types?
	}
  
	//*****************************************************************************************
  private void print(String str)
  {
    System.out.println(str);
  }
}