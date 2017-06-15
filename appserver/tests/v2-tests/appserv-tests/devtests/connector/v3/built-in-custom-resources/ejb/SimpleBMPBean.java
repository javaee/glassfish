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
