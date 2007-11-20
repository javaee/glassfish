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

/*
 * PersistenceStrategyBuilderFactory.java
 *
 * Created on September 30, 2002, 11:16 AM
 */

package com.sun.enterprise.web;

import java.util.*;
import java.util.logging.Logger;
import java.util.logging.Level;
import com.sun.logging.LogDomains;
import org.apache.catalina.Context;
import com.sun.appserv.ha.util.*;


public class PersistenceStrategyBuilderFactory {
    
  /**
  * The default path to the EE persistence strategy builders
   *only used if getEEBuilderPath fails (which should only occur
   *in unit-test 
  */ 
  protected final String DEFAULT_EE_BUILDER_PATH = "com.sun.enterprise.ee.web.initialization";    

    /**
     * return the path where the ee builders reside
     * although this method allows this to be configurable
     * via an property in server.xml we do not expose it
     * and it should not be re-configured
     *
     */    
    private String getEEBuilderPath() {
        if(_eeBuilderPath == null) {
            ServerConfigLookup lookup = new ServerConfigLookup();
            _eeBuilderPath = lookup.getEEBuilderPathFromConfig();
        }
        _logger.finest("_eeBuilderPath = " + _eeBuilderPath);    
        return _eeBuilderPath;
    }
    
    /** Creates a new instance of PersistenceStrategyBuilderFactory */
    public PersistenceStrategyBuilderFactory() {
        if (_logger == null) {
            _logger = LogDomains.getLogger(LogDomains.WEB_LOGGER);
        }            
    }   

    /**
     * creates the correct implementation of PersistenceStrategyBuilder
     * if an invalid combination is input; an error is logged
     * and MemoryStrategyBuilder is returned
     *
     * @param persistenceType
     * @param frequency
     * @param scope
     * @param ctx
     */    
    PersistenceStrategyBuilder createPersistenceStrategyBuilder(String persistenceType, String frequency, String scope, Context ctx) {
        
        String resolvedPersistenceFrequency = null;
        String resolvedPersistenceScope = null;
        PersistenceTypeResolver persistenceTypeResolver 
            = this.getPersistenceTypeResolver();
        String resolvedPersistenceType 
            = persistenceTypeResolver.resolvePersistenceType(persistenceType);
        
        if( resolvedPersistenceType.equalsIgnoreCase("memory") 
            | resolvedPersistenceType.equalsIgnoreCase("file")) {
            //deliberately leaving frequency & scope null
        } else {
            resolvedPersistenceFrequency = frequency;
            resolvedPersistenceScope = scope;
        }
        _logger.finest("resolvedPersistenceType = " + resolvedPersistenceType);
        _logger.finest("resolvedPersistenceFrequency = " + resolvedPersistenceFrequency);
        _logger.finest("resolvedPersistenceScope = " + resolvedPersistenceScope);
        
        String passedInPersistenceType = persistenceType;
        PersistenceStrategyBuilder builder = new MemoryStrategyBuilder();
        String className = createClassNameFrom(resolvedPersistenceType, resolvedPersistenceFrequency, resolvedPersistenceScope);
        _logger.finest("PersistenceStrategyBuilderFactory>>createPersistenceStrategyBuilder: "
            + "CandidateBuilderClassName = " + className);      
        try {
            builder = 
                (PersistenceStrategyBuilder) (Class.forName(className)).newInstance();
        } catch (Exception ex) {
            Object[] params = { getApplicationId(ctx), persistenceType, frequency, scope };
            _logger.log(Level.WARNING,
                        "webcontainer.invalidSessionManagerConfig",
                        params);            
        }
        builder.setPersistenceFrequency(frequency);
        builder.setPersistenceScope(scope);
        builder.setPassedInPersistenceType(passedInPersistenceType);

        return builder;
    } 

    private PersistenceTypeResolver getPersistenceTypeResolver() {
        String resolverClassName 
            = "com.sun.enterprise.ee.web.sessmgmt.EEPersistenceTypeResolver";
    
        PersistenceTypeResolver persistenceTypeResolver = null;
        try {
            persistenceTypeResolver = 
                (PersistenceTypeResolver) (Class.forName(resolverClassName)).newInstance();
        } catch (Exception ex) {
            _logger.finest("unable to create persistence type resolver");
        } 
        return persistenceTypeResolver;
    }

    /**
     * returns the application id for the module
     *
     * @param ctx the context
     */    
    public String getApplicationId(Context ctx) {
        com.sun.enterprise.web.WebModule wm =
        (com.sun.enterprise.web.WebModule)ctx;
        return wm.getID();
    }    
    
    /**
     * returns an appropriately camel-cased String
     * that is a candidate class name for a builder
     * if persistenceType is "memory" or "file" this returns
     * the correct class name and package name for these classes
     * i.e. com.iplanet.ias.web
     * otherwise they are in com.sun.appserv.ee.web.initialization
     *
     * @param persistenceType
     * @param frequency
     * @param scope
     */     
    private String createClassNameFrom(String persistenceType, String frequency, String scope) {
        StringBuffer sb = new StringBuffer();
        //using package name will mean this will work
        //even if class is moved to another package
        String pkg = this.getClass().getPackage().getName();
        if( !(persistenceType.equalsIgnoreCase("memory") 
            | persistenceType.equalsIgnoreCase("file")) )  
        {
            //pkg is the package where EE builders MUST reside
            //this defaults to
            //"com.sun.enterprise.ee.web.initialization"
            // but is configurable via (not-well-publicized)
            //property in server.xml
            //at "/server/availability-service/persistence-store/property[@name='ee-builder-path']"
            pkg = this.getEEBuilderPath();
        }
        sb.append(pkg + ".");
        sb.append(camelCase(persistenceType));
        if(frequency != null) {
            sb.append(camelCase(frequency));
        }
        if(scope != null) {
            sb.append(camelCase(scope));
        }
        sb.append("StrategyBuilder");
        String classname = sb.toString();
        return classname;
    }      

    /**
     * this method strips out all non-alpha characters; camelCases the result
     *
     * @param inputString
     */     
    private String camelCase(String inputString) {
        String strippedString = stripNonAlphas(inputString);
        String firstLetter = (strippedString.substring(0, 1)).toUpperCase();
        String remainingPart = 
            (strippedString.substring(1, strippedString.length())).toLowerCase();
        return firstLetter + remainingPart;
    }

    /**
     * this method strips out all non-alpha characters
     *
     * @param inputString
     */     
    private String stripNonAlphas(String inputString) {
        StringBuffer sb = new StringBuffer(50);
        for(int i=0; i<inputString.length(); i++) {
            char nextChar = inputString.charAt(i);
            if(Character.isLetter(nextChar)) {
                sb.append(nextChar);
            }
        }
        return sb.toString();
    }
    
    /**
     * The logger to use for logging ALL web container related messages.
     */
    private static Logger _logger = null; 
    
    /**
     * The path where ee builders reside
     */
    private String _eeBuilderPath = null;
    
}
