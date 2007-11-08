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

package com.sun.enterprise.web.connector.grizzly.comet;

/**
 * Simple event class used to pass information between <code>CometHandler</code>
 * and the Comet implementation.
 *
 * @author Jeanfrancois Arcand
 */
public class CometEvent<E> {

    
    /**
     * Interrupt the <code>CometHandler</code>.
     */
    public final static int INTERRUPT = 0;
    
    
    /**
     * Notify the <code>CometHandler</code>.
     */
    public final static int NOTIFY = 1;
    
    
    /**
     * Initialize the <code>CometHandler</code>.
     */    
    public final static int INITIALIZE = 2;
    
    
    /**
     * Terminate the <code>CometHandler</code>.
     */     
    public final static int TERMINATE = 3;    
    
    
    /**
     * Notify the <code>CometHandler</code> of available bytes.
     */       
    public final static int READ = 4;
    
    
    /**
     * Notify the <code>CometHandler</code> when the channel is writable.
     */       
    public final static int WRITE = 5;
    
    
    /**
     * This type of event.
     */
    protected int type;

    
    /**
     * Share an <code>E</code> amongst <code>CometHandler</code>
     */
    protected E attachment;
    
    
    /**
     * The CometContext from where this instance was fired.
     */
    private CometContext cometContext;
            
    
    /**
     * Create a new <code>ComettEvent</code>
     */
    public CometEvent() {
        type = NOTIFY;
    }
    
    
    /**
     * Return the <code>type</code> of this object.
     * @return int Return the <code>type</code> of this object
     */
    public int getType(){
        return type;
    }
    
    
    /**
     * Set the <code>type</code> of this object.
     * @param int the <code>type</code> of this object
     */    
    protected void setType(int type){
        this.type = type;
    }
    
    
    /**
     * Attach an <E>
     * @param attachment An attachment. 
     */
    public void attach(E attachment){
        this.attachment = attachment;
    }
    
    
    /**
     * Return the attachment <E>
     * @return attachment An attachment. 
     */    
    public E attachment(){
        return attachment;
    }

    
    /**
     * Return the <code>CometContext</code> that fired this event.
     */
    public CometContext getCometContext() {
        return cometContext;
    }

    
    /**
     * Set the <code>CometContext</code> that fired this event.
     */
    protected void setCometContext(CometContext cometContext) {
        this.cometContext = cometContext;
    }
    
}
