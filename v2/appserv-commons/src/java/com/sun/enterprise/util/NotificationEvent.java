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

import java.util.EventObject;
import java.util.Hashtable;

    /** Generic event for notifications.
    * @author Danny Coward
    */

public class NotificationEvent extends EventObject {

    public static final String OBJECT_THAT_CHANGED = "ObjectThatChanged";
    public static final String ATTRIBUTE_THAT_CHANGED = "AttributeThatChanged";

    private String type = null;
    protected Hashtable properties = new Hashtable();
    
    public NotificationEvent(Object source, String type) {
	super(source);
	this.type = type;
    }
    
    public NotificationEvent(Object source, String type, String name, Object value) {
	super(source);
	this.type = type;
	this.properties.put(name, value);
    }
    
    public NotificationEvent(Object source, String type, Object objectThatChanged) {
	super(source);
	this.type = type;
	this.properties.put(OBJECT_THAT_CHANGED, objectThatChanged);
    }
        
    public NotificationEvent(Object source, String type, Object objectThatChanged, Object attribThatChanged) {
	this(source, type, objectThatChanged);
	if (attribThatChanged != null) {
	    this.properties.put(ATTRIBUTE_THAT_CHANGED, attribThatChanged);
	}
    }

    public String getType() {
	return this.type;
    }
    
    public Object getValue(String name) {
	return this.properties.get(name);
    }
    
    public Object getObjectThatChanged() {
	return this.properties.get(OBJECT_THAT_CHANGED);
    }
        
    public Object getAttributeThatChanged() {
	return this.properties.get(ATTRIBUTE_THAT_CHANGED);
    }

}
