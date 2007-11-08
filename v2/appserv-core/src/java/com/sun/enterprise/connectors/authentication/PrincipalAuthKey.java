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

package com.sun.enterprise.connectors.authentication;

import com.sun.logging.LogDomains;

import java.util.logging.*;
import java.io.Serializable;


/**
 * This class represents the key for security Maps.
 * This is used as key in storing the backend principals as value in the
 * security map in ConnectorRegistry
 * @author    Srikanth P
 */

public class PrincipalAuthKey implements Serializable {

    private String rarName_ = null;
    private String poolName_ = null;
    private String principalName_ = null;
    private String userGroup_ = null;

    static Logger _logger = LogDomains.getLogger(LogDomains.RSR_LOGGER);

    /**
     * Constructor
     * @param rarName  Name of the rar
     * @param poolName Nmae of the pool
     * @param principalName Name of the principal to which security mapping
     *                      is to be done.
     * @param userGroupName  Name of the userGroup to which security mapping
     *                      is to be done.
     */

    public PrincipalAuthKey(String rarName,String poolName,
                        String principalName,String userGroupName){
        rarName_ = rarName;
        poolName_ = poolName;
        principalName_ = principalName;
        userGroup_ = userGroupName;
        _logger.log(Level.FINE,"Constructor: PrincipalAuthKey");
    }

    /**
     *  Generates Hashcode . Overloaded method from "Object" class.
     *  @return hashCode
     */

    public int hashCode() {
        return (rarName_+poolName_+principalName_+userGroup_).hashCode();
    }

    /** Checks whether two strings are equal including the null string 
     *  cases.
     */

    private boolean isEqual(String in, String out) {
        if(in == null && out == null) {
            return true;
        }
        if(in == null || out == null) {
            return false;
        }
        return (out.equals(in));
    }

    /**
     *  Checks for equality.
     *  Overloaded equals()  from Object class. Used when comparing strings 
     *  for equality.
     *  @param other Object to compare
     *  @return true if the objects are equal
     *          false if not so.
     */

    public boolean equals(Object other) {
        if( other == null || !(other instanceof PrincipalAuthKey)) {
            if ( _logger.isLoggable(Level.FINE) ) {
                String msg = "equals method PrincipalAuthKey: parameter not ";
                String msg1= "PrincipalAuthKey. Equals fails";
                _logger.log(Level.FINE,msg+msg1);
            }
	    return false;
        }
        PrincipalAuthKey otherkey = (PrincipalAuthKey)other;

        if(isEqual(this.rarName_,otherkey.rarName_)   && 
                 isEqual(this.poolName_,otherkey.poolName_) && 
                 isEqual(this.principalName_,otherkey.principalName_) &&
                 isEqual(this.userGroup_,otherkey.userGroup_)) {
            _logger.log(Level.FINE,"equal method of PrincipalAuthKey succeeds"); 
            return true;
        } else {
            _logger.log(Level.FINE,"equals method of PrincipalAuthKey fails"); 
            return false;
        }
    }
}
