/*
 * The contents of this file are subject to the terms 
 * of the Common Development and Distribution License 
 * (the License).  You may not use this file except in
 * compliance with the License.
 * 
 * You can obtain a copy of the license at 
 * https://glassfish.dev.java.net/public/CDDLv1.0.html or
 * glassfish/bootstrap/legal/CDDLv1.0.txt.
 * See the License for the specific language governing 
 * permissions and limitations under the License.
 * 
 * When distributing Covered Code, include this CDDL 
 * Header Notice in each file and include the License file 
 * at glassfish/bootstrap/legal/CDDLv1.0.txt.  
 * If applicable, add the following below the CDDL Header, 
 * with the fields enclosed by brackets [] replaced by
 * you own identifying information: 
 * "Portions Copyrighted [year] [name of copyright owner]"
 * 
 * Copyright 2008 Sun Microsystems, Inc. All rights reserved.
 */

package org.glassfish.admin.runtime.mgmtinfra;

import org.jvnet.hk2.component.CageBuilder;
import org.jvnet.hk2.component.Inhabitant;
import org.jvnet.hk2.annotations.Service;
import javax.management.*;
import javax.management.modelmbean.*;
import java.lang.management.*;
import java.io.*;

/**
 * Entry class for creating and registering runtime mBeans based on POJOs 
 *
 * @author Sreenivas Munnangi
 */
@Service
public class RuntimeMBeanCageBuilder implements CageBuilder {

    private static final boolean dbg = false;

    public void onEntered(Inhabitant<?> i) {
        if (dbg) {
            System.out.println("RuntimeMBeanCageBuilder: onEntered(" + i.typeName() + 
                "): time to create and register the mBean ...");
        }
        Object o = i.get();
        createMBean(o);
    }

    private void createMBean(Object o) {
        if (dbg) {
            System.out.println("RuntimeMBeanCageBuilder: createMBean: " + 
                "creating mbean for Object ..." + o);
        }
        try {
            String className = o.getClass().getName();
            String serName = className.substring(className.lastIndexOf(".") + 1);
            if (dbg) {
                System.out.println("RuntimeMBeanCageBuilder: createMBean: " + 
                    " className = " + className +
                    " serName = " + serName);
            }
            InputStream fis = null;
            ObjectInputStream inStream = null;
            try {
                fis = o.getClass().getResourceAsStream(serName+".ser");
                inStream = new ObjectInputStream( fis );
            } catch (Exception ex) {
                if (dbg) {
                    System.out.println("RuntimeMBeanCageBuilder: createMBean: " + 
                        "companion class " + o + " is not an mBean ...");
                }
                ex.printStackTrace();
                return;
            }
            ObjectName mbon = new ObjectName(((Runtime)o).getObjectName());
            ModelMBeanInfo mmbinfo = ( ModelMBeanInfo )inStream.readObject();
            RequiredModelMBean modelmbean = new RequiredModelMBean(mmbinfo);
            modelmbean.setManagedResource(o, "objectReference");
            MBeanServer mbs = ManagementFactory.getPlatformMBeanServer();
            mbs.registerMBean(modelmbean,mbon);
        } catch (Exception e) {
            if (dbg) {
                System.out.println("RuntimeMBeanCageBuilder: createMBean: " + 
                    "exception while creating mBean ...");
            }
            e.printStackTrace();
        }
    }

}
