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

package com.sun.enterprise.management.agent;


// java import
import java.util.Set;
import java.io.ObjectInputStream;
import javax.management.*;
import java.rmi.RemoteException;
import javax.management.j2ee.Management;


/* Combines the Management interface and the remaining javax.management.MBeanServer interface methods
 * with the exception of deserialize which does not return a valid RMI-IIOP type.
 * The MEJB interface also includes registerAppClient which faclitates the registration of an AppClient MO
 * from a the AppContainer
 *
 * @author Hans Hrasna
 */
public interface MEJB extends Management {

    /**
     * Gets the names of MBeans controlled by the MBean server. This method
     * enables any of the following to be obtained: The names of all MBeans,
     * the names of a set of MBeans specified by pattern matching on the
     * <CODE>ObjectName</CODE> and/or a Query expression, a specific MBean name (equivalent to
     * testing whether an MBean is registered). When the object name is
     * null or no domain and key properties are specified, all objects are selected (and filtered if a
     * query is specified). It returns the set of ObjectNames for the MBeans
     * selected.
     *
     * @param name The object name pattern identifying the MBeans to be retrieved. If
     * null or no domain and key properties are specified, all the MBeans registered will be retrieved.
     * @param query The query expression to be applied for selecting MBeans. If null
     * no query expression will be applied for selecting MBeans.
     *
     * @return  A set containing the ObjectNames for the MBeans selected.
     * If no MBean satisfies the query, an empty list is returned.
     *
     */
    Set queryNames(ObjectName name, QueryExp query) throws RemoteException  ;
  
}
