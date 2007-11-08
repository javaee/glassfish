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
 * IIOPEndPoint.java
 *
 * Created on June 13, 2004, 12:07 AM
 */

package com.sun.enterprise.tools.upgrade.cluster;

/**
 *
 * @author  prakash
 */
public class IIOPEndPoint {
    private String id;
    private String port;
    private String host;
    
    /** Creates a new instance of IIOPEndPoint */
    public IIOPEndPoint(String id, String host, String port) {
        this.id = id;
        this.host = host;
        this.port = port;
    }
    
    /** Getter for property host.
     * @return Value of property host.
     *
     */
    public java.lang.String getHost() {
        return host;
    }
    
    /** Setter for property host.
     * @param host New value of property host.
     *
     */
    public void setHost(java.lang.String host) {
        this.host = host;
    }
    
    /** Getter for property id.
     * @return Value of property id.
     *
     */
    public java.lang.String getId() {
        return id;
    }
    
    /** Setter for property id.
     * @param id New value of property id.
     *
     */
    public void setId(java.lang.String id) {
        this.id = id;
    }
    
    /** Getter for property port.
     * @return Value of property port.
     *
     */
    public java.lang.String getPort() {
        return port;
    }
    
    /** Setter for property port.
     * @param port New value of property port.
     *
     */
    public void setPort(java.lang.String port) {
        this.port = port;
    }
    
}
