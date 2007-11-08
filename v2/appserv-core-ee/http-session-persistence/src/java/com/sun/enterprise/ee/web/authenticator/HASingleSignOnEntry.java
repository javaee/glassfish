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

/*
 * HASingleSignOnEntry.java
 *
 * Created on January 22, 2003, 10:25 AM
 * @Author Sridhar Satuloori
 */

package com.sun.enterprise.ee.web.authenticator;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

import java.security.Principal;

import com.sun.enterprise.security.web.SingleSignOnEntry;
//import org.apache.catalina.authenticator.SingleSignOnEntry;


public class HASingleSignOnEntry extends SingleSignOnEntry {
   
    public String sessionIds[] = new String[0];
    public boolean dirty = false;
    private long version = 0L;

    /*    
    public HASingleSignOnEntry(Principal principal, String authType, String username, String password) {

       super(principal,authType,username,password);
    }
     */
    
    public HASingleSignOnEntry(Principal principal, String authType, String username, String password, String realmName) {

       super(principal,authType,username,password, realmName);
    }    

    public synchronized void addSessionId( String sessionId) {
        for (int i = 0; i < sessionIds.length; i++) {
            if (sessionId.equals(sessionIds[i]))
                return; //session is in memory
        }
        String results[] = new String[sessions.length + 1];
        System.arraycopy(sessions, 0, results, 0, sessions.length);
        results[sessions.length] = sessionId;
        sessionIds = results;
    }
    
    /**
     * Write a serialized version of the contents of this session object to
     * the specified object output stream, without requiring that the
     * StandardSession itself have been serialized.
     *
     * @param stream The object output stream to write to
     *
     * @exception IOException if an input/output error occurs
     */
    public void writeObjectData(ObjectOutputStream stream)
        throws IOException {

        writeObject(stream);

    }
    
    /**
     * Write a serialized version of this ssoEntry object to the specified
     * object output stream.
     *
     * @param stream The output stream to write to
     *
     * @exception IOException if an input/output error occurs
     */
    private void writeObject(ObjectOutputStream stream) throws IOException {

        // Write the scalar instance variables
        stream.writeObject(authType);
        stream.writeObject(username);
        stream.writeObject(password);
        stream.writeObject(realmName);
        stream.writeObject(new Long(lastAccessTime));        

        // Write principal if it is serializable else null
        Principal pal = null;
        if (this.principal != null && (this.principal instanceof Serializable)) {
            pal = this.principal;
        }
        stream.writeObject(pal);
        
    } 
    
    /**
     * Read a serialized version of the contents of this session object from
     * the specified object input stream, without requiring that the
     * SingleSignOnEntry itself have been serialized.
     *
     * @param stream The object input stream to read from
     *
     * @exception ClassNotFoundException if an unknown class is specified
     * @exception IOException if an input/output error occurs
     */
    public void readObjectData(ObjectInputStream stream)
        throws ClassNotFoundException, IOException {

        readObject(stream);

    } 
    
    /**
     * Read a serialized version of this sso entry object from the specified
     * object input stream.
     *
     * @param stream The input stream to read from
     *
     * @exception ClassNotFoundException if an unknown class is specified
     * @exception IOException if an input/output error occurs
     */
    private void readObject(ObjectInputStream stream)
        throws ClassNotFoundException, IOException {

        // Deserialize the scalar instance variables       
        authType = (String)stream.readObject();
        username = (String)stream.readObject();
        password = (String)stream.readObject();
        realmName = (String)stream.readObject();
        lastAccessTime = ((Long) stream.readObject()).longValue();
        
        // read principal if it is serializable else it will be null
        this.principal = (Principal)stream.readObject();       

    } 
    
    /** 
     * this increments the version number
     */    
    public void incrementVersion() {
        version++;
    } 
    
    /** 
     * this decrements the version number
     */    
    public void decrementVersion() {
        version--;
    } 
    
    /** 
     * this returns the version number
     */    
    public long getVersion() {
        return version;
    }
    
    /** 
     * this sets the version number
     */    
    public void setVersion(long value) {
        version = value;
    }     

    public String toString(){
	String str = "HASSOEntry:: principal ="+principal+" authType ="+authType+" username ="+username+"  password ="+password;
	return str;
    }
}
