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
 * ClusteredInstance.java
 *
 * Created on May 21, 2004, 3:26 PM
 */

package com.sun.enterprise.tools.upgrade.cluster;

/**
 *
 * @author  prakash
 */

/*
 * This class represents one instance in clinstance.conf file
 *
 */ 

public class ClusteredInstance {
    
    private String instanceName;
    private String user;
    private String host;
    private String port;
    private String domain;
    private String instancePort;
    private boolean master;
    
    /** 
     * Creates a new instance of ClusteredInstance 
     */
    public ClusteredInstance(String instName) {
        this.instanceName = instName;
    }
    
    public void extractDataFromLine(String line){
        if(line.trim().startsWith("user")){
            this.user = line.substring("user".length()).trim();
        }else if(line.trim().startsWith("host")){
            this.host = line.substring("host".length()).trim();
        }else if(line.trim().startsWith("port")){
            this.port = line.substring("port".length()).trim();
        }else if(line.trim().startsWith("domain")){
            this.domain = line.substring("domain".length()).trim();
        }else if(line.trim().startsWith("instanceport")){
            this.instancePort = line.substring("instanceport".length()).trim();
        }else if(line.trim().startsWith("master")){
            String masterStr = line.substring("master".length()).trim();
            this.master = ("true".equals(masterStr)) ? true :false; 
        }        
    }
    
    /** Gets property port.
     * @return Value of property port.
     *
     */
    public java.lang.String getPort() {
        return port;
    }    
    
    /** Sets property port.
     * @param port New value of property port.
     *
     */
    public void setPort(java.lang.String port) {
        this.port = port;
    }    

    /** Gets property domain.
     * @return Value of property domain.
     *
     */
    public java.lang.String getDomain() {
        return domain;
    }    
    
    /** Sets property domain.
     * @param domain New value of property domain.
     *
     */
    public void setDomain(java.lang.String domain) {
        this.domain = domain;
    }
    
    /** Gets property host.
     * @return Value of property host.
     *
     */
    public java.lang.String getHost() {
        return host;
    }
    
    /** Sets property host.
     * @param host New value of property host.
     *
     */
    public void setHost(java.lang.String host) {
        this.host = host;
    }
    
    /** Gets property instanceName.
     * @return Value of property instanceName.
     *
     */
    public java.lang.String getInstanceName() {
        return instanceName;
    }
    
    /** Sets property instanceName.
     * @param instanceName New value of property instanceName.
     *
     */
    public void setInstanceName(java.lang.String instanceName) {
        this.instanceName = instanceName;
    }
    
    /** Gets property instancePort.
     * @return Value of property instancePort.
     *
     */
    public java.lang.String getInstancePort() {
        return instancePort;
    }
    
    /** Sets property instancePort.
     * @param instancePort New value of property instancePort.
     *
     */
    public void setInstancePort(java.lang.String instancePort) {
        this.instancePort = instancePort;
    }
    
    /** Gets property user.
     * @return Value of property user.
     *
     */
    public java.lang.String getUser() {
        return user;
    }
    
    /** Sets property user.
     * @param user New value of property user.
     *
     */
    public void setUser(java.lang.String user) {
        this.user = user;
    }
    
    /** Gets property master.
     * @return Value of property master.
     *
     */
    public boolean isMaster() {
        return master;
    }
    
    /** Sets property master.
     * @param master New value of property master.
     *
     */
    public void setMaster(boolean master) {
        this.master = master;
    }
    
}
