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

package com.sun.enterprise.admin.mbeans.custom;
import java.util.List;
import javax.management.ObjectName;
import com.sun.enterprise.admin.server.core.CustomMBeanException;


/** Defines the behavior for querying the various MBean definitions. This is to factor
 * out the MBean related operations. The implementing classes could have specific behaviors
 * depending upon product edition, for example.
 * @since SJSAS9.0
 */
public interface CustomMBeanConfigQueries {
    
    /** Returns the collection of all the custom MBeans for a target as a List of Strings. Each element
     * in the list the <i> name </i> of the custom MBean created. Thus for a standalone instance
     * this method would return all the MBeans that are referenced from it.
     * @param target String representing the cluster or server instance
     * @return a List of Strings representing the referenced MBeans
     * @throws CustomMBeanException
     */
    public List<String> listMBeanNames(String target) throws CustomMBeanException;
    
    /** Returns access to get more information about the custom MBeans referenced from a
     * server instance. Every Config MBean is a wrapper over the domain.xml element and
     * the ObjectName of this MBean can allow clients to get more information such as ObjectType of
     * the custom MBean, by standard JMX calls. This method returns such ObjectNames.  This method
     * is more useful with a caveat that getting more information from the server results
     * in additional calls to the server.
     * @see #listMBeanNames(String) which returns only the names of these MBeans
     * @param target String representing the cluster or server instance
     * @return a List of ObjectNames representing the Config MBeans representing referenced MBeans
     * @throws CustomMBeanException
     */
    public List<ObjectName> listMBeanConfigObjectNames(String target) throws CustomMBeanException;
    
    /** A convenience method to take care of various client requests. It resembles the function
     * of a filter. Useful in getting the List of MBeans that for example are of object-type "USER_DEFINED"
     * and are not enabled. This is a more general purpose query interface.
     * @param target String representing the cluster or server instance
     * @param type an integer representing the type of MBean
     * @state boolean representing if or not the MBean is enabled
     * @see CustomMBeanConstants
     * @see #listMBeanConfigObjectNames(String)
     * @return the List of Config MBeans that represent an MBean definition referenced from a server instance or cluster
     */ 
    public List<ObjectName> listMBeanConfigObjectNames(String target, int type, boolean state) throws CustomMBeanException;
    
    /** Returns if the given MBean is referenced from a given target.
     * @param target String representing the cluster or server instance
     * @param name String representing the <i> name </name> of the MBean
     * @return a boolean representing if the given MBean exists in the given target
     * @throws CustomMBeanException
     */
    public boolean existsMBean(String target, String name) throws CustomMBeanException;
    
    /** Returns if the given MBean is referenced from a given target and is enabled.
     * @param target String representing the cluster or server instance
     * @param name String representing the <i> name </name> of the MBean
     * @return a boolean representing if the given MBean exists in the given target and is enabled
     * @throws CustomMBeanException
     */
    public boolean isMBeanEnabled(String target, String name) throws CustomMBeanException;
}