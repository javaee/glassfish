/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2003-2018 Oracle and/or its affiliates. All rights reserved.
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

/*
 * $Id: OfflineConfigTest.java,v 1.1.1.1 2005/05/28 00:39:19 dpatil Exp $
 */
package com.sun.enterprise.admin.config;

import java.lang.reflect.*;
import java.util.*;
import java.io.*;

//junit imports
import junit.framework.*;
import junit.textui.TestRunner;

//JMX
import javax.management.DynamicMBean;
import javax.management.Attribute;
import javax.management.AttributeList;
import javax.management.ObjectName;
import javax.management.AttributeNotFoundException;

//config imports
import com.sun.enterprise.config.ConfigFactory;
import com.sun.enterprise.config.ConfigContext;
import com.sun.enterprise.admin.config.OfflineConfigMgr;

public class OfflineCommander
{
    static String TEST_DIR = "/tmp/asadmintest";

    private OfflineConfigMgr _mgr; // = new AdminTester(_mode, _registry, _configContext);
    
    public OfflineCommander() throws Exception
    {
        _mgr = new OfflineConfigMgr(TEST_DIR+"/domain.xml");
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////////
    public static void main(String args[]) throws Exception
    {
        int length = args.length;
        if(length<2 ||
           !args[0].equals("-testpath"))
        {
            System.out.println("usage: offlineconfigtest -testpath <path to test directory> [print|create]");
            System.exit(1);
        }
        TEST_DIR = args[1]; 
        OfflineCommander commander = new OfflineCommander();
        System.out.print(">");
        String cmd = "";
        int iNext;
        ArrayList list;
        AttributeList attrs;
        while ((iNext = System.in.read())>0)
        {
            char ch = (char)iNext;
            if(ch==13 || ch==10)
            {//new command
               if(cmd.equals("q") || cmd.equals("Q"))
                   break;
               
               try {
                   if(cmd.startsWith("list ") || cmd.equals("list"))
                   {
                       //LIST COMMAND
                       String mask = cmd.equals("list")?"":cmd.substring(4).trim();
                       list = commander._mgr.getListDottedNames(mask);
                       printList("   ", list);
                   }
                   else if(cmd.startsWith("get "))
                   {
                       //GET COMMAND
                       attrs = commander._mgr.getAttributes(cmd.substring(3).trim());
                       printAttributes("   ", attrs);
                   }
                   else if(cmd.startsWith("set "))
                   {
                       //SET COMMAND
                       int eqIdx = cmd.indexOf("=");
                       if(eqIdx<0)
                       {
                           System.out.println("ERROR!");
                           continue;
                       }
                       attrs = commander._mgr.setAttribute(
                               cmd.substring(4, eqIdx).trim(),
                               cmd.substring(eqIdx+1).trim());
                       printAttributes("   ", attrs);
                   }
                   else if(cmd.startsWith("add "))
                   {
                       //add COMMAND
                       String rest = cmd.substring(4).trim();
                       int spaceIdx = rest.indexOf(" ");
                       if(spaceIdx<0)
                       {
                           System.out.println("ERROR!");
                           continue;
                       }
                       
                       attrs = commander._mgr.addSubvaluesToArrayAttribute(
                               rest.substring(0, spaceIdx).trim(),
                               rest.substring(spaceIdx).trim().split(","));
                       printAttributes("   ", attrs);
                   }
                   else if(cmd.startsWith("remove "))
                   {
                       //add COMMAND
                       String rest = cmd.substring(7).trim();
                       int spaceIdx = rest.indexOf(" ");
                       if(spaceIdx<0)
                       {
                           System.out.println("ERROR!");
                           continue;
                       }
                       
                       attrs = commander._mgr.removeSubvaluesFromArrayAttribute(
                               rest.substring(0, spaceIdx).trim(),
                               rest.substring(spaceIdx).trim().split(","));
                       printAttributes("   ", attrs);
                   }
               } catch (Exception e)
               {
                    System.out.println("\n***Exception: "+e.getMessage());
               }
               System.out.print("\n>");
               cmd="";
               continue;
            }
            //System.out.print(ch);
            cmd = cmd + ch;
        }
        System.out.println("\n finished");
    }

    

    
    //******************************************************************
    //******************HELPERS***************************************
    //******************************************************************
    static private void printAttributes(String title, AttributeList attrs)
    {
        if(title!=null)
            System.out.println(title);
        for(int i=0; i<attrs.size(); i++)
        {
            Attribute attr = (Attribute)attrs.get(i);
            if(attr.getValue() instanceof Object[])
            {
                System.out.println("         "+ 
                       attr.getName() + " = " );
                Object[] sub_attrs= (Object[])attr.getValue();
                for(int j=0; j<sub_attrs.length; j++)
                {
                    System.out.println("               "+ 
                        sub_attrs[j]);
                }
            }
            else
            {
                System.out.println("         "+ 
                       attr.getName() + " = " + attr.getValue());
            }
        }
    }
    static private void printList(String title, ArrayList list)
    {
        if(title!=null)
            System.out.println(title);
        for(int i=0; i<list.size(); i++)
        {
            System.out.println("         " + list.get(i));
        }
    }
}
