/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * // Copyright (c) 1998, 2007, Oracle. All rights reserved.
 * 
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
package oracle.toplink.essentials.threetier;

import java.util.*;
import java.io.*;
import oracle.toplink.essentials.internal.helper.*;
import oracle.toplink.essentials.sessions.Login;
import oracle.toplink.essentials.internal.localization.*;

/**
 * <p>
 * <b>Purpose</b>: Used to specify how a client session's should be allocated.
 * @see ServerSession
 */
public class ConnectionPolicy implements Cloneable, Serializable {
    protected Login login;
    protected String poolName;
    protected boolean isLazy;

    /** This attribute provides a mechanism to pass connection information to events. */
    protected Map properties;

    /**
     * PUBLIC:
     * A connection policy is used to define how the client session connection should be acquired.
     */
    public ConnectionPolicy() {
        this.isLazy = true;
    }

    /**
     * PUBLIC:
     * A connection policy is used to define how the client session connection should be acquired.
     */
    public ConnectionPolicy(String poolName) {
        this.isLazy = true;
        this.poolName = poolName;
    }

    /**
     * PUBLIC:
     * A connection policy is used to define how the client session connection should be acquired.
     */
    public ConnectionPolicy(Login login) {
        this.isLazy = false;
        this.login = login;
    }

    /**
     * INTERNAL:
     * Clone the query
     */
    public Object clone() {
        try {
            ConnectionPolicy clone = (ConnectionPolicy)super.clone();
            if (clone.hasLogin()) {
                clone.setLogin((Login)clone.getLogin().clone());
            }
            return clone;
        } catch (CloneNotSupportedException e) {
            return null;
        }
    }

    /**
     * PUBLIC:
     * A lazy connection only acquires a physical connection
     * when a transaction is started and releases the connection when the transaction completes.
     */
    public void dontUseLazyConnection() {
        setIsLazy(false);
    }

    /**
     * PUBLIC:
     * Return the login to use for this connection.
     * Client sessions support using a seperate user login for database modification.
     */
    public Login getLogin() {
        return login;
    }

    /**
     * PUBLIC:
     * Return the pool name or null if not part of a pool.
     */
    public String getPoolName() {
        return poolName;
    }

    /**
     * ADVANCED:
     * This method will return the collection of custom properties set on the Connection
     * policy.  Note that this will cause the lazy initialization of the HashMap.
     */
    public Map getProperties() {
        if (this.properties == null) {
            this.properties = new HashMap();
        }
        return this.properties;
    }

    /**
     * PUBLIC:
     * Returns the property associated with the corresponding key.  These properties will be available to
     * connection events.
     */
    public Object getProperty(Object object) {
        if (this.hasProperties()) {
            return this.getProperties().get(object);
        }
        return null;
    }

    /**
     * PUBLIC:
     * Return if a login is used, only one of login and pool can be used.
     */
    public boolean hasLogin() {
        return login != null;
    }

    /**
     * PUBLIC:
     * Returns true if properties are available on the Connection Policy
     */
    public boolean hasProperties() {
        return (this.properties != null) && (!this.properties.isEmpty());
    }

    /**
     * PUBLIC:
     * Return if a lazy connection should be used, a lazy connection only acquire a physical connection
     * when a transaction is started and releases the connection when the transaction completes.
     */
    public boolean isLazy() {
        return isLazy;
    }

    /**
     * INTERNAL:
     * Return if part of a connection pool.
     */
    public boolean isPooled() {
        return poolName != null;
    }

    /**
     * INTERNAL:
     * Return if part of a connection pool.
     */
    public boolean isUserDefinedConnection() {
        return poolName == null;
    }

    /**
     * PUBLIC:
     * This method is used to remove a custom property from the Connection Policy.
     * This method will return the propery removed.  If it was not found then null
     * will be returned.
     */
    public Object removeProperty(Object key) {
        if (this.hasProperties()) {
            return getProperties().remove(key);
        }
        return null;
    }

    /**
     * PUBLIC:
     * Set if a lazy connection should be used, a lazy connection only acquire a physical connection
     * when a transaction is started and releases the connection when the transaction completes.
     */
    public void setIsLazy(boolean isLazy) {
        this.isLazy = isLazy;
    }

    /**
     * PUBLIC:
     * Set the login to use for this connection.
     * Client sessions support using a seperate user login for database modification.
     * Pooled connections must use the pool's login and cannot define their own.
     */
    public void setLogin(Login login) {
        this.login = login;
    }

    /**
     * PUBLIC:
     * Set the pool name or null if not part of a pool.
     */
    public void setPoolName(String poolName) {
        this.poolName = poolName;
    }

    /**
     * PUBLIC:
     * Use this method to set custom properties on the Connection Policy.  These
     * properties will be available from within connection events but have no
     * effect on the connection directly.
     */
    public void setProperty(Object key, Object property) {
        getProperties().put(key, property);
    }

    /**
     * INTERNAL:
     * return a string representation of this ConnectionPolicy
     */
    public String toString() {
        String type = "";
        if (isPooled()) {
            type = "(" + ToStringLocalization.buildMessage("pooled", (Object[])null) + ": " + getPoolName();
        } else {
            type = "(" + ToStringLocalization.buildMessage("login", (Object[])null) + ": " + getLogin();
        }
        if (isLazy()) {
            type = type + "," + ToStringLocalization.buildMessage("lazy", (Object[])null) + ")";
        } else {
            type = type + "," + ToStringLocalization.buildMessage("non-lazy", (Object[])null) + ")";
        }

        return Helper.getShortClassName(getClass()) + type;
    }

    /**
     * PUBLIC:
     * A lazy connection only acquires a physical connection
     * when a transaction is started and releases the connection when the transaction completes.
     */
    public void useLazyConnection() {
        setIsLazy(true);
    }
}
