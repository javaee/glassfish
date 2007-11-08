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

/*
 *   EventListener of AdminConfig Changes  - keeps consistent MBean space
 *   ajusting it according AdminConfigContext changes
 *
 *   $Id: AdminConfigEventListener.java,v 1.3 2005/12/25 03:47:36 tcfujii Exp $
 *   @author: alexkrav
 *
 *   $Log: AdminConfigEventListener.java,v $
 *   Revision 1.3  2005/12/25 03:47:36  tcfujii
 *   Updated copyright text and year.
 *
 *   Revision 1.2  2005/06/27 21:19:43  tcfujii
 *   Issue number: CDDL header updates.
 *
 *   Revision 1.1.1.1  2005/05/27 22:52:02  dpatil
 *   GlassFish first drop
 *
 *   Revision 1.2  2004/11/14 07:04:20  tcfujii
 *   Updated copyright text and/or year.
 *
 *   Revision 1.1  2004/04/05 16:44:05  kravtch
 *   admin/meta/AdminConfigEventListener: new configcontext listener's code
 *   This listener is for synchronization of ConfigBeans changes with both MBeans and dotted-name spaces.
 *   admin/meta/MBeanRegistry: added methods (adoptConfigBeanDelete/Add) implementing beans ajustment
 *   admin/config/BaseConfigMBean: calls from MBean's postRegister/unregister methods to dotted-name-manager is commented out.
 *
 *   Reviewer: Sridatta
 *   Tests Passed: QuickLook +  UnitTest
 *
*/

package com.sun.enterprise.admin.meta;

import com.sun.enterprise.config.ConfigBean;
import com.sun.enterprise.config.ConfigContextEvent;
import com.sun.enterprise.config.ConfigContextEventListener;


public class  AdminConfigEventListener implements ConfigContextEventListener
{
    /**
        Creates new <code>AdminConfigEventListener</code>.
    */
    public AdminConfigEventListener()
    {
        super();
    }

    // "dummy" methods to satisfy interface
    public void preAccessNotification(ConfigContextEvent ccce) {}
    public void postAccessNotification(ConfigContextEvent ccce) {}
    public void preChangeNotification(ConfigContextEvent ccce) {}

    /**
     * after config add, delete, set, update or flush. type is in ccce
     */
    public void postChangeNotification(ConfigContextEvent event) 
    {
        boolean bAdded;
        //set bAdded (shows whether bean added or removed)
        if((event.getType()).equals(event.POST_DELETE_CHANGE))
            bAdded = false;
        else if((event.getType()).equals(event.POST_ADD_CHANGE))
            bAdded = true;
        else if((event.getType()).equals(event.POST_SET_CHANGE))
            bAdded = true;
        else
            return;
        //here we are only for add/set/delete operations
        //we are interesting only in bean operations:
        Object bean = event.getObject();
        if(bean instanceof ConfigBean)
        {
            String domainName = MBeanRegistryFactory.getAdminContext().getDomainName();
            if(bAdded)
                MBeanRegistryFactory.getAdminMBeanRegistry().adoptConfigBeanAdd((ConfigBean)bean, domainName);
            else
                MBeanRegistryFactory.getAdminMBeanRegistry().adoptConfigBeanDelete((ConfigBean)bean, domainName);
        }
    }
}
