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
import java.util.Map;
import javax.management.MBeanInfo;
import com.sun.enterprise.admin.server.core.CustomMBeanException;


/** Interface that defines the behavior of modifying the configuration in the admin server
 * for custom mbeans. Though this is mostly similar to a J2EE application or
 * module deployment, in order to factor out the Custom MBean Configuration changes,
 * this interface is defined. Note that <i> target </i> is a central concept in the
 * deployment of applications and mbeans. In all the interface methods that are defined here,
 * the target must be a server or a cluster. A special target with name "server"
 * defines a domain. The handling of targets is defined in the Custom MBean design
 * document.
 * @since SJSAS9.0
 */
public interface CustomMBeanOperationsMBean {
    /** Creates an MBean with given className in given target. Results in the call to
     * #createMBean(String, String, Map<String, String>, Map<String, String>).
     * The Map<String, String> will be empty, so will be Map<String, String>.
     * @param target String representing the target
     * @param className String representing the implementation class
     */
    public String createMBean(String target, String className) throws CustomMBeanException;

    /** Creates an MBean with given className in given target. Results in the call to
     * #createMBean(String, String, Map<String, String>, Map<String, String>).
     * The Map<String, String> should contain the 0 or more items from the @link{CustomMBeanConstants}
     * class.
     * <ul>
     * <li>class-name of the custom mbean, which is mandatory</li>
     * <li>name of the custom mbean</li>
     * <li>object-name of the custom mbean</li>
     * </ul>
     * @param target String representing the target
     * @param params a Map<String, String> explained above
     */
    public String createMBean(String target, Map<String, String> params) throws CustomMBeanException;

    /** Creates an MBean with given class-name in the given target's configuration.
     * The class-name may not be null. The class must represent a valid JMX MBean implementation.
     * All the standard requirements must be fulfilled by the given class. Here is a list of 
     * requirements from the given class-name. It is by no means an exhaustive one. Refer to
     * the design document for the complete list.
     * <ul>
     * <li> The class-name may not be null </li>
     * <li> It must be instantiable with a default constructor </li>
     * <li> The class bits must be available in order to be loaded dynamically, knowing full name </li>
     * Refer to the class Javadoc for a note on target.
     * The various mappings in the params map are as follows:
     * <ul> <li> String CustomMBeanConstants.NAME_KEY -> name of the MBean </li>
     * <li> Mandatory String CustomMBeanConstants.IMPL_CLASS_NAME_KEY -> the actual class </li>
     * <li> String CustomMBeanConstants.OBJECT_NAME_KEY -> intended ObjectName of the MBean </li> </ul>
     *
     * @param target String representing target where the MBean would be instantiated. The value null represents
     * the domain as the target
     * @param params a Map with mappings between String and String containing various configuration parameters
     * @param attributes a Map between two Strings repsenting name of an attribute and String representation of its value.
     * </ul>
     * @throws All the createMBean methods in this class throw CustomMBeanException if the custom MBean definition
     * is invalid or if we can guarantee that the registration of the MBean at a latter point in time without
     * any change in the runtime state of the system would be successful.
     * @return the <i> name </i> of the MBean as determined by the backend. The name returned is
     * guaranteed to be unique amongst all application sub-elements.
     */
    public String createMBean(String target, Map<String, String> params, Map<String, String> attributes) throws CustomMBeanException;
    
    /** Deletes the MBean with the given name from the configuration and optionally deletes the corresponding
     * classes from the file system. The deletion of classes should not be relied upon by the clients.
     * Refer to the design document for exact semantics of this method.
     */
    public String deleteMBean(String target, String name) throws CustomMBeanException;
    
    /** Creates a reference to the given MBean. It is a misnomer in that it actually creates an application-ref
     * sub-element in a server, but it is named so for functional clarity. This is available only in 
     * EE of appserver because it is against a precedent to create references in PE.
     * @param target String representing the target, could be cluster or a server instance. A null value represents the domain itself.
     * @param ref String representing the <i> name </i> of the custom MBean definition
     * @throws CustomMBeanException if the custom MBean reference could not be created for some reason
     */
    public void createMBeanRef(String target, String ref) throws CustomMBeanException;

    /** Deletes a reference to the given MBean from a server or cluster. It is a misnomer in that it actually deletes an application-ref
     * sub-element from a server, but it is named so for functional clarity. This is available only in 
     * EE of appserver because it is against a precedent to delete references in PE.
     * @param target String representing the target, could be cluster or a server instance
     * @param ref String representing the <i> name </i> of the custom MBean definition, which is same as
     * as the referenced name
     * @throws CustomMBeanException if the custom MBean reference could not be deleted for some reason
     */
    public void deleteMBeanRef(String target, String ref) throws CustomMBeanException;
 
    /**
     * Return the MBeanInfo of a given Custom MBean.  
     * The MBean must be loadable from the standard App Server location.
     * The code does this:
     * <ul>
     * <li>Register the MBean in the MBeanServer
     * <li>Fetch and save the MBeanInfo
     * <li>Unregister the MBean
     * </ul>
     * Note that if the MBean can't be deployed successfully then this method won't work.
     * @param classname 
     * @throws com.sun.enterprise.admin.mbeans.custom.CustomMBeanException 
     * @return The MBeanInfo object
     */
     public MBeanInfo getMBeanInfo(String classname) throws CustomMBeanException;
}