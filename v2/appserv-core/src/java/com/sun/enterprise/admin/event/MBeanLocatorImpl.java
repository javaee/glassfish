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

package com.sun.enterprise.admin.event;

import java.util.Set;
import com.sun.enterprise.admin.common.MBeanServerFactory;
import javax.management.ObjectName;
import javax.management.QueryExp;
import javax.management.MalformedObjectNameException;

import com.sun.enterprise.admin.common.constant.AdminConstants;
import java.util.logging.Level;
import java.util.logging.Logger;


/**
 * Implementation of mbeanLocator by using QueryNames
 *
 * @author Satish Viswanatham
 */
public class MBeanLocatorImpl implements MBeanLocator{

    static Logger logger = Logger.getLogger(AdminConstants.kLoggerName);

    static final String UNSUPPORTED_TYPE = "event.event_key_type_not_supported";

    static final String MALFORMED_OBJECT_KEY = "event.event_key_is_malformed";

    /**
     * This method returns the mbean of interest for the event/notification
     *
     * @param  Object object key -- it could EventKey, ObjectName or String
     * @return Object proxy for the mbean that is of interest
     */
    public Object locate(Object objectKey) {
                 
        ObjectName on = null;
        QueryExp e = null;

        if ( objectKey instanceof EventKey ) {
            EventKey eKey = (EventKey) objectKey;
            on = eKey.getObjectName();
            e = eKey.getQuery();
        } else if ( objectKey instanceof ObjectName) {
            on = (ObjectName) objectKey;
        } else if ( objectKey instanceof String ) {
            try {
                on = new ObjectName( (String) objectKey);
            } catch ( Exception exp ) {
               logger.log(Level.WARNING, MALFORMED_OBJECT_KEY, objectKey);
               return null;
            }
        } else {
            if ( objectKey != null ) 
                logger.log(Level.WARNING, UNSUPPORTED_TYPE, objectKey.getClass().getName());
            return null;
        }

        return MBeanServerFactory.getMBeanServer().queryMBeans(on, e);
    }

    public static void main (String[] args) {

        /* fix this later so that
         *  local can take MBeanServerproxy can be tested that way
         */
        if ( args.length < 1 ) 
        {
            System.out.println("Usage:  <object name> ");
            System.exit(3);
        }

        String name = args[0];
        System.out.println("name is " + args[0]);
        MBeanLocatorImpl m = new MBeanLocatorImpl();
        Set s = null;
        try {
           s = (Set)m.locate(new ObjectName(name));
        } catch (MalformedObjectNameException e ) {
            System.out.println("Please enter a valid object name ");
            System.exit(1);
        } catch ( NullPointerException  e) {
            System.out.println("Please enter a non null object name ");
            System.exit(2);
        }
        System.out.println(" The number of beans matched that description " + s.size());
        
    }

}
