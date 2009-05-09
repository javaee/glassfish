/*
 * The contents of this file are subject to the terms 
 * of the Common Development and Distribution License 
 * (the License).  You may not use this file except in
 * compliance with the License.
 * 
 * You can obtain a copy of the license at 
 * https://glassfish.dev.java.net/public/CDDLv1.0.html or
 * glassfish/bootstrap/legal/CDDLv1.0.txt.
 * See the License for the specific language governing 
 * permissions and limitations under the License.
 * 
 * When distributing Covered Code, include this CDDL 
 * Header Notice in each file and include the License file 
 * at glassfish/bootstrap/legal/CDDLv1.0.txt.  
 * If applicable, add the following below the CDDL Header, 
 * with the fields enclosed by brackets [] replaced by
 * you own identifying information: 
 * "Portions Copyrighted [year] [name of copyright owner]"
 * 
 * Copyright 2006 Sun Microsystems, Inc. All rights reserved.
 */
package com.sun.enterprise.deployment;


/** Objects exhiniting this interface represent an error page and the exception type or
** error code that will cause the redirect from the web container. 
* @author Danny Coward
*/

public class ErrorPageDescriptor implements java.io.Serializable{
    private int errorCode = -1;  // none
    private String exceptionType;
    private String location;
    
    /** The default constructor.
    */
    public ErrorPageDescriptor() {
    
    }
    
    /** Constructor for error code to error page mapping.*/
    public ErrorPageDescriptor(int errorCode, String location) {
	this.errorCode = errorCode;
	this.location = location;
    }
    
     /** Constructor for Java exception type to error page mapping.*/
    public ErrorPageDescriptor(String exceptionType, String location) {
	this.exceptionType = exceptionType;
	this.location = location;
    }
	/** Return the error code. -1 if none. */
    public int getErrorCode() {
	return this.errorCode;
    }
	/** Sets the error code.*/
    public void setErrorCode(int errorCode) {
	this.errorCode = errorCode;
    }
	/**
     *  If there is an exception type, then the exception type is returned.
     *  Otherwise, if the error code is not -1, then the error code is returned as a string.
     *  If the error code is -1, then nul is returned.
     */
    public String getErrorSignifierAsString() {
        if ("".equals(this.getExceptionType())) {
           if (getErrorCode() == -1) {
               return null;
           } else {
               return String.valueOf(this.getErrorCode());
           }
        }
        return this.getExceptionType();
    }
    /**Sets the error code if the argument is parsable as an int, or the exception type else.*/
    public void setErrorSignifierAsString(String errorSignifier) {
	try {
	    int errorCode = Integer.parseInt(errorSignifier);
	    this.setErrorCode(errorCode);
	    this.setExceptionType(null);
	    return;
	} catch (NumberFormatException nfe) {
	
	}
	this.setExceptionType(errorSignifier);
    }
    
    /** Return the exception type or the empty string if none.*/
    public String getExceptionType() {
	if (this.exceptionType == null) {
	    this.exceptionType = "";
	}
	return this.exceptionType;
    }
    
    /** Sets the exception type.*/
    public void setExceptionType(String exceptionType) {
	this.exceptionType = exceptionType;
    }
    /** Return the page to map to */
    public String getLocation() {
	if (this.location == null) {
	    this.location = "";
	}
	return this.location;
    }
    /* Set the page to map to */
    public void setLocation(String location) {
	this.location = location;
    }
    /* A formatted version of my state as a String. */
    public void print(StringBuffer toStringBuffer) {
	toStringBuffer.append("ErrorPage ").append(this.getErrorCode()).append(" ").append(
            this.getExceptionType()).append(" ").append(this.getLocation());
    }

}

