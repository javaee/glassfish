/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2008-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License.  You can
 * obtain a copy of the License at
 * https://glassfish.dev.java.net/public/CDDL+GPL_1_1.html
 * or packager/legal/LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at packager/legal/LICENSE.txt.
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

package net.munnangi.addon;

import org.jvnet.hk2.component.CageBuilder;
import org.jvnet.hk2.component.Inhabitant;
import org.jvnet.hk2.annotations.Service;
import javax.management.*;
import javax.management.modelmbean.*;
import java.lang.management.*;
import java.io.*;

@Service
public class RuntimeMBeanCageBuilder implements CageBuilder {

    public static int k = 0;

    public void onEntered(Inhabitant<?> i) {
        System.out.println("RuntimeMBeanCageBuilder: onEntered("+i.typeName()+"): time to create and register the mBean ...");

        Object o = i.get();
        if (o instanceof net.munnangi.addon.AddonCompanion) {
            System.out.println("instance of AddonCompanion ...");
            //System.out.println("classname = " + i.getClass().getName());
            System.out.println("getname = " + ((net.munnangi.addon.AddonCompanion)o).getName());
        } else if (o instanceof net.munnangi.addon.ConfigCompanion) {
            System.out.println("instance of ConfigCompanion ...");
            //System.out.println("classname = " + i.getClass().getName());
            System.out.println("getname = " + ((net.munnangi.addon.ConfigCompanion)o).getName());
        }

        if (k == 0) createMBean(o);
        k++;

    }



    private void createMBean(Object o) {
        System.out.println("creating mbean for o ..." + o);
        try {
            ObjectName mbon = new ObjectName("oem:name=companion");
            //Test1 t1 = new Test1();
            //System.out.println("ser res = " + t1.getClass().getResource("Test1.ser"));
            String className = o.getClass().getName();
            System.out.println("class name = " + className);
            System.out.println("ser res = " + o.getClass().getResource("AddonCompanion"+".ser"));
            InputStream fis = o.getClass().getResourceAsStream("AddonCompanion"+".ser");
            ObjectInputStream inStream = new ObjectInputStream( fis );
            ModelMBeanInfo mmbinfo1 = ( ModelMBeanInfo )inStream.readObject();
            RequiredModelMBean modelmbean = new RequiredModelMBean(mmbinfo1);
            modelmbean.setManagedResource(o, "objectReference");
            MBeanServer mbs = ManagementFactory.getPlatformMBeanServer();
            mbs.registerMBean(modelmbean,mbon);
            System.out.println("invoking value ..." + mbs.invoke(mbon, "getName", null, null));
            //System.out.println("getattr value ..." + mbs.getAttribute(mbon, "Name"));
            //System.out.println("Waiting forever..."); 
            //Thread.sleep(Long.MAX_VALUE); 
        } catch (Exception e) {
            System.out.println("MSR: exception while creating mBean ...");
            e.printStackTrace();
        }
    }

}
