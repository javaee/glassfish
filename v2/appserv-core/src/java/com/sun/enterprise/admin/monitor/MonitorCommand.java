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

/**
 * PROPRIETARY/CONFIDENTIAL.  Use of this product is subject to license terms.
 *
 * Copyright 2001-2002 by iPlanet/Sun Microsystems, Inc.,
 * 901 San Antonio Road, Palo Alto, California, 94303, U.S.A.
 * All rights reserved.
 */
package com.sun.enterprise.admin.monitor;

import java.io.Serializable;

import javax.management.JMException;
import javax.management.ObjectName;

/**
 * Monitoring AdminCommand. This is the super class of all monitoring commands.
 */
public abstract class MonitorCommand implements Serializable {

    /**
     * Constant to denote monitoring result attribute in event result.
     */
    public static final String MONITOR_RESULT = "monitor.result";

    /**
     * Object name for the MBean to which command applies.
     */
    protected ObjectName objectName;

    /**
     * Sub-types to which this command applies. If not null, the monitoring
     * command applies only to child MBeans of this type.
     */
    protected String monitoredObjectType;

    /**
     * Action Code. The sub-classes can use this to branch processing. 
     */
    protected int actionCode;

    /**
     * Run this command. The returned object should contain result of running
     * this command.
     * @throws JMXException if the command could not be run
     */
    abstract Object runCommand() throws JMException;

    /**
     * Get a string representation
     */
    public String toString() {
        return "MonitorCommand[ObjectName:" + objectName + ";Type:" + monitoredObjectType + ";ActionCode:" + actionCode + "]";
    }
}
