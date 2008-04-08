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
 * Copyright 2004-2005 Sun Microsystems, Inc.  All rights reserved.
 * Use is subject to license terms.
 */

package com.sun.enterprise.web.session;

/**
 * Represents each of the persistence mechanisms supported by the session
 * managers.
 */
public final class PersistenceType {

    // ------------------------------------------------------- Static Variables

    /**
     * Memory based persistence for sessions (i.e. none);
     */
    public static final PersistenceType MEMORY =
        new PersistenceType("memory");

    /**
     * File based persistence for sessions.
     */
    public static final PersistenceType FILE =
        new PersistenceType("file");

    /**
     * Custom/user implemented session manager.
     */
    public static final PersistenceType CUSTOM =
        new PersistenceType("custom");
    
    /**
     * old iWS 6.0 style session manager.
     */
    public static final PersistenceType S1WS60 =
        new PersistenceType("s1ws60");

    /**
     * old iWS 6.0 style
     * MMapSessionManager.
     */
    public static final PersistenceType MMAP =
        new PersistenceType("mmap");

    /**
     * JDBC based persistence for sessions.
     */
    public static final PersistenceType JDBC =
        new PersistenceType("jdbc");   
    
    /**
     * HADB based persistence for sessions.
     */
    public static final PersistenceType HA =
        new PersistenceType("ha");     
    
    /**
     * SJSWS replicated persistence for sessions.
     */
    public static final PersistenceType REPLICATED =
        new PersistenceType("replicated");

    // ----------------------------------------------------------- Constructors

    /**
     * Default constructor that sets its type to the specified string.
     */
    private PersistenceType(String type) {
        _type = type;
    }

    // ----------------------------------------------------- Instance Variables

    /**
     * The persistence type specifier.
     */
    private String _type = null;

    // ------------------------------------------------------------- Properties
    
    /**
     * Returns a string describing the persistence mechanism that the
     * object represents.
     */
    public String getType() {
        return _type;
    }

    // --------------------------------------------------------- Static Methods

    /**
     * Parse the specified string and return the corresponding instance
     * of this class that represents the persistence type specified
     * in the string.
     */
    public static PersistenceType parseType(String type) {
        // Default persistence type is MEMORY
        PersistenceType pType = MEMORY;
        if (type != null) {
            if (type.equalsIgnoreCase(FILE.getType()))
                pType = FILE;
            else if (type.equalsIgnoreCase(CUSTOM.getType()))
                pType = CUSTOM;
            else if (type.equalsIgnoreCase(S1WS60.getType()))
                pType = S1WS60;
            else if (type.equalsIgnoreCase(MMAP.getType()))
                pType = MMAP;
            else if (type.equalsIgnoreCase(JDBC.getType()))
                pType = JDBC;            
            else if (type.equalsIgnoreCase(HA.getType()))
                pType = HA;
            else if (type.equalsIgnoreCase(REPLICATED.getType()))
                pType = REPLICATED; 
        }
        return pType;
    }
    
    /**
     * Parse the specified string and return the corresponding instance
     * of this class that represents the persistence type specified
     * in the string.  Default back into passed-in parameter
     */
    public static PersistenceType parseType(String type, PersistenceType defaultType) {
        // Default persistence type is defaultTypee
        PersistenceType pType = defaultType;
        if (type != null) {
            if (type.equalsIgnoreCase(MEMORY.getType()))
                pType = MEMORY;            
            else if (type.equalsIgnoreCase(FILE.getType()))
                pType = FILE;
            else if (type.equalsIgnoreCase(CUSTOM.getType()))
                pType = CUSTOM;
            else if (type.equalsIgnoreCase(S1WS60.getType()))
                pType = S1WS60;
            else if (type.equalsIgnoreCase(MMAP.getType()))
                pType = MMAP;
            else if (type.equalsIgnoreCase(JDBC.getType()))
                pType = JDBC;            
            else if (type.equalsIgnoreCase(HA.getType()))
                pType = HA;
            else if (type.equalsIgnoreCase(REPLICATED.getType()))
                pType = REPLICATED;    
        }
        return pType;
    }    

}

