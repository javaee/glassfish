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
 * MBeanServerResponseActor.java
 * $Id: MBeanServerResponseActor.java,v 1.3 2005/12/25 04:26:34 tcfujii Exp $
 * $Revision: 1.3 $
 * $Date: 2005/12/25 04:26:34 $
 * Indentation Information:
 * 0. Please (try to) preserve these settings.
 * 1. Tabs are preferred over spaces.
 * 2. In vi/vim -
 *		:set tabstop=4 :set shiftwidth=4 :set softtabstop=4
 * 3. In S1 Studio -
 *		1. Tools->Options->Editor Settings->Java Editor->Tab Size = 4
 *		2. Tools->Options->Indentation Engines->Java Indentation Engine->Expand Tabs to Spaces = False.
 *		3. Tools->Options->Indentation Engines->Java Indentation Engine->Number of Spaces per Tab = 4.
 * Unit Testing Information:
 * 0. Is Standard Unit Test Written (y/n):
 * 1. Unit Test Location: (The instructions should be in the Unit Test Class itself).
 */

package com.sun.enterprise.admin.jmx.remote.internal;

import javax.management.remote.message.MBeanServerResponseMessage;

/** A class to <code> act </code> on the instances of {@link MBeanServerResponseMessage}.
 * Since the response may have both the exceptions and valid results, we have to
 * carefully handle them.
 * @author  mailto:Kedar.Mhaswade@Sun.Com
 * @since Sun Java System Application Server 8
 */
class MBeanServerResponseActor {
    
    private MBeanServerResponseActor() {
        //disllow
    }
    static final void voidOrThrow(final MBeanServerResponseMessage message) throws Exception {
        if (message.isException()) {
            throw ((Exception)message.getWrappedResult());
        }
        /*ignore the return value, if there is no exception, as the message
        implies a method invocation that returns void. */
    }
    
    static final Object returnOrThrow(final MBeanServerResponseMessage message) throws Exception {
        voidOrThrow(message);
        return ( message.getWrappedResult() );
    }
}
