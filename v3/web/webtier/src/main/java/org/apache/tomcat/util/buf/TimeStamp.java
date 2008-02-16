

/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
 * 
 * Portions Copyright Apache Software Foundation.
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
package org.apache.tomcat.util.buf;

import org.apache.tomcat.util.buf.MessageBytes;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;

// XXX Shouldn't be here - has nothing to do with buffers.

/**
 * Main tool for object expiry. 
 * Marks creation and access time of an "expirable" object,
 * and extra properties like "id", "valid", etc.
 *
 * Used for objects that expire - originally Sessions, but 
 * also Contexts, Servlets, cache - or any other object that
 * expires.
 * 
 * @author Costin Manolache
 */
public final class TimeStamp implements  Serializable {
    private long creationTime = 0L;
    private long lastAccessedTime = creationTime;
    private long thisAccessedTime = creationTime;
    private boolean isNew = true;
    private long maxInactiveInterval = -1;
    private boolean isValid = false;
    MessageBytes name;
    int id=-1;
    
    Object parent;
    
    public TimeStamp() {
    }

    // -------------------- Active methods --------------------

    /**
     *  Access notification. This method takes a time parameter in order
     *  to allow callers to efficiently manage expensive calls to
     *  System.currentTimeMillis() 
     */
    public void touch(long time) {
	this.lastAccessedTime = this.thisAccessedTime;
	this.thisAccessedTime = time;
	this.isNew=false;
    }

    // -------------------- Property access --------------------

    /** Return the "name" of the timestamp. This can be used
     *  to associate unique identifier with each timestamped object.
     *  The name is a MessageBytes - i.e. a modifiable byte[] or char[]. 
     */
    public MessageBytes getName() {
	if( name==null ) name=MessageBytes.newInstance();//lazy
	return name;
    }

    /** Each object can have an unique id, similar with name but
     *  providing faster access ( array vs. hashtable lookup )
     */
    public int getId() {
	return id;
    }

    public void setId( int id ) {
	this.id=id;
    }
    
    /** Returns the owner of this stamp ( the object that is
     *  time-stamped ).
     *  For a 
     */
    public void setParent( Object o ) {
	parent=o;
    }

    public Object getParent() {
	return parent;
    }

    public void setCreationTime(long time) {
	this.creationTime = time;
	this.lastAccessedTime = time;
	this.thisAccessedTime = time;
    }


    public long getLastAccessedTime() {
	return lastAccessedTime;
    }

    /** Inactive interval in millis - the time is computed
     *  in millis, convert to secs in the upper layer
     */
    public long getMaxInactiveInterval() {
	return maxInactiveInterval;
    }

    public void setMaxInactiveInterval(long interval) {
	maxInactiveInterval = interval;
    }

    public boolean isValid() {
	return isValid;
    }

    public void setValid(boolean isValid) {
	this.isValid = isValid;
    }

    public boolean isNew() {
	return isNew;
    }

    public void setNew(boolean isNew) {
	this.isNew = isNew;
    }

    public long getCreationTime() {
	return creationTime;
    }

    // -------------------- Maintainance --------------------

    public void recycle() {
	creationTime = 0L;
	lastAccessedTime = 0L;
	maxInactiveInterval = -1;
	isNew = true;
	isValid = false;
	id=-1;
	if( name!=null) name.recycle();
    }

}

