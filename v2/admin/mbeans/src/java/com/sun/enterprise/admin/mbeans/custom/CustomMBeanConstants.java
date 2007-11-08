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
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import com.sun.enterprise.config.serverbeans.ServerTags;
import javax.management.ObjectName;

/** Class that holds the constant fields used by custom MBean deployment/registration code.
 */
public class CustomMBeanConstants {
    
    private CustomMBeanConstants() {
    }
    /** Represents the key for the name of a custom MBean.
     */
    public static final String NAME_KEY         = ServerTags.NAME;
    /** Represents the key for the object-name of a custom MBean.
     */
    public static final String OBJECT_NAME_KEY  = ServerTags.OBJECT_NAME;

    /** Represents the key for the class-name of a custom MBean.
     */
    public static final String IMPL_CLASS_NAME_KEY  = ServerTags.IMPL_CLASS_NAME;
    
    /** Represents the key for the object-type of the custom MBean.
     */
    public static final String OBJECT_TYPE_KEY = ServerTags.OBJECT_TYPE;
    
    /** Represents the key for server where the custom MBean is created.
     */
    public static final String SERVER_KEY = ServerTags.SERVER;
    
    /** Represents the key for enabled flag on the custom MBean.
     */
    public static final String ENABLED_KEY = ServerTags.ENABLED;
    /** Represents the user-defined MBean.
     */
    public static final int USER_DEFINED    = 0;
    /** Represents a "system-all"  MBean.
     */ 
    public static final int SYSTEM_ALL      = 1;
    /** Represents a "system-admin"  MBean.
     */     
    public static final int SYSTEM_ADMIN    = 2;
    
    /** LocalStrings in this package
     */
    public static final String CMB_LOCAL = "com.sun.enterprise.admin.mbeans.custom.LocalStrings";
    
    /** LocalStrings in loading package
     */
    public static final String CMB_LOADING_LOCAL = "com.sun.enterprise.admin.mbeans.custom.loading.LocalStrings";

    /** LocalStrings in ee package
     */
    public static final String CMB_EE_LOCAL = "com.sun.enterprise.ee.admin.mbeans.LocalStrings";
    
    /** LogStrings
     */
    public static final String CMB_LOG = "com.sun.logging.enterprise.system.tools.admin.LogStrings";
    
    public static final String CUSTOM_MBEAN_DOMAIN      = "user";
    private static final Set<Integer> iSet;
    static {
        iSet = new HashSet<Integer> ();
        iSet.add(USER_DEFINED);
        iSet.add(SYSTEM_ALL);
        iSet.add(SYSTEM_ADMIN); //use autoboxing wherever possible
    }
    public static final Set<Integer> MBEAN_TYPES        = Collections.unmodifiableSet(iSet);
    
    
    /** Returns an unmodifiable Map that contains the given valid mapping for this class.
     * Note that the valid keys are defined as the fields of this class.
     * @see #OBJECT_NAME_KEY
     * @see #NAME_KEY
     * @see #IMPL_CLASS_NAME_KEY
     */
    public static final Map<String, String> unmodifiableMap(final String key, final String value) throws IllegalArgumentException {
        final boolean vk = OBJECT_NAME_KEY.equals(key) || IMPL_CLASS_NAME_KEY.equals(key) || NAME_KEY.equals(key);
        if (!vk)
            throw new IllegalArgumentException(CMBStrings.get("InternalError", "invalid arg")); //TODO
        if (value == null)
            throw new IllegalArgumentException(CMBStrings.get("InternalError", "can't be null value")); //TODO
        final Map<String, String> m = new HashMap<String, String> ();
        m.put(key, value);
        return ( Collections.unmodifiableMap(m) );
    }
}