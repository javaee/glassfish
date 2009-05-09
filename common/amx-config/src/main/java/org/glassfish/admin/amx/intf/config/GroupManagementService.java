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
package org.glassfish.admin.amx.intf.config;

import org.glassfish.admin.amx.core.AMXProxy;

/**
 * Configuration for the &lt;group-management-service&gt; for a &lt;config&gt; in
 * a domain. This element controls the behavior of the group management service
 * used for cluster monitoring and failure detection.
 * @since Appserver 9.0
 */
public interface GroupManagementService
        extends AMXProxy, ConfigElement, PropertiesAccess
{
    /** Return the FD protocol tries.
     *  This is the maximum number of attempts to try before GMS confirms that a  
     *  failure is suspected in the group.
     *  @return positive integer specifying the number of such attempts
     */
    
    public String getFDProtocolMaxTries();

    /** Set the FD protocol tries to the specified positive integer value.
     * Must be a positive integer.
     * @param tries a positive integer specifying the number of attempts
     */
    public void setFDProtocolMaxTries(final String tries);

    
    public String getFDProtocolTimeoutMillis();

    public void setFDProtocolTimeoutMillis(final String duration);

    
    public String getMergeProtocolMaxIntervalMillis();

    public void setMergeProtocolMaxIntervalMillis(final String duration);

    
    public String getMergeProtocolMinIntervalMillis();

    public void setMergeProtocolMinIntervalMillis(final String duration);

    
    public String getPingProtocolTimeoutMillis();

    public void setPingProtocolTimeoutMillis(final String duration);

    
    public String getVSProtocolTimeoutMillis();

    public void setVSProtocolTimeoutMillis(final String duration);
}
