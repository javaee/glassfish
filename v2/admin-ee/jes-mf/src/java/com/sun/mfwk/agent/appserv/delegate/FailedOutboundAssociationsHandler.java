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
 * Copyright 2006 Sun Microsystems, Inc. All rights reserved.
 * Use is subject to license terms.
 */
package com.sun.mfwk.agent.appserv.delegate;

import java.io.IOException;
import java.util.HashSet;
import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.Set;

import com.sun.mfwk.agent.appserv.logging.LogDomains;
import com.sun.mfwk.agent.appserv.util.Constants;
import com.sun.mfwk.agent.appserv.util.Utils;

import javax.management.AttributeNotFoundException;
import javax.management.InstanceNotFoundException;
import javax.management.ObjectName;
import javax.management.MBeanException;
import javax.management.MBeanServerConnection;
import javax.management.ReflectionException;

/**
 * Returns failed outbound associations count.
 */
public class FailedOutboundAssociationsHandler extends BaseHandler {

    /**
     * Constructor.
     */
    public FailedOutboundAssociationsHandler() {
        super();
    }
    
    public Object handleAttribute(ObjectName peer, String attribute, 
            MBeanServerConnection mbs) throws HandlerException, AttributeNotFoundException, 
             MBeanException, ReflectionException, InstanceNotFoundException,
             IOException {

        long failedOutboundAssociations = 0;

        try {
            String serverName = peer.getKeyProperty("name");

            //Get hold of Connector Connection Pool objects.
            ObjectName connectorConnectionPoolPattern = new ObjectName( 
                Constants.CONNECTOR_CONNECTION_POOL_PATTERN + "," + "server=" + serverName);
            Set objectNames = mbs.queryNames(connectorConnectionPoolPattern, null);
 
            //Get the of sum of all the "numconntimedout-count" attribute from all the
            //Connector Connection Pool objects.
            failedOutboundAssociations = 
                Utils.getAttributeSum(mbs, objectNames, "numconntimedout-count");


            //Get hold of JDBC Connection Pool objects.
            ObjectName jdbcConnectionPoolPattern = new ObjectName( 
                Constants.JDBC_CONNECTION_POOL_PATTERN + "," + "server=" + serverName);
            objectNames = mbs.queryNames(jdbcConnectionPoolPattern, null);
 
            //Get the of sum of all the "numconntimedout-count" attribute 
            //from all the Connector Connection Pool objects and add it
            //to the failed outbound associations count.
            failedOutboundAssociations = failedOutboundAssociations +
                Utils.getAttributeSum(mbs, objectNames, "numconntimedout-count");
        } catch (Exception exception) {
            Utils.log(Level.SEVERE, 
                "Error while getting Failed Outbound Associations", exception);
            throw new HandlerException(exception);
        }

        return new Long(failedOutboundAssociations);
    }
}
