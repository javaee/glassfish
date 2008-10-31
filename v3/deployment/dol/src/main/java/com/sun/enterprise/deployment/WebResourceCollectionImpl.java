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
package com.sun.enterprise.deployment;

import com.sun.enterprise.deployment.web.WebResourceCollection;

import java.util.Enumeration;
import java.util.HashSet;
import java.util.Set;
import java.util.Vector;

/** 
 * This descriptor represents a description of a portion of a web app
 * in terms of a collection of url patterns and 
 * a collection of http methods on this patterns.
 *
 *@author Danny Coward
 */


public class WebResourceCollectionImpl extends Descriptor 
				implements WebResourceCollection 
{
    private Set urlPatterns;
    private Set httpMethods;
    
    /** 
     * Return my urls patterns (String objects)
     * @return the enumeration of the url patterns.
     */
    public Enumeration getUrlPatterns() {
	return (new Vector(this.getUrlPatternsSet())).elements();
    }
    
    /**
     * Add a URL pattern to this collection.
     * @param the url pattern to be added.
     */
    public void addUrlPattern(String urlPattern) {
	this.getUrlPatternsSet().add(urlPattern);
    }
    
    /**
     * Remove the specified url pattern from the collection.
     * @param the url pattern to be removed.
     */
    public void removeUrlPattern(String urlPattern) {
	this.getUrlPatternsSet().remove(urlPattern);
    }
    
    /**
     * Clean out the collection of URL pattern and replace
     * it with the given Set of (String) url patterns.
     * @param the url patterns to replace the current set. 
     */
    public void setUrlPatterns(Set urlPatterns) {
	this.urlPatterns = urlPatterns;
    }
    
    private Set getHttpMethodsSet() {
	if (this.httpMethods == null) {
	    this.httpMethods = new HashSet();
	}
	return this.httpMethods;
    }
    
    /**
     * Return the enumeration of HTTP methods this collection has.
     * @return the enumeration of HTTP methods.
     */
    public Enumeration getHttpMethods() {
	return (new Vector(this.getHttpMethodsSet())).elements();
    }
    /** 
     * Returns the HttpMethods this collection has in an array of strings
     * @return array of strings of HttpMethods
     */
    /*
     * added to speed up processing while creating webresource permissions
     */
    public String[] getHttpMethodsAsArray(){
	if(httpMethods == null){
	    return (String[]) null;
	}
	String[] array = (String[])httpMethods.toArray(new String[0]);
	return array;
    }
    /**
     * Sets the set of HTTP methods this collection has.
     * @param the set of HTTP methods.
     */
    public void setHttpMethods(Set httpMethods) {
	this.httpMethods = httpMethods;
    }

    /**
     * Adds the given HTTP method to the collection of http methods this
     * collection has.
     * @param the HTTP method to be added.
     */
    public void addHttpMethod(String httpMethod) {
	this.getHttpMethodsSet().add(httpMethod);
    }
    
    /**
     * Removes the given HTTP method from the collection of http methods.
     * @param the HTTP method to be removed.
     */
    public void removeHttpMethod(String httpMethod) {
	this.getHttpMethodsSet().remove(httpMethod);    
    }
    
    /**
     * A formatted string of the state.
     */
    public void print(StringBuffer toStringBuffer) {
	toStringBuffer.append("WebresourceCollection: ");
	toStringBuffer.append(" urlPatterns: ").append(this.urlPatterns);
	toStringBuffer.append(" httpMethods ").append(this.httpMethods);
    }

    private Set getUrlPatternsSet() {
	if (this.urlPatterns == null) {
	    this.urlPatterns = new HashSet();
	}
	return this.urlPatterns;
    }

}

