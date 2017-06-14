/*
 * Copyright 2003 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

package com.sun.enterprise.admin.mbeanapi.config;

import java.util.HashMap;

/**
 * This is the class for element representing object.
 * It contains element name and attributes for testing element
 * Thhis object is using in cofig related generic tests (create/delete/update/list...)
 * @author alexkrav
 * @version $Revision: 1.6 $
 */
public class TestElemRegistry {
    private static HashMap mRegistry = null;
    public static String mConfigName;

    //************************************************************************************************
    static RegEntry getRegEntry(String name)
    {
        return (RegEntry)mRegistry.get(name);
    }
    private static void  addRegEntry(HashMap map, String masterNode, String entryName, String[] req)
    {
        map.put(entryName, 
              new RegEntry(entryName, entryName, req, masterNode));
    }
    private static void  addRegEntry(HashMap map, String masterNode, String entryName, String dtd_name, String[] req)
    {
        map.put(entryName, 
              new RegEntry(entryName, dtd_name, req, masterNode));
    }
 /*
    public static String[] getRequiredAttrs(String name)
    {
        RegEntry entry = getRegEntry(name);
        return entry.getReqAttrs();
    }
    public static String[] getRequiredAttrClasses(String name)
    {
        RegEntry entry = getRegEntry(name);
        return entry.getReqAttrClasses();
    }
    public static String getDtdName(String name)
    {
        RegEntry entry = getRegEntry(name);
        return entry.dtdName;
    }
    
    public static int getLevel(String name)
    {
        RegEntry entry = getRegEntry(name);
        return entry.getLevel();
    }
*/
    
    //////////////////////////////////////////////////////////////////////////////////////
    public static boolean initRegistry(String configName)
    {
        mConfigName = configName;
        
        HashMap reg = new HashMap();
        addRegEntry(reg, "domain", "jdbc-connection-pool", new String[]{"name", "datasource-classname"});
        addRegEntry(reg, "domain", "custom-resource", new String[]{"jndi-name", "res-type", "factory-class"});
        addRegEntry(reg, "domain", "jndi-resource", "external-jndi-resource", new String[]{"jndi-name", "jndi-lookup-name", "res-type", "factory-class"} );
        addRegEntry(reg, "domain", "jdbc-resource", new String[]{"jndi-name", "pool-name"});
        addRegEntry(reg, "domain", "mail-resource", new String[]{"jndi-name", "host", "user", "from"});
        addRegEntry(reg, "domain", "persistence-manager-factory-resource", new String[]{"jndi-name"});
        addRegEntry(reg, "domain", "connector-connection-pool-resource", "connector-connection-pool", new String[]{"name", "resource-adapter-name", "connection-definition-name"});
        addRegEntry(reg, "domain", "connector-resource", new String[]{"jndi-name", "pool-name"});
        addRegEntry(reg, "domain", "resource-adapter", "resource-adapter-config", new String[]{"resource-adapter-name"});
        addRegEntry(reg, "domain", "admin-object-resource", new String[]{"jndi-name", "res-type", "res-adapter"});
        
        addRegEntry(reg, "http-service", "virtual-server", new String[]{"id", "hosts"});
        addRegEntry(reg, "http-service", "http-listener", new String[]{"id", "address", "port*int", "default-virtual-server", "server-name"});

        addRegEntry(reg, "iiop-service", "iiop-listener", new String[]{"id", "address"});
        addRegEntry(reg, "iiop-listener", "ssl", new String[]{"cert-nickname"});
        mRegistry = reg;
        return true;
    }
    //////////////////////////////////////////////////////////////////////////////////////
}