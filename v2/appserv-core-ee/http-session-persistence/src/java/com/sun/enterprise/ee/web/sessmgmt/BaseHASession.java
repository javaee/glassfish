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
 * BaseHASession.java
 *
 * Created on October 23, 2003, 11:20 AM
 */

package com.sun.enterprise.ee.web.sessmgmt;

import javax.servlet.http.HttpSession;
import org.apache.catalina.Manager;
import org.apache.catalina.session.StandardSession;
import javax.servlet.http.HttpSessionBindingEvent;
import org.apache.catalina.core.StandardContext;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.ObjectInputStream;
import java.security.Principal;
import java.util.Enumeration;

/**
 *
 * @author  lwhite
 */
public class BaseHASession extends StandardSession implements HASession {
    
    protected String ssoId="";
    protected String userName="";
    protected boolean persistentFlag = false;
    
    /** Creates a new instance of BaseHASession */
    public BaseHASession(Manager manager) {
        super(manager);
    }
    
    /** return the ssoId */
    public String getSsoId(){
	return ssoId;
    }
    
    /** set the ssoId 
     * @param ssoId
     */
    public void setSsoId(String ssoId){
	this.ssoId = ssoId;
    }
    
    /**
     * always return true for isDirty()
     * this type of session is always dirty
     */
    public boolean isDirty() {
        return true;
    }

    /** 
     * this is deliberately a no-op
     * store framework calls this method
     * so it must be there but must not have
     * any effect
     * @param value
     */    
    public void setDirty(boolean value) {
        //this is deliberately a no-op
    } 
        
    /** 
     * is the session persistent
     */    
    public boolean isPersistent() {
        return persistentFlag;
    }
    
    /** 
     * this sets the persistent flag
     */    
    public void setPersistent(boolean value) {
        persistentFlag = value;
    }    
    
    /** 
     * this returns the user name
     */    
    public String getUserName() {
        return userName;
    }
    
    /** 
     * this sets the user name
     */    
    public void setUserName(String value) {
        userName = value;
    }
    
    /**
     * Overriding the setPrincipal of StandardSession
     *
     * @param principal The new Principal, or <code>null</code> if none
     */
    public void setPrincipal(Principal principal) {
        super.setPrincipal(principal);
        this.setDirty(true);
    }   

    public boolean isPersistentFlag() {
        return persistentFlag;
    }
    
    public void recycle() {
        super.recycle();
        userName = "";
        ssoId = "";
        persistentFlag = false;
    }
    
    public void sync() {
        
        HttpSessionBindingEvent event = null;
        event = new HttpSessionBindingEvent
                ((HttpSession) this, null, null);       
        
        // Notify special event listeners on sync()
        Manager manager = this.getManager();
        StandardContext stdContext = (StandardContext) manager.getContainer();        
        // fire container event       
        stdContext.fireContainerEvent("sessionSync", event);        
    }
    
    /**
     * Read a serialized version of this session object from the specified
     * object input stream.
     * <p>
     * <b>IMPLEMENTATION NOTE</b>:  The reference to the owning Manager
     * is not restored by this method, and must be set explicitly.
     *
     * @param stream The input stream to read from
     *
     * @exception ClassNotFoundException if an unknown class is specified
     * @exception IOException if an input/output error occurs
     */
    private void readObject(ObjectInputStream stream)
        throws ClassNotFoundException, IOException {

        // Deserialize the scalar instance variables (except Manager)
        userName = (String) stream.readObject();
    }    
    
    /**
     * Write a serialized version of this session object to the specified
     * object output stream.
     * <p>
     * <b>IMPLEMENTATION NOTE</b>:  The owning Manager will not be stored
     * in the serialized representation of this Session.  After calling
     * <code>readObject()</code>, you must set the associated Manager
     * explicitly.
     * <p>
     * <b>IMPLEMENTATION NOTE</b>:  Any attribute that is not Serializable
     * will be unbound from the session, with appropriate actions if it
     * implements HttpSessionBindingListener.  If you do not want any such
     * attributes, be sure the <code>distributable</code> property of the
     * associated Manager is set to <code>true</code>.
     *
     * @param stream The output stream to write to
     *
     * @exception IOException if an input/output error occurs
     */
    private void writeObject(ObjectOutputStream stream) throws IOException {

        // Write the scalar instance variables
        stream.writeObject(userName);
    }
    
    /**
     * Return a string representation of this object.
     */
    public String superToString() {

        // STARTS S1AS
        /*
        StringBuffer sb = new StringBuffer();
        sb.append("StandardSession[");
        sb.append(id);
        sb.append("]");
        return (sb.toString());
        */
        // END S1AS
        // START S1AS
/*
        StringBuffer sb = null;
        if(!this.isValid) {
            sb = new StringBuffer();
        } else {
            sb = new StringBuffer(1000);
        }
*/
        StringBuffer sb = new StringBuffer(1000);
        sb.append("BaseHASession[");
        sb.append(id);
        sb.append("]");
        
        sb.append("\n");
        sb.append("isValid:" + this.isValid);
        
        //if (this.isValid) {
            Enumeration<String> attrNamesEnum = getAttributeNames();
            while(attrNamesEnum.hasMoreElements()) {
                String nextAttrName = attrNamesEnum.nextElement();
                Object nextAttrValue = getAttributeInternal(nextAttrName);
                sb.append("\n");
                sb.append("attrName = " + nextAttrName);
                sb.append(" : attrValue = " + nextAttrValue);
            }
        //}

        return sb.toString();
        // END S1AS
    }    
    
    public String toString() {
        StringBuffer sb = new StringBuffer(200);
        //sb.append(super.toString());
        sb.append(this.superToString());
        sb.append(" ssoid: " + this.getSsoId());
        sb.append(" userName: " + this.getUserName());
        sb.append(" version: " + this.getVersion());
        sb.append(" persistent: " + this.isPersistent());
        return sb.toString();
    }
    
}
