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

package com.sun.enterprise.ee.admin.mbeans;

import com.sun.enterprise.admin.server.core.AdminService;

import javax.management.ObjectName;
import javax.management.MalformedObjectNameException;

/**
 * Provides ObjectNames for EE specific MBeans
 * @author <a href=mailto:shreedhar.ganapathy@sun.com>Shreedhar Ganapathy</a>
 *         Date: Aug 3, 2005
 * @version $Revision: 1.1.1.1 $
 */
public final class EEConfigMBeanObjectNames {
    private static final String GmsClientName =
            new StringBuilder().append(AdminService.PRIVATE_MBEAN_DOMAIN_NAME)
                .append(":type=GMSClientMBean,category=monitor")
                .toString();

    public static ObjectName getGMSClientObjName()
            throws MalformedObjectNameException
    {
        return new ObjectName(GmsClientName );
    }
    
    public static ObjectName getClusterConfigMBeanObjectName(final String cluster) throws RuntimeException {
        //read the descriptor file here.
        try {
            final String ons = new StringBuilder().append(AdminService.PRIVATE_MBEAN_DOMAIN_NAME).append(":type=cluster,category=config").append(",name=").append(cluster).toString();
            final ObjectName on = new ObjectName(ons);
            return (on);
        }  catch(final Exception e) {
            throw new RuntimeException(e);
        }
    }
    public static ObjectName getClustersConfigMBeanObjectName() throws RuntimeException {
        //read the descriptor file here.
        try {
            final String ons = new StringBuilder().append(AdminService.PRIVATE_MBEAN_DOMAIN_NAME).append(":type=clusters,category=config").toString();
            final ObjectName on = new ObjectName(ons);
            return (on);
        }  catch(final Exception e) {
            throw new RuntimeException(e);
        }
    }
}
