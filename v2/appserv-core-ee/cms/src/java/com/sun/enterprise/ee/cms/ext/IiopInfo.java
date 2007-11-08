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

package com.sun.enterprise.ee.cms.ext;

import com.sun.enterprise.config.serverbeans.IiopListener;
import java.io.Serializable;


/**
 * This is a convenience class that is specific to the Sun Java Application
 * Server's Iiop component. This encapsulates details related to a particular
 * instance's Iiop relevant information.
 *
 * @author <a href=mailto:shreedhar.ganapathy@sun.com>Shreedhar Ganapathy</a>
 *         Date: Jul 9, 2005
 * @version $Revision: 1.2 $
 */
public class IiopInfo implements Serializable{
    private int weight;
    private String Id;
    private String address;
    private String port;
    private String hostName;
    public static final String IIOP_MEMBER_DETAILS_KEY = "IIOPListenerEndPoints";

    /**
     * corresponds to the weight assigned to an instance for round robin load
     * balancing. The GMS Lifecycle code populates this value by looking up the
     * config-api for the attribute lb-weight in the Server object.
     * @param weight - int showing the weight assigned to that instance.
     */
    public void setWeight(final int weight) {
        this.weight = weight;
    }

    /**
     * returns the weight assigned to an instance for round robin load balancing
     * @return
     */
    public int getWeight() {
        return weight;
    }

    /**
     * corresponds to the Id attribute of the iiop-listener element.
     * @param Id
     */

    public void setID(final String Id){
        this.Id = Id;
    }

    /**
     * returns the Id of this iiop listener
     */
    public String getID(){
        return Id;
    }

    /**
     * corresponds to the address attribute of the iiop-listener element
     * @param address
     */
    public void setAddress(final String address){
        this.address=address;
    }

    /**
     * returns the IP Address of the listener
     * @return String - ip address
     */
    public String getAddress(){
        return address;
    }
    /**
     * corresponds to the port attribute of the iiop-listener element
     * @param port
     */
    public void setPort(final String port){
        this.port=port;
    }
    /**
     * return the port value of the listener
     * @return String - port of the listener
     */
    public String getPort(){
        return port;
    }

    public String toString(){
        return new StringBuffer().append( "[Weight=" )
                .append( weight )
                .append( ":Id=" )
                .append( Id )
                .append( ":Address=" )
                .append( address )
                .append( ":Port=" )
                .append( port )
                .append( ":HostName=")
                .append( hostName )
                .append( "]" ).toString();
    }

    /**
     * corresponds to the host on which this instance's Node Agent
     * is located.
     * @param nodeAgentHostName
     */
    public void setHostName ( final String nodeAgentHostName ) {
        this.hostName = nodeAgentHostName;
    }
    /**
     * returns the hostName for this instance.
     * @return String - hostname of node agent of this instance
     */
    public String getHostName (){
        return hostName;
    }
}
