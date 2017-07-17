/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2017 Oracle and/or its affiliates. All rights reserved.
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

package com.sun.s1asdev.jdbc.CustomResourceFactories.ejb;

import com.sun.s1asdev.custom.resource.CustomResourceJavaBean;

import javax.ejb.*;
import javax.naming.*;
import javax.sql.*;
import java.sql.*;
import java.rmi.RemoteException;
import java.util.Properties;
import java.util.Enumeration;
import java.util.Set;
import java.util.Iterator;
import java.net.URL;


public class SimpleBMPBean
        implements EntityBean {

    protected Object obj;

    public void setEntityContext(EntityContext entityContext) {
    }

    public boolean testJavaBean(String testValue) {
        try {
            InitialContext ic = new InitialContext();
            CustomResourceJavaBean o = (CustomResourceJavaBean) ic.lookup("java:comp/env/custom/my-java-bean");
            if (o != null) {
                //System.out.println("Custom Resource : " + o);
                System.out.println("Custom resource value : " + o.getProperty());
                if (o.getProperty().equalsIgnoreCase(testValue)) {
                    return true;
                }else{
                   System.out.println("testJavaBean failed");
                }
            }
        } catch (NamingException e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean testPrimitives(String type, String value, String resourceName) throws RemoteException {
        try {
            InitialContext ic = new InitialContext();
            if(!resourceName.startsWith("java:")){
                resourceName = "java:comp/env/"+resourceName;
            }
            Object o = ic.lookup(resourceName);
            if (o != null) {
                System.out.println("Custom resource value : " + o);
                if (o.toString().equalsIgnoreCase(value)) {
                    return true;
                }
            }
        } catch (NamingException e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean testProperties(Properties properties, String resourceName) throws RemoteException {

        try {
            InitialContext ic = new InitialContext();
            Properties p = (Properties) ic.lookup(resourceName);

            Set keys = p.keySet();
            Iterator iterator = keys.iterator();
            while (iterator.hasNext()) {
                String key = (String) iterator.next();
                String value = (String) p.get(key);

                String result = (String) properties.get(key);
                if (result != null) {
                    if (!result.equalsIgnoreCase(value)) {
                        return false;
                    }
                } else {
                    return false;
                }
            }

            return true;
        } catch (NamingException e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean testURL(String url, String resourceName) throws RemoteException {
        try {
            InitialContext ic = new InitialContext();
            URL boundURL = (URL) ic.lookup(resourceName);
            if (boundURL != null) {
                if (boundURL.toString().equals(url))
                    return true;
            }
        } catch (NamingException e) {
            e.printStackTrace();
        }
        return false;
    }

    public Integer ejbCreate() throws CreateException {
        return new Integer(1);
    }

    public void ejbLoad() {
    }

    public void ejbStore() {
    }

    public void ejbRemove() {
    }

    public void ejbActivate() {
    }

    public void ejbPassivate() {
    }

    public void unsetEntityContext() {
    }

    public void ejbPostCreate() {
    }
}
