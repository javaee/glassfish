/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2018 Oracle and/or its affiliates. All rights reserved.
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

package test;

import com.sun.ejte.ccl.reporter.SimpleReporterAdapter;
import java.util.*;
import java.io.IOException;
import javax.management.JMException;
import com.sun.enterprise.util.*;

/*
 * DeployManyMBeans.java
 *
 * Created on December 12, 2005, 10:22 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

/**
 *
 * @author bnevins
 */
public class DeployManyMBeans extends LifeCycle
{
    public DeployManyMBeans()
    {
        rnd = new Random();
        int len = 4 + rnd.nextInt(7);
        char[] chars = new char[len];
        
        for(int i = 0; i < len; i++)
        {
            chars[i] = alphabet.charAt(rnd.nextInt(26));
        }
        namePrefix = new String(chars);
        numIter = LocalStrings.getInt("DeployManyMBeans.NUM_BEANS", numIter);
        System.out.println(LocalStrings.get("DeployManyMBeans.NUM_BEANS"));
        System.out.println(LocalStrings.get("foo"));
    }
    
    String testInternal() throws JMException, IOException
    {
        title("DeployManyMBeans");
        title("Stage I -- create " + numIter + " MBeans");
        
        for(int i = 0; i < numIter; i++)
        {
            long msec = System.currentTimeMillis();
            String name = namePrefix + i;
            String classname = classnames[rnd.nextInt(4)];
            String objname = "user:foo=" + name;
            create(classname, objname, name);
            msec = System.currentTimeMillis() - msec;
            System.out.println("Created CMB, name: " + name + ", impl class: " 
                    + classname + ", obj-name: " + objname + ", Time(msec): " 
                    + msec + ", Memory Usage: " + getMemoryInfo());
        }            

        title("Stage II -- check " + numIter + " MBeans");
        
        List<String> list = list();
        for(int i = 0; i < numIter; i++)
        {
            String name = namePrefix + i;
            if(!list.contains(name))
                throw new RuntimeException("Could not find " + name + " in list of deployed MBeans");
            System.out.println("Found: " + name);
        }
        title("All MBeans were registered OK");

        title("Stage III -- delete " + numIter + " MBeans");
        
        if(interactive)
            Console.readLine("Shall I delete all of the MBeans? [y]: ");
        
        for(int i = 0; i < numIter; i++)
        {
            String name = namePrefix + i;
            delete(name);
            System.out.println("Deleted: " + name);
        }
        
        title("Stage IV  -- check deletion of " + numIter + " MBeans");
        
        list = list();
        for(int i = 0; i < numIter; i++)
        {
            String name = namePrefix + i;
            if(list.contains(name))
                throw new RuntimeException(name + " is still in the list of deployed MBeans");
            System.out.println("Verified Deletion of " + name);
        }

        title("All Done!");
        
        return ( SimpleReporterAdapter.PASS );
    }

    private static final String[] classnames = {
            "testmbeans.OneClassDynamicMBean", 
            "testmbeans.MicrowaveOvenImpl", 
            "testmbeans.SimpleStandard", 
            "testmbeans.PrimitiveStandard", 
    };
    
    private int numIter = 500;
    private String namePrefix;
    private String alphabet = "abcdefghijklmnopqrstuvwxyz";
    private Random rnd;
    private boolean interactive = false;
}
