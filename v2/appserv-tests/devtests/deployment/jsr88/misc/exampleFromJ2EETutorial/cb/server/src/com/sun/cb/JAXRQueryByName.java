/*
 * Copyright (c) 2003 Sun Microsystems, Inc.  All rights reserved.  U.S. 
 * Government Rights - Commercial software.  Government users are subject 
 * to the Sun Microsystems, Inc. standard license agreement and 
 * applicable provisions of the FAR and its supplements.  Use is subject 
 * to license terms.  
 * 
 * This distribution may include materials developed by third parties. 
 * Sun, Sun Microsystems, the Sun logo, Java and J2EE are trademarks 
 * or registered trademarks of Sun Microsystems, Inc. in the U.S. and 
 * other countries.  
 * 
 * Copyright (c) 2003 Sun Microsystems, Inc. Tous droits reserves.
 * 
 * Droits du gouvernement americain, utilisateurs gouvernementaux - logiciel
 * commercial. Les utilisateurs gouvernementaux sont soumis au contrat de 
 * licence standard de Sun Microsystems, Inc., ainsi qu'aux dispositions 
 * en vigueur de la FAR (Federal Acquisition Regulations) et des 
 * supplements a celles-ci.  Distribue par des licences qui en 
 * restreignent l'utilisation.
 * 
 * Cette distribution peut comprendre des composants developpes par des 
 * tierces parties. Sun, Sun Microsystems, le logo Sun, Java et J2EE 
 * sont des marques de fabrique ou des marques deposees de Sun 
 * Microsystems, Inc. aux Etats-Unis et dans d'autres pays.
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
