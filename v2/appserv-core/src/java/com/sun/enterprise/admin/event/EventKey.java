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

import javax.management.QueryExp;
import javax.management.ObjectName;

import com.sun.enterprise.admin.common.constant.AdminConstants;
import java.util.logging.Level;
import java.util.logging.Logger;


/**
 * This class encapsulates event information that will be useful
 * in devlivering the event, like the target mbean name and 
 * and/or query expression, so that this can be potentially can be delivered 
 * to one than one MBean
 *
 * @author Satish Viswanatham
 * @version 1.0
 */
public class EventKey implements java.io.Serializable {

    static Logger logger = Logger.getLogger(AdminConstants.kLoggerName);

    static final String MALFORMED_OBJECT_NAME = "event.object_name_in_event_is_invalid";

    /**
     * Target object name for event
     */
    private ObjectName objName;

    /**
     * Filter query for event processing
     */
    private QueryExp qry;


    /**
     * public constructor
     */
    public EventKey ( String name, QueryExp q) {
    	try {
           objName = new ObjectName ( name);
           qry = q;
	    } catch( Exception e) {
	        //throw a warning
	        // user need to reset values 
            logger.log(Level.WARNING, MALFORMED_OBJECT_NAME, name);
	    }
    }
    
    /**
     * retuns the ObjectName
     */
    public ObjectName getObjectName() { return objName; }

    /**
     * return query for this event
     *
     * @return QuertExp query object
     */
    public QueryExp   getQuery()         { return qry; }


    /**
     * object name setter
     */
    public void setObjectName(ObjectName obj) { objName = obj; }

    /**
     * query exp setter
     */
    public void setQuery(QueryExp q) { qry = q;}
}
