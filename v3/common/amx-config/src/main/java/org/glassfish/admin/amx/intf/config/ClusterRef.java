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

/**
 * represents the &lt;server-ref&gt; element.
 */
public interface ClusterRef extends Ref, HealthCheckerCR
{
    /**
    @since Appserver 9.0
     */
    public String getReferencedClusterName();

    /**
    @since Appserver 9.0
     */
    public void setReferencedClusterName(String clusterName);

    /**
    <b>EE only</b>
    Load balancing policy to be used for this cluster. Possible
    values are round-robin , weighted-round-robin or
    user-defined. round-robin is the default. For
    weighted-round-robin, the weights of the instance are
    considered while load balancing. For user-defined, the policy
    is implemented by a shared library which is loaded by the
    load balancer and the instance selected is delegated to the
    loaded module.

    Return load balancing policy to be used for this cluster. Possible
    values are: See {@link LbPolicyTypeValues}.
     */
    public String getLBPolicy();

    /**
    <b>EE only</b>
    Set the load balancing policy to be used for this cluster.
    See {@link org.glassfish.admin.amx.intf.config.LbPolicyTypeValues}.
     */
    public void setLBPolicy(final String value);

    /**
    <b>EE only</b>
    Returns the absolute path to the shared library
    implementing the {@link LbPolicyTypeValues#USER_DEFINED} policy.
     */
    public String getLBPolicyModule();

    /**
    <b>EE only</b>
    Sets the absolute path to the shared library implementing the
    {@link LbPolicyTypeValues#USER_DEFINED} policy.
     */
    public void setLBPolicyModule(final String lbPolicyModule);
}
