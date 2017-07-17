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

import javax.annotation.Resource;
import javax.ejb.*;
import javax.naming.*;
import java.rmi.RemoteException;
import java.sql.*;
import java.rmi.RemoteException;
import java.util.Properties;
import java.util.Enumeration;
import java.util.Set;
import java.util.Iterator;
import java.net.URL;

@Stateful
public class SimpleBMPBean {

    protected Object obj;


    @Resource(mappedName="custom/my-properties-file")
    Properties myPropertiesFile;

    @Resource(mappedName="custom/my-simple-properties")
    Properties mySimpleProperties;

    @Resource(mappedName="custom/my-properties-xml-file")
    Properties myPropertiesXMLFile;

    @Resource(mappedName="custom/my-properties-file-with-values")
    Properties myPropertiesWithValues;

    @Resource(mappedName="custom/my-properties-xml-file-with-values")
    Properties myPropertiesWithXMLFileWithValues;

    @Resource(mappedName="custom/my-properties-file")
    Properties myPropertiesFileLookup;

    @Resource(mappedName="custom/my-simple-properties")
    Properties mySimplePropertiesLookup;

    @Resource(mappedName="custom/my-properties-xml-file")
    Properties myPropertiesXMLFileLookup;

    @Resource(mappedName="custom/my-properties-file-with-values")
    Properties myPropertiesWithValuesLookup;

    @Resource(mappedName="custom/my-properties-xml-file-with-values")
    Properties myPropertiesWithXMLFileWithValuesLookup;


    private boolean testAndPrintProperties(String resourceName, Properties properties){
        boolean propertiesFound = false;
        System.out.println("resource-name : " + resourceName);
        if(properties != null){
            for(Object o : properties.keySet()){
                propertiesFound = true;
                System.out.println("key : " + o + ", value : " + properties.get(o));
            }
        }else{
            propertiesFound = false;
                System.out.println("null properties from resource : " + resourceName);
            }
        return propertiesFound;
    }

    public boolean testLookupNames() throws RemoteException{

        System.out.println("myPropertiesFileLookup : " + myPropertiesFileLookup);
        System.out.println("mySimplePropertiesLookup : " + mySimplePropertiesLookup);
        System.out.println("myPropertiesXMLFileLookup : " + myPropertiesXMLFileLookup);
        System.out.println("myPropertiesWithValuesLookup : " + myPropertiesWithValuesLookup);
        System.out.println("myPropertiesWithXMLFileWithValuesLookup : " + myPropertiesWithXMLFileWithValuesLookup);

        boolean  myPropertiesFileLookupBoolean = testAndPrintProperties("myPropertiesFileLookup", myPropertiesFileLookup);
        boolean  mySimplePropertiesLookupBoolean = testAndPrintProperties("mySimplePropertiesLookup", mySimplePropertiesLookup);
        boolean  myPropertiesXMLFileLookupBoolean = testAndPrintProperties("myPropertiesXMLFileLookup", myPropertiesXMLFileLookup);
        boolean  myPropertiesWithValuesLookupBoolean = testAndPrintProperties("myPropertiesWithValuesLookup", myPropertiesWithValuesLookup);
        boolean  myPropertiesWithXMLFileWithValuesLookupBoolean = testAndPrintProperties("myPropertiesWithXMLFileWithValuesLookup", myPropertiesWithXMLFileWithValuesLookup);

        boolean injectionSucceeded = myPropertiesFileLookup != null && mySimplePropertiesLookup != null
                && myPropertiesXMLFileLookup != null && myPropertiesWithValuesLookup != null && myPropertiesWithXMLFileWithValuesLookup != null;
        boolean nonEmptyProperties =  myPropertiesFileLookupBoolean  && mySimplePropertiesLookupBoolean &&  myPropertiesXMLFileLookupBoolean
                && myPropertiesWithValuesLookupBoolean && myPropertiesWithXMLFileWithValuesLookupBoolean;

        return injectionSucceeded && nonEmptyProperties;
    }

    public boolean testMappedNames() throws RemoteException{

        System.out.println("myPropertiesFile : " + myPropertiesFile);
        System.out.println("mySimpleProperties : " + mySimpleProperties);
        System.out.println("myPropertiesXMLFile : " + myPropertiesXMLFile);
        System.out.println("myPropertiesWithValues : " + myPropertiesWithValues);
        System.out.println("myPropertiesWithXMLFileWithValues : " + myPropertiesWithXMLFileWithValues);

        boolean myPropertiesFileBoolean= testAndPrintProperties("myPropertiesFile", myPropertiesFile);
        boolean mySimplePropertiesBoolean= testAndPrintProperties("mySimpleProperties", mySimpleProperties);
        boolean myPropertiesXMLFileBoolean= testAndPrintProperties("myPropertiesXMLFile", myPropertiesXMLFile);
        boolean myPropertiesWithValuesBoolean= testAndPrintProperties("myPropertiesWithValues", myPropertiesWithValues);
        boolean myPropertiesWithXMLFileWithValuesBoolean= testAndPrintProperties("myPropertiesWithXMLFileWithValues", myPropertiesWithXMLFileWithValues);

        boolean injectionSucceeded = myPropertiesFile != null && mySimpleProperties != null
                && myPropertiesXMLFile != null && myPropertiesWithValues != null && myPropertiesWithXMLFileWithValues != null;

        boolean nonEmptyProperties =  myPropertiesFileBoolean &&  mySimplePropertiesBoolean &&  myPropertiesXMLFileBoolean &&
                 myPropertiesWithValuesBoolean &&   myPropertiesWithXMLFileWithValuesBoolean;

        return injectionSucceeded && nonEmptyProperties;
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
            Object o = ic.lookup("java:comp/env/" + resourceName);
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
