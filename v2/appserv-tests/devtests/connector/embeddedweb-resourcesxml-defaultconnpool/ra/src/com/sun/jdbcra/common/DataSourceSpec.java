/*
 * Copyright 2003 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
 
package com.sun.jdbcra.common;

import java.util.Hashtable;

/**
 * Encapsulate the DataSource object details obtained from 
 * ManagedConnectionFactory.
 *
 * @version	1.0, 02/07/23
 * @author	Binod P.G
 */
public class DataSourceSpec implements java.io.Serializable{

    public static final int USERNAME				= 1;    
    public static final int PASSWORD				= 2;  
    public static final int URL					= 3;  
    public static final int LOGINTIMEOUT			= 4;
    public static final int LOGWRITER				= 5;
    public static final int DATABASENAME			= 6;
    public static final int DATASOURCENAME			= 7;
    public static final int DESCRIPTION				= 8;
    public static final int NETWORKPROTOCOL			= 9;
    public static final int PORTNUMBER				= 10;
    public static final int ROLENAME				= 11;
    public static final int SERVERNAME				= 12;
    public static final int MAXSTATEMENTS			= 13;
    public static final int INITIALPOOLSIZE			= 14;
    public static final int MINPOOLSIZE				= 15;
    public static final int MAXPOOLSIZE				= 16;
    public static final int MAXIDLETIME				= 17;
    public static final int PROPERTYCYCLE			= 18;
    public static final int DRIVERPROPERTIES			= 19;    
    public static final int CLASSNAME				= 20;
    public static final int DELIMITER				= 21;
    
    public static final int XADATASOURCE			= 22;
    public static final int DATASOURCE				= 23;
    public static final int CONNECTIONPOOLDATASOURCE		= 24;
      
    //GJCINT      
    public static final int CONNECTIONVALIDATIONREQUIRED	= 25;
    public static final int VALIDATIONMETHOD			= 26;
    public static final int VALIDATIONTABLENAME			= 27;
    
    public static final int TRANSACTIONISOLATION		= 28;
    public static final int GUARANTEEISOLATIONLEVEL		= 29;
            
    private Hashtable details = new Hashtable();
    
    /**
     * Set the property.
     *
     * @param	property	Property Name to be set.
     * @param	value		Value of property to be set.
     */
    public void setDetail(int property, String value) {
    	details.put(new Integer(property),value);
    }
    
    /**
     * Get the value of property
     *
     * @return	Value of the property.
     */
    public String getDetail(int property) {
    	if (details.containsKey(new Integer(property))) {
    	    return (String) details.get(new Integer(property));
    	} else {
    	    return null;
    	}    	
    }
    
    /**
     * Checks whether two <code>DataSourceSpec</code> objects
     * are equal or not.
     *
     * @param	obj	Instance of <code>DataSourceSpec</code> object.
     */
    public boolean equals(Object obj) {
    	if (obj instanceof DataSourceSpec) {
    	    return this.details.equals(((DataSourceSpec)obj).details);
    	}
    	return false;
    }
    
    /**
     * Retrieves the hashCode of this <code>DataSourceSpec</code> object.
     *
     * @return	hashCode of this object.
     */
    public int hashCode() {
    	return this.details.hashCode();
    }    
}
