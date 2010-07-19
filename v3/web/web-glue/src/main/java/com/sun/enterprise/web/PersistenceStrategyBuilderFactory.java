/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 1997-2010 Sun Microsystems, Inc. All rights reserved.
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

package com.sun.enterprise.web;

import com.sun.appserv.ha.util.PersistenceTypeResolver;
import com.sun.enterprise.web.session.PersistenceType;
import com.sun.logging.LogDomains;
import org.apache.catalina.Context;
import org.jvnet.hk2.annotations.Inject;
import org.jvnet.hk2.component.Habitat;

import java.util.logging.Level;
import java.util.logging.Logger;


public class PersistenceStrategyBuilderFactory {
    
    private static final Logger _logger = LogDomains.getLogger(
            PersistenceStrategyBuilderFactory.class, LogDomains.WEB_LOGGER);

    Habitat habitat;

    // The path where ee builders reside
    private String _eeBuilderPath = null;

    private ServerConfigLookup serverConfigLookup;

    /**
     * Constructor.
     */
    public PersistenceStrategyBuilderFactory(
            ServerConfigLookup serverConfigLookup, Habitat habitat) {
        this.serverConfigLookup = serverConfigLookup;
        this.habitat = habitat;
        this._eeBuilderPath = serverConfigLookup.getEEBuilderPathFromConfig();
        if (_logger.isLoggable(Level.FINEST)) {
            _logger.finest("_eeBuilderPath = " + _eeBuilderPath);
        }
    }


    /**
     * creates the correct implementation of PersistenceStrategyBuilder
     * if an invalid combination is input; an error is logged
     * and MemoryStrategyBuilder is returned
     */    
    public PersistenceStrategyBuilder createPersistenceStrategyBuilder(
            String persistenceType, String frequency, String scope,
            Context ctx) {

        String resolvedPersistenceType = "memory";
        String resolvedPersistenceFrequency = null;
        String resolvedPersistenceScope = null;

        PersistenceTypeResolver persistenceTypeResolver =
            getPersistenceTypeResolver();
        if (persistenceTypeResolver != null) {
            resolvedPersistenceType =
                persistenceTypeResolver.resolvePersistenceType(persistenceType);
        } else {
            if (persistenceType != null) {
                resolvedPersistenceType = persistenceType;
            }
        }

        if (_logger.isLoggable(Level.FINEST)) {
            _logger.finest("Resolved persistence type is " + resolvedPersistenceType);
        }

        if (resolvedPersistenceType.equalsIgnoreCase(PersistenceType.MEMORY.getType()) ||
                resolvedPersistenceType.equalsIgnoreCase(PersistenceType.FILE.getType()) ||
                resolvedPersistenceType.equalsIgnoreCase(PersistenceType.COOKIE.getType())) {
            // Deliberately leaving frequency & scope null
        } else {
            resolvedPersistenceFrequency = frequency;
            resolvedPersistenceScope = scope;
        }

        if (_logger.isLoggable(Level.FINEST)) {
            _logger.finest("resolvedPersistenceType = " +
                           resolvedPersistenceType);
            _logger.finest("resolvedPersistenceFrequency = " +
                           resolvedPersistenceFrequency);
            _logger.finest("resolvedPersistenceScope = " +
                           resolvedPersistenceScope);
        }

        if (habitat == null) {
            if (_logger.isLoggable(Level.FINEST)) {
                _logger.finest("Habitat is null");
            }
        }
        PersistenceStrategyBuilder builder = habitat.getComponent(PersistenceStrategyBuilder.class, resolvedPersistenceType);
        if (builder == null) {
            builder = new MemoryStrategyBuilder();
            if (_logger.isLoggable(Level.FINEST)) {
                _logger.finest("Could not find PersistentStrategyBuilder for persistenceType  " + resolvedPersistenceType);
            }
        } else {
                if (_logger.isLoggable(Level.INFO)) {
                    _logger.info(
                    "PersistenceStrategyBuilderFactory>>createPersistenceStrategyBuilder: "
                    + "CandidateBuilderClassName = " + builder.getClass());
                }

              builder.setPersistenceFrequency(frequency);
              builder.setPersistenceScope(scope);
              builder.setPassedInPersistenceType(persistenceType);
          }
        return builder;
    } 

    /**
     * creates the correct implementation of PersistenceStrategyBuilder
     * if an invalid combination is input; an error is logged
     * and MemoryStrategyBuilder is returned
     *
     * @param persistenceType
     * @param frequency
     * @param scope
     */     
    PersistenceStrategyBuilder createPersistenceStrategyBuilder(String persistenceType, String frequency, String scope) {
        String passedInPersistenceType = persistenceType;
        PersistenceStrategyBuilder builder = habitat.getComponent(PersistenceStrategyBuilder.class, persistenceType);
        if (_logger.isLoggable(Level.FINEST)) {
            _logger.finest("PersistenceStrategyBuilderFactory>>createPersistenceStrategyBuilder: "
                           + "CandidateBuilderClassName = " + builder.getClass());
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
            persistenceTypeResolver = (PersistenceTypeResolver)
                (Class.forName(resolverClassName)).newInstance();
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
        return ((com.sun.enterprise.web.WebModule)ctx).getID();
    }    
}
