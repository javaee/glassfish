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
package com.sun.enterprise.repository;

/**
 * A Configuration object stores all the properties that are needed
 * by various components within the EJB server.    
 * @author Harish Prabandham
 */
public interface Configuration extends java.rmi.Remote {
    /**
     * This method gets a property value associated with the given key.
     * @param The key for the property.
     * @return A property value corresponding to the key
     */
    public String getProperty(String key) throws java.rmi.RemoteException;
    
    /**
     * This method associates a property value with the given key.
     * @param The key for the property.
     * @param The value for the property.
     */
    public void setProperty(String key, String value) 
	throws java.rmi.RemoteException;
    
    /**
     * This method removes value corresponding to the key.
     * @param The key for the property.     
     */
    public void removeProperty(String key) throws java.rmi.RemoteException;
    
    /**
     * This method gets an Object associated with the given key.
     * @param The key for the property.     
     * @return An Object corresponding to the key
     */
    public Object getObject(String key) throws java.rmi.RemoteException;
    
    /**
     * This method associates an Object with the given key. The Object
     * must implement Serializable interface.
     * @param The key for the property.     
     * @param The object to be stored.
     */
    public void setObject(String key, Object obj) 
	throws java.rmi.RemoteException;
    
    /**
     * This method removes an Object with the given key.
     * @param The key for the property.     
     */
    public void removeObject(String key) throws java.rmi.RemoteException;
    
    
    /**
     * This method returns all the keys for the given index. The index is
     * name before the first . for a given property. For example. 
     * If there's a property called http.server.port. The index name for 
     * this property is http
     * @param The index name of the repository, 
     */
    public String[] getKeys(String index) throws java.rmi.RemoteException;
}

