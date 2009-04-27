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


import java.util.Map;
import javax.management.MBeanOperationInfo;
import javax.management.ObjectName;
import org.glassfish.admin.amx.annotation.ManagedAttribute;
import org.glassfish.admin.amx.annotation.ManagedOperation;
import org.glassfish.admin.amx.core.AMXProxy;
import org.glassfish.admin.amx.base.Singleton;
import org.glassfish.admin.amx.base.Utility;
import org.glassfish.api.amx.AMXMBeanMetadata;



/**
    Useful informational tools.
    
    @since GlassFish V3
 */
@AMXMBeanMetadata(type="tools",leaf=true, singleton=true)
public interface Tools extends AMXProxy, Utility, Singleton
{
    /** emit information about all MBeans */
    @ManagedAttribute
    public String getInfo();
    
    /** emit information about all MBeans of the specified type, or path */
    @ManagedOperation(impact=MBeanOperationInfo.INFO)
    String infoType(final String type);
    
    /** emit information about all MBeans of the specified type, or path */
    @ManagedOperation(impact=MBeanOperationInfo.INFO)
    String infoPath(final String path);
    
    /** emit information about all MBeans having the specified parent path (PP), recursively */
    @ManagedOperation(impact=MBeanOperationInfo.INFO)
    String infoPP(final String type, final boolean recursive);
    
    /** emit information about MBeans, loosey-goosey seach string eg type alone */
    @ManagedOperation(impact=MBeanOperationInfo.INFO)
    String info(final String searchString);
    
    /**
        Validate all AMX MBeans.  Return a Map key by ObjectName for all failures.
        The type of the value might be String or something else which can be displayed for
        further information.
     */
    @ManagedOperation(impact=MBeanOperationInfo.INFO)
    public String  validate();
    
    @ManagedOperation(impact=MBeanOperationInfo.INFO)
    public String validate(final ObjectName mbean);
    
    @ManagedOperation(impact=MBeanOperationInfo.INFO)
    public String validate(final ObjectName[] mbeans);
}











