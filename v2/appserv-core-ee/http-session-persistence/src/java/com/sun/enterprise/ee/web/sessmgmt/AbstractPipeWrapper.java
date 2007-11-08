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
 * AbstractPipeWrapper.java
 *
 * Created on June 15, 2006, 1:27 PM
 *
 */

package com.sun.enterprise.ee.web.sessmgmt;

import com.sun.enterprise.web.ServerConfigLookup;

/**
 *
 * @author Larry White
 */
public abstract class AbstractPipeWrapper {
    
    public final static String SENDER_PIPE = "S";
    public final static String RECEIVER_PIPE = "R";    
    
    /** Creates a new instance of AbstractPipeWrapper */
    public AbstractPipeWrapper() {
    }
    
    protected String getInstanceName() {
        ServerConfigLookup lookup = new ServerConfigLookup();
        return lookup.getServerName();
    }
    
    protected boolean isMessageFromYourself(String returnInstance) {
        return (returnInstance.equalsIgnoreCase(getInstanceName()));
    }
    
    String getSenderOrReceiver() {
        return _senderOrReceiver;
    }
    
    boolean isSender() {
        return SENDER_PIPE.equals(_senderOrReceiver);
    }
    
    boolean isReceiver() {
        return RECEIVER_PIPE.equals(_senderOrReceiver);
    }
    
    String getPartnerInstanceName() {
        return _partnerInstanceName;
    }
    
    public String toString() {
        StringBuffer sb = new StringBuffer(100);
        sb.append("_partnerInstanceName=" + _partnerInstanceName + "\n");
        sb.append("isSender=" + isSender() + "\n");
        return sb.toString();       
    }
    
    protected String _senderOrReceiver = SENDER_PIPE;
    protected String _partnerInstanceName = null;
}
