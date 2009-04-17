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
package org.glassfish.admin.amx.core;
 
import javax.management.ObjectName;

import java.util.Set;
import java.util.Map;
import java.util.HashSet;
import java.lang.reflect.Proxy;

import javax.management.*;

import org.glassfish.admin.amx.util.ExceptionUtil;
import org.glassfish.admin.amx.util.jmx.JMXUtil;



/**
    Validates and AMX MBean.
 */
public class AMXValidator  {
    private final MBeanServerConnection mConn;
    private final ObjectName            mTarget;
    private final MBeanInfo             mMBeanInfo;
    private final AMXProxy                   mProxy;
    
    private AMXValidator( final AMXProxy amx ) {
        final Extra extra = amx.extra();
        
        mProxy     = amx;
        mConn      = extra.mbeanServerConnection();
        mTarget    = extra.objectName();
        mMBeanInfo = extra.mbeanInfo();
    }
    
    private void
    fail( final String msg )
    {
        throw new AMXException( "MBean " + mTarget  + " failed validation: " + msg );
    }
    
    public static void validate(final AMXProxy proxy) {
        new AMXValidator(proxy).validateAsAMX();
    }
    
    private void validateObjectName() {
        final String type = mTarget.getKeyProperty("type");
        if ( type == null || type.length() == 0 )
        {
            fail ( "type property required in ObjectName" );
        }
    
        final String nameProp = mTarget.getKeyProperty("name");
        if ( nameProp != null )
        {
            if ( mTarget.getKeyProperty("name").length() == 0 )
            {
                fail ( "name property of ObjectName may not be empty" );
            }
        }
        else
        {
            // no name property, it's by definition a singleton
            final String name = mProxy.getName();
            if ( ! name.equals(AMXConstants.NO_NAME) )
            {
                fail ( "getName() returned incorrect name for a singleton: " + name);
            }
        }
    }
    
    private void validateRequiredAttributes() {
        // verify that the required attributes are present
        final Map<String,MBeanAttributeInfo> infos = JMXUtil.attributeInfosToMap(mMBeanInfo.getAttributes());
        final Set<String> attrNames = infos.keySet();
        if ( ! attrNames.contains("Name") ) {
            fail("MBeanInfo does not contain Name attribute" );
        }
        if ( ! attrNames.contains("Parent") ) {
            fail("MBeanInfo does not contain Parent attribute" );
        }
        
        if ( attrNames.contains("Children") ) {
            // must contain a non-null list of children
            try {
                if ( mProxy.getChildren() == null )
                {
                    fail( "value of Children attribute must not be null" );
                }
            }
            catch( final AMXException e ) {
                throw e;
            }
            catch( final Exception e ) {
                fail( "does not supply children correctly" );
            }
        }
        else {
            // must NOT contain children, we expect an exception
            try {
                mProxy.getChildren();
                fail( "Children attribute is present, but not listed in MBeanInfo" );
            }
            catch( final Exception e ) {
                // good, this is expected
            }
        }
    }
    
    private void validateAsAMX() {
        validateRequiredAttributes();
        validateObjectName();
    }

}













