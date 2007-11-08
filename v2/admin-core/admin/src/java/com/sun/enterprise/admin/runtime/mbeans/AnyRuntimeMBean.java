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
 *   $Id: AnyRuntimeMBean.java,v 1.3 2005/12/25 03:47:42 tcfujii Exp $
 *   @author: alexkrav
 *
 *   $Log: AnyRuntimeMBean.java,v $
 *   Revision 1.3  2005/12/25 03:47:42  tcfujii
 *   Updated copyright text and year.
 *
 *   Revision 1.2  2005/06/27 21:19:46  tcfujii
 *   Issue number: CDDL header updates.
 *
 *   Revision 1.1.1.1  2005/05/27 22:52:02  dpatil
 *   GlassFish first drop
 *
 *   Revision 1.3  2004/11/14 07:04:24  tcfujii
 *   Updated copyright text and/or year.
 *
 *   Revision 1.2  2004/02/20 03:56:19  qouyang
 *
 *
 *   First pass at code merge.
 *
 *   Details for the merge will be published at:
 *   http://javaweb.sfbay.sun.com/~qouyang/workspace/PE8FCSMerge/02202004/
 *
 *   Revision 1.1.4.1  2004/02/02 07:25:24  tcfujii
 *   Copyright updates notices; reviewer: Tony Ng
 *
 *   Revision 1.1  2003/07/18 20:14:47  kravtch
 *   1. ALL config mbeans are now covered by descriptors.xml
 *   2. new infrastructure for runtime mbeans is added
 *   3. generic constructors added to jsr77Mdl beans (String[])
 *   4. new test cases are added to admintest
 *   5. MBeanRegistryFactory has now different methods to obtain admin/runtime registries
 *   6. runtime-descriptors xml-file is added to build
 *
 *
 *
*/

package com.sun.enterprise.admin.runtime.mbeans;

import com.sun.enterprise.admin.runtime.*;

import javax.management.Attribute;
import javax.management.ObjectName;
import javax.management.modelmbean.ModelMBeanInfo;



/**
 * <p>Template
 *
 */

public class AnyRuntimeMBean extends BaseRuntimeMBean
{
}

