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
