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
package com.sun.enterprise.util;

import java.util.Hashtable;
import java.util.Vector;

//START OF IASRI 4660742
import java.util.logging.*;
import com.sun.logging.*;
//END OF IASRI 4660742

public abstract class ResourceCache 
{

    // START OF IASRI 4660742
    static Logger _logger=LogDomains.getLogger(LogDomains.UTIL_LOGGER);
    // END OF IASRI 4660742

    /**
     *
     */
    public ResourceCache(int count)
    {
	resources = new Hashtable();
	this.count = count;
    }

    /**
     * @exception Exception  on exception
     */
    public Object getResource(Object name) throws Exception
    {
	Object resource = null;
	Vector list     = null;
	Resource r      = null;

	// get the list of resources
	synchronized (resources) {

	    list = (Vector) resources.get(name);

	    if (list == null) {
		list = new Vector(count);
	    	resources.put(name, list);
	    }
	}

	// check for available resource
	boolean newR = false; // this thread needs to create a new resource
	synchronized (list) {

	    while (resource == null) {

	        // try to find an available resource
	        for (int i=0; i < list.size(); i++) {
		    if ((r = (Resource) list.elementAt(i)).isAvailable()) {
		        r.markInUse();
		        resource = r.getResource();
			break;
		    }
	        }
    
	        // create new resource if limit not reached
	        if (resource == null) {
		    if (list.size() < count) {
		        r = new Resource();
		        list.addElement(r);
		        newR = true;
			break;
		    }

		    // wait for the resource to free up
		    if (!newR) list.wait();
		}
	    }
	}

	// if new resource created, initialize the resource
	if (newR) {

	    try {
	    	resource = createResource(name);

	        // XXX need to handle case when resource is null

	    } catch (Exception ex) {
		
                /** IASRI 4660742
                ex.printStackTrace(); 
                **/
		            // START OF IASRI 4660742
                _logger.log(Level.SEVERE,"enterprise_util.excep_rescache_getres",ex);
                // END OF IASRI 4660742
	    }

	    synchronized (list) {
	        r.setResource(resource);
	        r.markInUse();
	    }
	}

	return resource;
    }

    /**
     *
     */
    public void returnResource(Object name, Object oldResource, 
	Object newResource)
    {
	Resource r = null;
	Vector list = (Vector) resources.get(name);

	if (list != null) {
	    synchronized (list) {
	        for (int i=0; i < list.size(); i++) {
		    r = (Resource) list.elementAt(i);
		    if (r.getResource() == oldResource) {
			r.setResource(newResource);
		        r.markAvailable();
			list.notifyAll();
			break;
		    }
	        }
	    }
	} 
    }

    /**
     * Typically a long operation.
     *
     * @exception Exception on exception
     */
    public abstract Object createResource(Object name) throws Exception;

    private Hashtable resources;
    private int count;
}

/**
 *
 */
class Resource
{
    private static final int IN_USE=1;
    private static final int AVAILABLE=2;
    private static final int NOT_AVAILABLE=3;

    private Object resource;
    private int    state;

    /**
     *
     */
    Resource() 
    {
	resource = null;
	state    = NOT_AVAILABLE;
    }

    /**
     *
     */
    Object getResource()
    {
	return resource;
    }

    /**
     *
     */
    void setResource(Object resource)
    {
	this.resource = resource;
    }

    /**
     *
     */
    void markAvailable()
    {
	state = AVAILABLE;
    }

    /**
     *
     */
    void markInUse()
    {
	state = IN_USE;
    }

    /**
     *
     */
    boolean isAvailable()
    {
	return (state == AVAILABLE) ? true : false;
    }
}
