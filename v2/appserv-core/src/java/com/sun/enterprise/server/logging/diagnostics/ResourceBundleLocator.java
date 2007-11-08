/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
 * 
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License. You can obtain
 * a copy of the License at https://glassfish.dev.java.net/public/CDDL+GPL.html
 * or glassfish/bootstrap/legal/LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 * 
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at glassfish/bootstrap/legal/LICENSE.txt.
 * Sun designates this particular file as subject to the "Classpath" exception
 * as provided by Sun in the GPL Version 2 section of the License file that
 * accompanied this code.  If applicable, add the following below the License
 * Header, with the fields enclosed by brackets [] replaced by your own
 * identifying information: "Portions Copyrighted [year]
 * [name of copyright owner]"
 * 
 * Contributor(s):
 * 
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
package com.sun.enterprise.server.logging.diagnostics;

import java.util.Hashtable;
import java.util.ResourceBundle;
import java.util.Locale;

/**
 * A Class to locate the Resource Bundle based on Module Id.
 *
 * @author Hemanth Puttaswamy
 *
 * Issues: 
 * 1. We need to be able to handle a case where the diagnostic information
 *    may come from multiple resource bundles. Ex: JDO's message ids comes from
 *    4 different resource bundles.
 */
public class ResourceBundleLocator {
    private static Hashtable moduleIdToResourceBundleTable;

    private static String[] jdoResourceBundles = {
        "com.sun.jdo.spi.persistence.support.ejb.ejbc.Bundle",
        "com.sun.jdo.spi.persistence.generator.database.Bundle",
        "com.sun.jdo.spi.persistence.support.ejb.ejbqlc.Bundle",
        "com.sun.jdo.spi.persistence.support.sqlstore.Bundle",
        "com.sun.jdo.spi.persistence.utility.logging.Bundle" };

    static {
        moduleIdToResourceBundleTable = new Hashtable( );
        moduleIdToResourceBundleTable.put( "ADM", 
            "com.sun.logging.enterprise.system.tools.admin.LogStrings" );
        moduleIdToResourceBundleTable.put( "CONF", 
            "com.sun.logging.enterprise.system.core.config.LogStrings" );
        moduleIdToResourceBundleTable.put( "DPL", 
            "com.sun.logging.enterprise.system.tools.deployment.LogStrings" );
        moduleIdToResourceBundleTable.put( "EJB", 
            "com.sun.logging.enterprise.system.container.ejb.LogStrings" );
        moduleIdToResourceBundleTable.put( "IOP", 
            "com.sun.corba.ee.impl.logging.LogStrings" );
        moduleIdToResourceBundleTable.put( "JAXR", 
            "com.sun.logging.enterprise.system.webservices.registry.LogDomains" );
        moduleIdToResourceBundleTable.put( "NAM", 
            "com.sun.logging.enterprise.system.core.naming.LogStrings" );
        moduleIdToResourceBundleTable.put( "DTX", 
            "com.sun.logging.enterprise.resource.jta.LogStrings" );
        moduleIdToResourceBundleTable.put( "SYNC", 
            "com.sun.logging.ee.enterprise.system.tools.synchronization.LogStrings" );
        moduleIdToResourceBundleTable.put( "HADBMG", 
            "com.sun.enterprise.ee.admin.hadbmgmt.LocalStrings" );
        moduleIdToResourceBundleTable.put( "JAXRPC", 
            "com.sun.logging.enterprise.system.webservices.rpc.LogDomains" );
        moduleIdToResourceBundleTable.put( "JML", 
            "com.sun.logging.enterprise.resource.javamail.LogStrings");
        moduleIdToResourceBundleTable.put( "JMS", 
            "com.sun.logging.enterprise.resource.jms.LogStrings");
        moduleIdToResourceBundleTable.put( "JTS", 
            "com.sun.logging.enterprise.system.core.transaction.LogStrings");
        moduleIdToResourceBundleTable.put( "LDR", 
            "com.sun.logging.enterprise.system.core.classloading.LogStrings");
        moduleIdToResourceBundleTable.put( "MDB", 
            "com.sun.logging.enterprise.system.container.ejb.mdb.LogStrings" );
        moduleIdToResourceBundleTable.put( "JNDI", 
            "com.sun.logging.enterprise.system.core.naming.LogStrings" );
        moduleIdToResourceBundleTable.put( "RAR", 
            "com.sun.logging.enterprise.resource.resourceadapter.LogStrings" );
        moduleIdToResourceBundleTable.put( "SAAJ", 
            "com.sun.logging.enterprise.system.webservices.saaj.LogDomains" );
        moduleIdToResourceBundleTable.put( "SEC", 
            "com.sun.logging.enterprise.system.core.security.LogStrings" );
        moduleIdToResourceBundleTable.put( "SERVER", 
            "com.sun.logging.enterprise.system.LogStrings");
        moduleIdToResourceBundleTable.put( "TLS", 
            "com.sun.logging.enterprise.system.tools.LogStrings");
        moduleIdToResourceBundleTable.put( "UTIL", 
            "com.sun.logging.enterprise.system.util.LogStrings" );
        moduleIdToResourceBundleTable.put( "VRFY", 
            "com.sun.logging.enterprise.system.tools.verifier.LogStrings");
        moduleIdToResourceBundleTable.put( "WEB", 
            "com.sun.logging.enterprise.system.container.web.LogStrings");
        moduleIdToResourceBundleTable.put( "PWC", 
            "com.sun.enterprise.web.logging.pwc.LogStrings");
        moduleIdToResourceBundleTable.put( "CMNUTL", 
            "com.sun.common.util.logging.LogStrings");
        moduleIdToResourceBundleTable.put( "TEST", 
            "com.sun.enterprise.server.logging.diagnostics.LogStrings" );
    }

    /**
      * Utility method to get the ResourceBundle.
      */
     public static ResourceBundle getResourceBundleForMessageId( String messageId ) {
         String moduleId = getModuleId( messageId );
         if( moduleId == null ) { return null; }
         ResourceBundle rb = null;
         if( moduleId.equals( Constants.JDO_MESSAGE_PREFIX ) ) {
             // Specialized search for JDO.
             rb = getResourceBundleForJDOMessageId( messageId );
         } else {
             rb = getResourceBundleForModuleId( moduleId );
         }
         return rb;
     }


    /**
     * Locates the Resource Bundle using moduleId as the key.
     * ModuleId's will be the 3-6 character prefix in the message id's
     * examples: JAXRPC1234, JDO5678. Here JAXRPC and JDO are the module
     * id's used to locate resource bundles.
     */
    public static ResourceBundle getResourceBundleForModuleId( String moduleId ) {
        if( moduleId == null ) { return null; }
        String bundleName = null;
        try {
            bundleName = (String)moduleIdToResourceBundleTable.get( moduleId );
            if( bundleName == null ) return null;
            return ResourceBundle.getBundle(bundleName, 
                Locale.getDefault(), getClassLoader( ) ); 
        } catch( Exception e) {
            System.err.println( e );
            e.printStackTrace( );
        }
        return null; 
    }


    /**
     *  A Specialized method to look up JDO ResourceBundle with a 
     *  particular message id. JDO Module has more than 1 resource bundle,
     *  hence this specialization is required.
     */
    public static ResourceBundle getResourceBundleForJDOMessageId( 
        String messageId )
    {
        for( int i = 0; i < jdoResourceBundles.length; i++ ) {
            ResourceBundle rb = ResourceBundle.getBundle(jdoResourceBundles[i], 
                Locale.getDefault(), getClassLoader( ) ); 
            if( rb != null ) {
                try {
                    if( rb.getString( 
                        messageId + Constants.CAUSE_PREFIX + 1 ) != null ) 
                    {
                        // We found the diagnostics for the JDO MessageId. 
                        // Return the resource bundle.
                        return rb;
                    }
                } catch( java.util.MissingResourceException e ) {
                    // We will just drop this exception becuse it's normal
                    // for java to throw an exception if a key look up in
                    // resource bundle fails
                } 
            }
        }
        return null;
    }

    private static ClassLoader getClassLoader( ) {
        // Use the thread's context ClassLoader.  If there isn't one,
        // use the SystemClassloader.
        ClassLoader cl = Thread.currentThread().getContextClassLoader();
        if (cl == null) {
            cl = ClassLoader.getSystemClassLoader();
        }
        return cl;
    } 

     /**
      * An utility method to separate module id prefix in the message id.
      */
     public static String getModuleId( String messageId ) {
        if( (messageId == null )
          ||(messageId.length() == 0 ) )
        {
            return null;
        }
        int lastIndex = 6;
        if( messageId.length() < lastIndex ) { lastIndex = messageId.length();}
                                                                                 
        char[] moduleIdCharacters =
            messageId.substring(0,lastIndex).toCharArray( );
        lastIndex = moduleIdCharacters.length;
        // If there are no numbers and the moduleId like JAXRPC doesn't need
        // further processing
        if( Character.isDigit(moduleIdCharacters[moduleIdCharacters.length-1]) )
        {
             for( int index = (moduleIdCharacters.length-1); index > 0;
                 index-- )
             {
                 if( !Character.isDigit(moduleIdCharacters[index]) ) {
                    lastIndex = index + 1;
                    break;
                 }
            }
        }
        return messageId.substring( 0, lastIndex );
    }
}
