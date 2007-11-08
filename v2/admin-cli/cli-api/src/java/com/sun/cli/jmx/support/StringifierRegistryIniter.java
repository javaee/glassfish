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
 * $Header: /cvs/glassfish/admin-cli/cli-api/src/java/com/sun/cli/jmx/support/StringifierRegistryIniter.java,v 1.3 2005/12/25 03:45:51 tcfujii Exp $
 * $Revision: 1.3 $
 * $Date: 2005/12/25 03:45:51 $
 */
 
package com.sun.cli.jmx.support;

import javax.management.*;
import javax.management.modelmbean.*;
import java.util.Iterator;
import java.util.Collection;

import com.sun.cli.jmx.support.ResultsForGetSetStringifier;
import com.sun.cli.util.stringifier.*;


/*
	Registers all included stringifiers with the default registry.
 */
public final class StringifierRegistryIniter
{
		private static void
	add( Class theClass, Stringifier theStringifier )
	{
		StringifierRegistry.DEFAULT.add( theClass, theStringifier );
	}
	
		public
	StringifierRegistryIniter(  )
	{
		add( Iterator.class, IteratorStringifier.DEFAULT );
		add( Collection.class, CollectionStringifier.DEFAULT );
		add( Object.class, SmartStringifier.DEFAULT );
		
		add( MBeanInfo.class, MBeanInfoStringifier.DEFAULT );
		add( ModelMBeanInfo.class, ModelMBeanInfoStringifier.DEFAULT );
		
		add( MBeanOperationInfo.class, MBeanOperationInfoStringifier.DEFAULT );
		add( ModelMBeanOperationInfo.class, ModelMBeanOperationInfoStringifier.DEFAULT );
		
		add( MBeanAttributeInfo.class, MBeanAttributeInfoStringifier.DEFAULT );
		add( ModelMBeanAttributeInfo.class, ModelMBeanAttributeInfoStringifier.DEFAULT );
		
		add( MBeanParameterInfo.class, MBeanParameterInfoStringifier.DEFAULT );
		
		add( MBeanNotificationInfo.class, MBeanNotificationInfoStringifier.DEFAULT );
		add( ModelMBeanNotificationInfo.class, ModelMBeanNotificationInfoStringifier.DEFAULT );
		
		add( MBeanConstructorInfo.class, MBeanConstructorInfoStringifier.DEFAULT );
		add( ModelMBeanConstructorInfo.class, ModelMBeanConstructorInfoStringifier.DEFAULT );
		
		add( Attribute.class, AttributeStringifier.DEFAULT );
		add( AttributeList.class, AttributeListStringifier.DEFAULT );
		
		add( Notification.class, NotificationStringifier.DEFAULT );
		add( AttributeChangeNotification.class, AttributeChangeNotificationStringifier.DEFAULT );
		add( MBeanServerNotification.class, MBeanServerNotificationStringifier.DEFAULT );
		
		add( ResultsForGetSet.class, new ResultsForGetSetStringifier( ) );
		add( InspectResult.class, new InspectResultStringifier( ) );
		add( InvokeResult.class, new InvokeResultStringifier( ) );
	}
	
}



