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
 * Copyright 2004-2005 Sun Microsystems, Inc.  All rights reserved.
 * Use is subject to license terms.
 */
 
/*
 * $Header: /cvs/glassfish/admin/mbeanapi-impl/src/java/com/sun/enterprise/management/support/oldconfig/OldConfig.java,v 1.2 2005/12/25 03:40:57 tcfujii Exp $
 * $Revision: 1.2 $
 * $Date: 2005/12/25 03:40:57 $
 */

package com.sun.enterprise.management.support.oldconfig;

import java.util.Properties;
import javax.management.Attribute;
import javax.management.ObjectName;
import javax.management.AttributeList;


public interface OldConfig
{
    public boolean  getDynamicReconfigurationEnabled();
    public void     setDynamicReconfigurationEnabled( final boolean value );

    public String   getName();
    public void     setName( final String value );

    public ObjectName       copy( final String param1, final Properties param2 );
    public ObjectName       createAdminService( final AttributeList attribute_list );
    public ObjectName       createAlertService( final AttributeList attribute_list );
    public ObjectName       createAvailabilityService( final AttributeList attribute_list );
    public ObjectName       createConnectorService( final AttributeList attribute_list );
    public ObjectName       createDiagnosticService( final AttributeList attribute_list );
    public ObjectName       createEjbContainer( final AttributeList attribute_list );
    public ObjectName       createGroupManagementService( final AttributeList attribute_list );
    public ObjectName       createHttpService( final AttributeList attribute_list );
    public ObjectName       createIiopService( final AttributeList attribute_list );
    public ObjectName       createJavaConfig( final AttributeList attribute_list );
    public ObjectName       createJmsService( final AttributeList attribute_list );
    public ObjectName       createLogService( final AttributeList attribute_list );
    public ObjectName       createManagementRules( final AttributeList attribute_list );
    public ObjectName       createMdbContainer( final AttributeList attribute_list );
    public ObjectName       createMonitoringService( final AttributeList attribute_list );
    public ObjectName       createSecurityService( final AttributeList attribute_list );
    public void     createSystemProperties( final Properties param1 );
    public ObjectName       createThreadPools( final AttributeList attribute_list );
    public ObjectName       createTransactionService( final AttributeList attribute_list );
    public ObjectName       createWebContainer( final AttributeList attribute_list );
    public void     delete();
    public void     deleteSystemProperty( final String param1 );
    public boolean  destroyConfigElement();
    public ObjectName       getAdminService();
    public ObjectName       getAlertService();
    public ObjectName       getAvailabilityService();
    public ObjectName       getConnectorService();
    public String   getDefaultAttributeValue( final String attributeName );
    public ObjectName       getDiagnosticService();
    public ObjectName       getEjbContainer();
    public ObjectName       getGroupManagementService();
    public ObjectName       getHttpService();
    public ObjectName       getIiopService();
    public ObjectName       getJavaConfig();
    public ObjectName       getJmsService();
    public ObjectName       getLogService();
    public ObjectName       getManagementRules();
    public ObjectName       getMdbContainer();
    public ObjectName       getMonitoringService();
    public AttributeList    getProperties();
    public Object   getPropertyValue( final String propertyName );
    public ObjectName       getSecurityService();
    public AttributeList    getSystemProperties();
    public Object   getSystemPropertyValue( final String propertyName );
    public ObjectName       getThreadPools();
    public ObjectName       getTransactionService();
    public ObjectName       getWebContainer();
    public boolean  isReferencedByAnyCluster();
    public boolean  isReferencedByDAS();
    public javax.management.ObjectName[]    listReferencees();
    public Properties       listSystemProperties( final boolean param1 );
    public void     removeAdminService();
    public void     removeAlertService();
    public void     removeAvailabilityService();
    public void     removeConnectorService();
    public void     removeDiagnosticService();
    public void     removeEjbContainer();
    public void     removeGroupManagementService();
    public void     removeHttpService();
    public void     removeIiopService();
    public void     removeJavaConfig();
    public void     removeJmsService();
    public void     removeLogService();
    public void     removeManagementRules();
    public void     removeMdbContainer();
    public void     removeMonitoringService();
    public void     removeSecurityService();
    public void     removeThreadPools();
    public void     removeTransactionService();
    public void     removeWebContainer();
    public void     setProperty( final Attribute nameAndValue );
    public void     setSystemProperty( final Attribute nameAndValue );
}