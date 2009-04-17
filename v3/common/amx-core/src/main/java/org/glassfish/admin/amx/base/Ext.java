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
package org.glassfish.admin.amx.base;

import org.glassfish.admin.amx.annotation.*;

import org.glassfish.admin.amx.core.AMXProxy;
import org.glassfish.api.amx.AMXMBeanMetadata;


/**
    "Ex" = extensions: groups most domain-level utility and singleton MBeans,
    some of which might have other dependencies beyond amx-core.
 * <p>
 * Well-known children have explicit getters, but there could be many
 * more children made available at runtime by other modules.
 */
@AMXMBeanMetadata(type="ext", singleton=true)
public interface Ext extends AMXProxy, Singleton, Utility
{
    @ManagedAttribute
    public RealmsMgr getRealmsMgr();
    
    @ManagedAttribute
    public RuntimeMgr getRuntimeMgr();
    
    @ManagedAttribute
    public LoggingPropertiesMgr getLoggingPropertiesMgr();
    

    /**
        Get all the {@link LoadBalancer} instances
        @return Map of items, keyed by name.
        @see LoadBalancer
        @see com.sun.appserv.management.config.LoadBalancerConfig
        @since AppServer 9.0
    public Map<String,LoadBalancer> getLoadBalancerMap();
     */

    /**
       Contacts Update Center Server and get the updates status.
     */
    @ManagedAttribute
    public UpdateStatus  getUpdateStatus();
        
    /**
            Get the NotificationServiceMgr
    
    @ManagedAttribute
    public NotificationServiceMgr	getNotificationServiceMgr();
    */

    /**
        @return the singleton SystemInfo
     */
    @ManagedAttribute
    public SystemStatus		getSystemStatus();
	
    /**
        @return the singleton KitchenSink
     */
    @ManagedAttribute
    public KitchenSink		getKitchenSink();

    /**
        @return the singleton {@link QueryMgr}.
     */
    @ManagedAttribute
    public QueryMgr		getQueryMgr();

    /**
        @return the singleton {@link BulkAccess}.
     */
    @ManagedAttribute
    public BulkAccess		getBulkAccess();

    /**
       @return the singleton {@link UploadDownloadMgr}.
     */
    @ManagedAttribute
    public UploadDownloadMgr		getUploadDownloadMgr();

    /**
        @return the singleton {@link Pathnames}.
     */
    @ManagedAttribute
    public Pathnames		getPathnames();


    /**
            Get all {@link NotificationEmitterService} instances.
            Possible kinds include those defined in {@link NotificationEmitterServiceKeys}.
            @since AppServer 9.0

    @ManagedAttribute
    public Map<String,NotificationEmitterService>
            getNotificationEmitterServiceMap();
*/
    /**
        Get the NotificationEmitterService whose name is
        {@link NotificationEmitterServiceKeys#DOMAIN_KEY}.  Same
        as calling <code>getNotificationEmitterServiceMap().get( DOMAIN_KEY )</code>.
       @return the singleton {@link NotificationEmitterService}.

    @ManagedAttribute
    public NotificationEmitterService 	getDomainNotificationEmitterService();
*/
}















