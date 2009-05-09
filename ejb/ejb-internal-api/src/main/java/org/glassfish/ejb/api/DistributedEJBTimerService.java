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

package org.glassfish.ejb.api;

import org.jvnet.hk2.annotations.Contract;

@Contract
public interface DistributedEJBTimerService {

    public int migrateTimers( String serverId );

    public String[] listTimers( String[] serverIds );

    /*
     * Since GMS is now dropped from 8.1, need a mechanism by which a canceltimer
     * initiated on another server instance would be affected on the owner server
     * instance. This would be done by reading the database before delivering the
     * ejbTimeout call to ensure that the timer is still valid. Since this would
     * lead to potential performance degradation also need to provide some user
     * interaction to control this behavior.
     *
     * For SE/EE the default value if system property is not specified would be
     * "true" (i.e. always read from DB before delivering a ejbTimeout for a timer)
     *
    */
    public void setPerformDBReadBeforeTimeout( boolean defaultDBReadValue );

//    public void cancelTimerTask( Object timerId, String ownerServerId );

} //DistributedEJBTimerService.java
