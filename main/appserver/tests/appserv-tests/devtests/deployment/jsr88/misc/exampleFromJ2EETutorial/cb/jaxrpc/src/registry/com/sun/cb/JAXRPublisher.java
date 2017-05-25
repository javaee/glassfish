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
import java.security.*;
import java.util.*;
import javax.naming.*;

/**
 * The JAXRPublisher class consists of a makeConnection
 * method and an executePublish method. The makeConnection
 * method establishes a connection to the Registry Server.
 * The executePublish method creates an organization and
 * publishes it to the registry.
 */
public class JAXRPublisher {

    Connection connection = null;

    public JAXRPublisher() {}

    /**
     * Establishes a connection to a registry.
     *
     * @param queryUrl	the URL of the query registry
     * @param publishUrl	the URL of the publish registry
     */
    public void makeConnection(String queryUrl,
        String publishUrl) {

        Context           context = null;
        ConnectionFactory factory = null;

        /*
         * Define connection configuration properties.
         * To publish, you need both the query URL and the
         * publish URL.
         */
        Properties props = new Properties();
        props.setProperty("javax.xml.registry.queryManagerURL",
            queryUrl);
        props.setProperty("javax.xml.registry.lifeCycleManagerURL",
            publishUrl);

        try {
            // Create the connection, passing it the
            // configuration properties
            context = new InitialContext();
            factory = (ConnectionFactory) 
                context.lookup("java:comp/env/eis/JAXR");
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
    }

    /**
     * Creates an organization, its classification, and its
     * services, and saves it to the registry.
     */
    public String executePublish(String username,
        String password, String endpoint) {

        String id = null;
        RegistryService rs = null;
        BusinessLifeCycleManager blcm = null;
        BusinessQueryManager bqm = null;

        try {
            rs = connection.getRegistryService();
            blcm = rs.getBusinessLifeCycleManager();
            bqm = rs.getBusinessQueryManager();
            System.out.println("Got registry service, query " +
                "manager, and life cycle manager");

            // Get authorization from the registry
            PasswordAuthentication passwdAuth =
                new PasswordAuthentication(username,
                    password.toCharArray());

            Set creds = new HashSet();
            creds.add(passwdAuth);
            connection.setCredentials(creds);
            System.out.println("Established security credentials");

            // Get hardcoded strings from a ResourceBundle
            ResourceBundle bundle =
                ResourceBundle.getBundle("com.sun.cb.CoffeeRegistry");

            // Create organization name and description
            Organization org =
                blcm.createOrganization(bundle.getString("org.name"));
            InternationalString s =
                blcm.createInternationalString
                (bundle.getString("org.description"));
            org.setDescription(s);

            // Create primary contact, set name
            User primaryContact = blcm.createUser();
            PersonName pName =
                blcm.createPersonName(bundle.getString("person.name"));
            primaryContact.setPersonName(pName);

            // Set primary contact phone number
            TelephoneNumber tNum = blcm.createTelephoneNumber();
            tNum.setNumber(bundle.getString("phone.number"));
            Collection phoneNums = new ArrayList();
            phoneNums.add(tNum);
            primaryContact.setTelephoneNumbers(phoneNums);

            // Set primary contact email address
            EmailAddress emailAddress =
                blcm.createEmailAddress(bundle.getString("email.address"));
            Collection emailAddresses = new ArrayList();
            emailAddresses.add(emailAddress);
            primaryContact.setEmailAddresses(emailAddresses);

            // Set primary contact for organization
            org.setPrimaryContact(primaryContact);

            // Set classification scheme to NAICS
            ClassificationScheme cScheme =
                bqm.findClassificationSchemeByName
                    (null, bundle.getString("classification.scheme"));

            // Create and add classification
            Classification classification = (Classification)
                blcm.createClassification(cScheme,
                    bundle.getString("classification.name"),
                    bundle.getString("classification.value"));
            Collection classifications = new ArrayList();
            classifications.add(classification);
            org.addClassifications(classifications);

            // Create services and service
            Collection services = new ArrayList();
            Service service =
                blcm.createService(bundle.getString("service.name"));
            InternationalString is =
                blcm.createInternationalString
                (bundle.getString("service.description"));
            service.setDescription(is);

            // Create service bindings
            Collection serviceBindings = new ArrayList();
            ServiceBinding binding = blcm.createServiceBinding();
            is = blcm.createInternationalString
                (bundle.getString("service.binding"));
            binding.setDescription(is);
 					  binding.setValidateURI(false);
            binding.setAccessURI(endpoint);
            serviceBindings.add(binding);

            // Add service bindings to service
            service.addServiceBindings(serviceBindings);

            // Add service to services, then add services to organization
            services.add(service);
            org.addServices(services);

            // Add organization and submit to registry
            // Retrieve key if successful
            Collection orgs = new ArrayList();
            orgs.add(org);
            BulkResponse response = blcm.saveOrganizations(orgs);
            Collection exceptions = response.getExceptions();
            if (exceptions == null) {
                System.out.println("Organization saved");

                Collection keys = response.getCollection();
                Iterator keyIter = keys.iterator();
                if (keyIter.hasNext()) {
                    javax.xml.registry.infomodel.Key orgKey =
                        (javax.xml.registry.infomodel.Key) keyIter.next();
                    id = orgKey.getId();
                    System.out.println("Organization key is " + id);
                }
            } else {
                Iterator excIter = exceptions.iterator();
                Exception exception = null;
                while (excIter.hasNext()) {
                    exception = (Exception) excIter.next();
                    System.err.println("Exception on save: " +
                        exception.toString());
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            if (connection != null) {
                try {
                    connection.close();
                } catch (JAXRException je) {
                    System.err.println("Connection close failed");
                }
            }
        }
    return id;
    }
}
