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

/* CVS information
 * $Header: /cvs/glassfish/jmx-remote/rjmx-impl/src/java/com/sun/enterprise/admin/jmx/remote/comm/MBeanServerMessageConductor.java,v 1.4 2005/12/25 04:26:31 tcfujii Exp $
 * $Revision: 1.4 $
 * $Date: 2005/12/25 04:26:31 $
 */

package com.sun.enterprise.admin.jmx.remote.comm;

import javax.management.remote.message.MBeanServerRequestMessage;
import javax.management.remote.message.MBeanServerResponseMessage;
import com.sun.enterprise.admin.jmx.remote.streams.StreamMBeanServerRequestMessage;

/** A Class that uses an instance of {@link IConnection} to actually invoke
 * some operation on remote resource and read the response back. What is Serialized
 * and deserialized contains instances of {@link MBeanServerRequestMessage} and 
 * {@link MBeanServerResponseMessage} class. Note that all the objects travelling
 * back and forth have to be serializable. There is no (dynamic) class loader support
 * provided here. The classes and their versions have to be agreed upon by the
 * client and server sides.
 * @author Kedar Mhaswade
 * @since S1AS8.0
 * @version 1.0
 */

public class MBeanServerMessageConductor {
    
    private final IConnection connection;
    
    /** Creates a new instance of MessageConductor */
    public MBeanServerMessageConductor(IConnection connection) {
        this.connection = connection;
    }
    
    public MBeanServerResponseMessage invoke(int methodId, Object[] params)
    throws Exception {
        final StreamMBeanServerRequestMessage request = new StreamMBeanServerRequestMessage(methodId, params, null); // delegationSubject to be considered: todo
        connection.send(request);
        //No matter what, only MBeanResponseMessage should be read back.
        return ((MBeanServerResponseMessage)connection.receive());
    }
}
