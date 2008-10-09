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

package com.sun.ejb.base.sfsb.initialization;

import java.util.logging.Level;
import java.util.logging.Logger;

import com.sun.logging.LogDomains;

import com.sun.appserv.ha.util.*;

import com.sun.ejb.base.sfsb.store.FileTxStoreManager;

import com.sun.ejb.spi.sfsb.store.SFSBTxStoreManager;
import org.jvnet.hk2.annotations.Service;
import org.jvnet.hk2.annotations.Scoped;
import org.jvnet.hk2.component.PerLookup;

/**
 * Factory for creating SFSBTxStoreManager. SFSBTxStoreManager is
 * responsible for checkpointing an Array of SFSBBeanState(s)
 * (as a single transactional unit if possible)
 *
 * @author Mahesh Kannan
 */
public class SFSBTxStoreManagerFactory {

    private static final Logger _logger =
            LogDomains.getLogger(SFSBTxStoreManagerFactory.class, LogDomains.EJB_LOGGER);

    protected static final String DEFAULT_EE_PACKAGE =
            "com.sun.ejb.ee.sfsb.store";

    public static SFSBTxStoreManager createSFSBTxStoreManager(
            String persistenceType) {
        if ("file".equalsIgnoreCase(persistenceType)) {
            return new FileTxStoreManager();
        }
        String resolvedPersistenceType
                = getResolvedPersistenceType(persistenceType);
        Exception exception = null;
        try {
            String className = createClassNameFrom(resolvedPersistenceType);
            return (SFSBTxStoreManager)
                    (Class.forName(className)).newInstance();
        } catch (ClassNotFoundException cnfEx) {
            exception = cnfEx;
            _logger.log(Level.FINE,
                    "Exception while creating SFSBTxStoreManager for persistence "
                            + "type: " + persistenceType + ". Exception: " + cnfEx);
        } catch (Exception ex) {
            exception = ex;
            _logger.log(Level.FINE,
                    "Exception while creating SFSBTxStoreManager for "
                            + "persistence type: " + persistenceType, ex);
        }

        if (_logger.isLoggable(Level.WARNING)) {
            _logger.log(Level.WARNING, "Created FileTxStoreManager for persistence"
                    + " type: " + persistenceType, exception);
        }

        return new FileTxStoreManager();
    }

    /**
     * @param persistenceType
     */
    private static String createClassNameFrom(String persistenceType) {
        StringBuffer sbuf = new StringBuffer();
        sbuf.append(getEEPackage()).append(".")
                .append(camelCase(persistenceType))
                .append("TxStoreManager");
        String classname = sbuf.toString();
        return classname;
    }

    /**
     * this method strips out all non-alpha characters; camelCases the result
     *
     * @param inputString
     */
    private static String camelCase(String inputString) {
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
    private static String stripNonAlphas(String inputString) {
        StringBuffer sb = new StringBuffer(50);
        for (int i = 0; i < inputString.length(); i++) {
            char nextChar = inputString.charAt(i);
            if (Character.isLetter(nextChar)) {
                sb.append(nextChar);
            }
        }
        return sb.toString();
    }

    /**
     * return the path where the ee builders reside
     * although this method allows this to be configurable
     * via an property in server.xml we do not expose it
     * and it should not be re-configured
     */
    private static String getEEPackage() {
        return DEFAULT_EE_PACKAGE;
    }

    private static String getResolvedPersistenceType(String persistenceType) {
        String resolverClassName
                = "com.sun.enterprise.ee.web.sessmgmt.EEPersistenceTypeResolver";

        String resolvedPersistenceType = persistenceType;
        PersistenceTypeResolver persistenceTypeResolver = null;
        try {
            persistenceTypeResolver =
                    (PersistenceTypeResolver) (Class.forName(resolverClassName)).newInstance();
            if (persistenceTypeResolver != null) {
                resolvedPersistenceType
                        = persistenceTypeResolver.resolvePersistenceType(persistenceType);
                //this is because file not memory is the correct 'default'
                if ("memory".equalsIgnoreCase(resolvedPersistenceType)) {
                    resolvedPersistenceType = "file";
                } else if ("replicated".equalsIgnoreCase(resolvedPersistenceType)) {
                    //this is because replicated can make use of the same
                    //HaTxStoreManager that ha uses
                    resolvedPersistenceType = "ha";
                }
            }
        } catch (Exception ex) {
            _logger.log(Level.WARNING, "unable to create persistence type resolver");
        }
        return resolvedPersistenceType;
    }

}
