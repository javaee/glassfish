/*
 * The contents of this file are subject to the terms 
 * of the Common Development and Distribution License 
 * (the License).  You may not use this file except in
 * compliance with the License.
 * 
 * You can obtain a copy of the license at 
 * https://glassfish.dev.java.net/public/CDDLv1.0.html or
 * glassfish/bootstrap/legal/CDDLv1.0.txt.
 * See the License for the specific language governing 
 * permissions and limitations under the License.
 * 
 * When distributing Covered Code, include this CDDL 
 * Header Notice in eokach file and include the License file 
 * at glassfish/bootstrap/legal/CDDLv1.0.txt.  
 * If applicable, add the following below the CDDL Header, 
 * with the fields enclosed by brackets [] replaced by
 * you own identifying information: 
 * "Portions Copyrighted [year] [name of copyright owner]"
 * 
 * Copyright 2006 Sun Microsystems, Inc. All rights reserved.
 */

package org.glassfish.api.container;

import org.glassfish.api.deployment.DeploymentContext;
import org.jvnet.hk2.annotations.Contract;


/**
 * A sniffer implementation is responsible for identifying a composite
 * application.
 *
 * <p>
 * For clients who want to work with Sniffers, see <tt>SnifferManager</tt> in the kernel.
 *
 */
@Contract
public interface CompositeSniffer extends Sniffer {

    /**
     * Returns true if the passed file or directory is recognized by this
     * composite sniffer.
     * @param context deployment context
     * @return true if the location is recognized by this sniffer
     */
    public boolean handles(DeploymentContext context);
}
