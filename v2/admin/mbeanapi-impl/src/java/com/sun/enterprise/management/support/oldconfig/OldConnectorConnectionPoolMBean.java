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
 * Copyright 2004-2005 Sun Microsystems, Inc.  All rights reserved.
 * Use is subject to license terms.
 */

/**
	Generated: Mon Apr 19 18:21:54 PDT 2004
	Generated from:
	com.sun.appserv:type=connector-connection-pool,name=__SYSTEM/pools/foo/bar,category=config
*/

package com.sun.enterprise.management.support.oldconfig;

import java.util.ArrayList;

import javax.management.MBeanNotificationInfo;
import javax.management.NotificationListener;
import javax.management.ObjectName;
import javax.management.AttributeList;
import javax.management.NotificationFilter;


public interface OldConnectorConnectionPoolMBean
{
        /**
                note: actual Attribute name is: connection-definition-name
        */
        public String   getConnectionDefinitionName();
        /**
                note: actual Attribute name is: connection-definition-name
        */
        public void     setConnectionDefinitionName( final String value );

        /**
                note: actual Attribute name is: description
        */
        public String   getDescription();
        /**
                note: actual Attribute name is: description
        */
        public void     setDescription( final String value );

        /**
                note: actual Attribute name is: fail-all-connections
        */
        public boolean  getFailAllConnections();
        /**
                note: actual Attribute name is: fail-all-connections
        */
        public void     setFailAllConnections( final boolean value );

        /**
                note: actual Attribute name is: idle-timeout-in-seconds
        */
        public String   getIdleTimeoutInSeconds();
        /**
                note: actual Attribute name is: idle-timeout-in-seconds
        */
        public void     setIdleTimeoutInSeconds( final String value );

        /**
                note: actual Attribute name is: is-connection-validation-required
        */
        public boolean  getIsConnectionValidationRequired();
        /**
                note: actual Attribute name is: is-connection-validation-required
        */
        public void     setIsConnectionValidationRequired( final boolean value );

        /**
                note: actual Attribute name is: max-pool-size
        */
        public String   getMaxPoolSize();
        /**
                note: actual Attribute name is: max-pool-size
        */
        public void     setMaxPoolSize( final String value );

        /**
                note: actual Attribute name is: max-wait-time-in-millis
        */
        public String   getMaxWaitTimeInMillis();
        /**
                note: actual Attribute name is: max-wait-time-in-millis
        */
        public void     setMaxWaitTimeInMillis( final String value );

        /**
                note: actual Attribute name is: name
        */
        public String   getName();
        /**
                note: actual Attribute name is: name
        */
        public void     setName( final String value );

        /**
                note: actual Attribute name is: pool-resize-quantity
        */
        public String   getPoolResizeQuantity();
        /**
                note: actual Attribute name is: pool-resize-quantity
        */
        public void     setPoolResizeQuantity( final String value );

        /**
                note: actual Attribute name is: resource-adapter-name
        */
        public String   getResourceAdapterName();
        /**
                note: actual Attribute name is: resource-adapter-name
        */
        public void     setResourceAdapterName( final String value );

        /**
                note: actual Attribute name is: steady-pool-size
        */
        public String   getSteadyPoolSize();
        /**
                note: actual Attribute name is: steady-pool-size
        */
        public void     setSteadyPoolSize( final String value );

        /**
                note: actual Attribute name is: transaction-support
        */
        public String   getTransactionSupport();
        /**
                note: actual Attribute name is: transaction-support
        */
        public void     setTransactionSupport( final String value );


// -------------------- Operations --------------------
        public ObjectName       createSecurityMap( final AttributeList attribute_list );
        public ObjectName       createSecurityMap( final AttributeList param1, final String param2, final String param3, final String param4 );
        public boolean  destroyConfigElement();
        public AttributeList    getAttributes( final String param1, final String param2 );
        public String   getDefaultAttributeValue( final String attributeName );
        public AttributeList    getProperties();
        public Object   getPropertyValue( final String propertyName );
        public javax.management.ObjectName[]    getSecurityMap();
        public ObjectName       getSecurityMapByName( final String key );
        public java.util.ArrayList      listSecurityMap( final String param1, final Boolean param2, final String param3, final String param4 );
        public void     removeSecurityMapByName( final String key );
        public void     setProperty( final javax.management.Attribute nameAndValue );
        public boolean  updateSecurityMap( final AttributeList param1, final String param2 );

}