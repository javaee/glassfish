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
package org.glassfish.admin.amx.util;

import javax.management.MBeanInfo;
import java.io.Serializable;
import java.util.Collections;
import java.util.Map;
import java.util.Set;

/**
	Dummy implementation used when coverage is not being gathered.
	@see AMXDebugStuff
 */
public final class CoverageInfoDummy implements Serializable, CoverageInfo
{
    public static final long    serialVersionUID    = 0xABCDEF;
    
        public
    CoverageInfoDummy()
    {
    }

    static private final Set<String>           EMPTY_SET   = Collections.emptySet();
    static private final Map<String,Integer>    EMPTY_MAP  = Collections.emptyMap();
    
    public void         clear() {}
    
    public MBeanInfo    getMBeanInfo()  { return null; }
    public void         setMBeanInfo(MBeanInfo info)  { }
    public void         merge( final CoverageInfo info )  {}
    
    public Set<String>  getReadableAttributes() { return EMPTY_SET; }
    public Set<String>  getWriteableAttributes() { return EMPTY_SET; }
    public Set<String>  getOperations() { return EMPTY_SET; }
    
    public Set<String>  getAttributesRead()  { return EMPTY_SET; }
    public Set<String>  getAttributesNotRead()  { return EMPTY_SET; }
    
    public Set<String>  getAttributesWritten()  { return EMPTY_SET; }
    public Set<String>  getAttributesNotWritten()  { return EMPTY_SET; }
    
    public Set<String>  getOperationsInvoked()  { return EMPTY_SET; }
    public Set<String>  getOperationsNotInvoked()  { return EMPTY_SET; }
    
    public Map<String,Integer>  getAttributeGetFailures()  { return EMPTY_MAP; }
    public Map<String,Integer>  getAttributeSetFailures()  { return EMPTY_MAP; }
    
    public Map<String,Integer>  getUnknownAttributes()  { return EMPTY_MAP; }
    public Map<String,Integer>  getUnknownOperations()  { return EMPTY_MAP; }
    
    public Map<String,Integer>  getInvocationFailures()  { return EMPTY_MAP; }
    
    public String       toString( final boolean   verbose ) { return "CoverageInfoDummy"; }
    
    
    public  int         getAttributeReadCoverage()  { return 0; }
    public  int         getAttributeWriteCoverage()  { return 0; }
    public  int         getOperationCoverage()  { return 0; }
    public  boolean     getFullCoverage()  { return false; }
    
    public void         ignoreUnknownAttribute( final String name )  {}
    public void         unknownAttribute( final String name ) {}
    public void         attributeWasRead( final String name )  {}
    public void         attributesWereRead( final String[] name )  {}
    public void         attributeWasWritten( final String name )  {}
    public void         attributeGetFailure( final String name ) {}
    public void         attributeSetFailure( final String name ) {}

    public void         unknownOperation(final String name, final String[]  sig) {}
    public void         operationWasInvoked(final String name, final String[]  sig) {}
    public void         markAsInvoked(final String fullname ){}
    
    public void         operationFailed(final String name, final String[] sig) {}
}








































