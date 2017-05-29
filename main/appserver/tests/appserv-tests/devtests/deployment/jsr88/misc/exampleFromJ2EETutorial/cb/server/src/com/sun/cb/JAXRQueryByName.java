/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2003-2017 Oracle and/or its affiliates. All rights reserved.
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

package com.sun.cb;

import javax.xml.registry.*; 
import javax.xml.registry.infomodel.*; 
import java.net.*;
import java.util.*;

/**
 * The JAXRQueryByName class consists of a main method, a 
 * makeConnection method, an executeQuery method, and some 
 * helper methods. It searches a registry for 
 * information about organizations whose names contain a 
 * user-supplied string.
 * 
 * To run this program, use the command 
 * 
 *     ant -Dquery-string=<value> run-query
 *
 * after starting Tomcat and Xindice.
 */
public class JAXRQueryByName {
    static Connection connection = null;

    public JAXRQueryByName() {}

    public static void main(String[] args) {
      String queryURL = URLHelper.getQueryURL();
      String publishURL = URLHelper.getPublishURL();

        if (args.length < 1) {
            System.out.println("Usage: ant " +
                "-Dquery-string=<value> run-query");
            System.exit(1);
        }
        String queryString = new String(args[0]);
        System.out.println("Query string is " + queryString);
        
        JAXRQueryByName jq = new JAXRQueryByName();

        connection = jq.makeConnection(queryURL, publishURL);

        jq.executeQuery(queryString);
    }

    /**
     * Establishes a connection to a registry.
     *
     * @param queryUrl	the URL of the query registry
     * @param publishUrl	the URL of the publish registry
     * @return the connection
     */
    public Connection makeConnection(String queryUrl, 
        String publishUrl) {

        /*
         * Edit to provide your own proxy information
         *  if you are going beyond your firewall.
         * Host format: "host.subdomain.domain.com".
         * Port is usually 8080.
         * Leave blank to use Registry Server.
         */
        String httpProxyHost = "";
        String httpProxyPort = "";

        /* 
         * Define connection configuration properties. 
         * For simple queries, you need the query URL.
         * To obtain the connection factory class, set a System 
         *   property.
         */
        Properties props = new Properties();
        props.setProperty("javax.xml.registry.queryManagerURL",
            queryUrl);
        props.setProperty("com.sun.xml.registry.http.proxyHost", 
            httpProxyHost);
        props.setProperty("com.sun.xml.registry.http.proxyPort", 
            httpProxyPort);

        try {
            // Create the connection, passing it the 
            // configuration properties
            ConnectionFactory factory = 
                ConnectionFactory.newInstance();
            factory.setProperties(props);
            connection = factory.createConnection();
            System.out.println("Created connection to registry");
        } catch (Exception e) {
            e.printStackTrace();
            if (connection != null) {
                try {
                    connection.close();
                } catch (JAXRException je) {}
            }
        }
        return connection;
    }
    
    /**
     * Returns  organizations containing a string.
     *
     * @param qString	the string argument
     * @return a collection of organizations
     */
    public Collection executeQuery(String qString) {
        RegistryService rs = null;
        BusinessQueryManager bqm = null;
        Collection orgs = null;

        try {
            // Get registry service and query manager
            rs = connection.getRegistryService();
            bqm = rs.getBusinessQueryManager();
            System.out.println("Got registry service and " + "query manager");

            // Define find qualifiers and name patterns
            Collection findQualifiers = new ArrayList();
            findQualifiers.add(FindQualifier.SORT_BY_NAME_DESC);
            Collection namePatterns = new ArrayList();
            // % still doesn't work
            namePatterns.add(qString);
            //namePatterns.add("%" + qString + "%");

            // Find using the name
            BulkResponse response = 
                bqm.findOrganizations(findQualifiers, 
                    namePatterns, null, null, null, null);
            orgs = response.getCollection();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        } 
        
        return orgs;
    }

    /**
     * Returns the name value for a registry object.
     *
     * @param ro	a RegistryObject
     * @return		the String value
     */
    public String getName(RegistryObject ro) 
        throws JAXRException {

        try {
            return ro.getName().getValue();
        } catch (NullPointerException npe) {
            return "No Name";
        }
    }
    
    /**
     * Returns the description value for a registry object.
     *
     * @param ro	a RegistryObject
     * @return		the String value
     */
    public String getDescription(RegistryObject ro) 
        throws JAXRException {
        try {
            return ro.getDescription().getValue();
        } catch (NullPointerException npe) {
            return "No Description";
        }
    }
    
    /**
     * Returns the key id value for a registry object.
     *
     * @param ro	a RegistryObject
     * @return		the String value
     */
    public String getKey(RegistryObject ro) 
        throws JAXRException {

        try {
            return ro.getKey().getId();
        } catch (NullPointerException npe) {
            return "No Key";
        }
    }
}
