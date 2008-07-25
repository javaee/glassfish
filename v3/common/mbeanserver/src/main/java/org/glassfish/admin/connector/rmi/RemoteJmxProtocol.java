/*
 * The contents of this file are subject to the terms 
 * of the Common Development and Distribution License 
 * (the License).  You may not use this file except in
 * compliance with the License.
 * 
 * You can obtain a copy of the license at 
 * https://glassfish.dev.java.net/public/CDDLv1.0.html or
 * glassfish/bootstrap/legal/CDDLv1.0.txt.
 * See the License for the specific language governing 
 * permissions and limitations under the License.
 * 
 * When distributing Covered Code, include this CDDL 
 * Header Notice in each file and include the License file 
 * at glassfish/bootstrap/legal/CDDLv1.0.txt.  
 * If applicable, add the following below the CDDL Header, 
 * with the fields enclosed by brackets [] replaced by
 * you own identifying information: 
 * "Portions Copyrighted [year] [name of copyright owner]"
 * 
 * Copyright 2006 Sun Microsystems, Inc. All rights reserved.
 */

/*
 * RjmxProtocol.java
 * Indentation Information:
 * 0. Please (try to) preserve these settings.
 * 1. No tabs are used, all spaces.
 * 2. In vi/vim -
 *      :set tabstop=4 :set shiftwidth=4 :set softtabstop=4
 * 3. In S1 Studio -
 *      1. Tools->Options->Editor Settings->Java Editor->Tab Size = 4
 *      2. Tools->Options->Indentation Engines->Java Indentation Engine->Expand Tabs to Spaces = True.
 *      3. Tools->Options->Indentation Engines->Java Indentation Engine->Number of Spaces per Tab = 4.
 * Unit Testing Information:
 * 0. Is Standard Unit Test Written (y/n):
 * 1. Unit Test Location: (The instructions should be in the Unit Test Class itself).
 */

package org.glassfish.admin.connector.rmi;

/** An enumerated type for the Remote Jmx protocol.
 *
 * @author  mailto:Kedar.Mhaswade@Sun.Com
 * @since Sun Java System Application Server 8
 */
public class RemoteJmxProtocol {

    private final String protocol;
    /** Field */
    public static final RemoteJmxProtocol RMIJRMP	= new RemoteJmxProtocol("rmi_jrmp");
    /** Field */
    public static final RemoteJmxProtocol RMIIIOP	= new RemoteJmxProtocol("rmi_iiop");
    /** Field */
    public static final RemoteJmxProtocol JMXMP         = new RemoteJmxProtocol("jmxmp");
    /*Implementation note: The fields above are defined per default values in DTD */
    /** Creates a new instance of RemoteJmxProtocol */
    private RemoteJmxProtocol(final String prot) {
        this.protocol = prot;
    }

    public String getName() {
        return ( this.protocol );
    }

    public static RemoteJmxProtocol instance(final String protocol) {
        if (RMIJRMP.getName().equals(protocol))
            return ( RMIJRMP );
        else if (RMIIIOP.getName().equals(protocol))
            return ( RMIIIOP );
        else if (JMXMP.getName().equals(protocol))
            return ( JMXMP );
        else
            throw new UnsupportedOperationException("Unsupported protocol: \"" + protocol + "\"");
    }
    
    public String toString() { return protocol; }
}