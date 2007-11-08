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

package com.sun.enterprise.admin.server.core.jmx.tester;

import java.io.*;
import javax.management.NotCompliantMBeanException;
import com.sun.enterprise.admin.util.Debug;
import com.sun.enterprise.admin.server.core.jmx.MBeanIntrospector;

public class MBeanIntrospectorTester
{
    public static void main(String[] args) throws Exception
    {
        BufferedReader reader = new BufferedReader(new FileReader(args[0]));
        String str;
        long begin = System.currentTimeMillis();
        while ((str = reader.readLine()) != null)
        {
            str = str.trim();
            if (isPrimitive(str))
            {
                try
                {
                    checkPrimitive(str);
                    Debug.println("Test failed : " + str);
                }
                catch (NotCompliantMBeanException e)
                {
                }
            }
            else
            {
                try
                {
                    checkNonPrimitive(str);
                }
                catch (NotCompliantMBeanException e)
                {
                    Debug.println("Not Compliant : " + str);
                }
            }
        }
        long end = System.currentTimeMillis();
        //Debug.println("Time lapse = " + (end - begin));
        reader.close();
    }

    private static boolean isPrimitive(String str)
    {
        return (str.equals("int")       ||
                str.equals("boolean")   ||
                str.equals("short")     ||
                str.equals("byte")      ||
                str.equals("char")      ||
                str.equals("float")     ||
                str.equals("long")      ||
                str.equals("double"));
    }

    private static void checkPrimitive(String str) 
        throws NotCompliantMBeanException
    {
        Class c = null;
        if (str.equals("int"))          { c = int.class;        }
        else if (str.equals("boolean")) { c = boolean.class;    }
        else if (str.equals("short"))   { c = short.class;      }
        else if (str.equals("byte"))    { c = byte.class;       }
        else if (str.equals("char"))    { c = char.class;       }
        else if (str.equals("float"))   { c = float.class;      }
        else if (str.equals("long"))    { c = long.class;       }
        else if (str.equals("double"))  { c = double.class;     }

        new MBeanIntrospector(c);
    }
    
    private static void checkNonPrimitive(String str) 
        throws ClassNotFoundException, NotCompliantMBeanException
    {
        Class c = Class.forName(str);
        MBeanIntrospector intr = new MBeanIntrospector(c);
        String msg = " Type = ";
        msg += intr.isStandardMBean() ? "isStandard" :
                        intr.isDynamicMBean() ? "isDynamic" : "Don't know";
        Debug.println(str + msg);
        Debug.println("Management interface = " + 
            intr.getMBeanInterfaceClass().getName());
    }
}