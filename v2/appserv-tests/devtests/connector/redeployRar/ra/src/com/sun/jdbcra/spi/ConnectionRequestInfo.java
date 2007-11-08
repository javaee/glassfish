/*
 * Copyright 2003 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
 
package com.sun.jdbcra.spi;

/**
 * ConnectionRequestInfo implementation for Generic JDBC Connector.
 *
 * @version	1.0, 02/07/31
 * @author	Binod P.G
 */
public class ConnectionRequestInfo implements javax.resource.spi.ConnectionRequestInfo{ 

    private String user;
    private String password;

    /**
     * Constructs a new <code>ConnectionRequestInfo</code> object
     *
     * @param	user	User Name.
     * @param	password	Password
     */
    public ConnectionRequestInfo(String user, String password) {
        this.user = user;
        this.password = password;
    }

    /**
     * Retrieves the user name of the ConnectionRequestInfo.
     *
     * @return	User name of ConnectionRequestInfo.
     */
    public String getUser() {
        return user;
    }

    /**
     * Retrieves the password of the ConnectionRequestInfo.
     *
     * @return	Password of ConnectionRequestInfo.
     */
    public String getPassword() {
        return password;
    }

    /**
     * Verify whether two ConnectionRequestInfos are equal.
     *
     * @return	True, if they are equal and false otherwise.
     */
    public boolean equals(Object obj) {
        if (obj == null) return false;
        if (obj instanceof ConnectionRequestInfo) {
            ConnectionRequestInfo other = (ConnectionRequestInfo) obj;
            return (isEqual(this.user, other.user) &&
                    isEqual(this.password, other.password));
        } else {
            return false;
        }
    }

    /**
     * Retrieves the hashcode of the object.
     *
     * @return	hashCode.
     */
    public int hashCode() {
        String result = "" + user + password;
        return result.hashCode();
    }
    
    /**
     * Compares two objects.
     *
     * @param	o1	First object.
     * @param	o2	Second object.
     */
    private boolean isEqual(Object o1, Object o2) {
        if (o1 == null) {
            return (o2 == null);
        } else {
            return o1.equals(o2);
        }
    }

}
