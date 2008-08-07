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
package com.sun.appserv.management.ext.coverage;

import javax.management.MBeanInfo;
import java.util.Map;
import java.util.Set;


/**
	Information about code coverage of an MBean.
 */
public interface CoverageInfo
{
    /** get the current MBeanInfo, possibly null if not yet set */
    public MBeanInfo    getMBeanInfo();
    
    /**
        Get the Set of legal attributes which can be read.
     */
    public Set<String>  getReadableAttributes();
    
    /**
        Get the Set of legal attributes which can be read.
     */
    public Set<String>  getWriteableAttributes();
    
    /**
        Get the Set of legal operations which can be invoked.
        Format: opname(<signature>);
     */
    public Set<String>  getOperations();
    
    /**
        Get the Set of legal attributes which were read.
     */
    public Set<String>  getAttributesRead();
    
    /**
        Get the Set of legal attributes which were NOT read.
     */
    public Set<String>  getAttributesNotRead();
    
    
    /**
        Get the Set of legal attributes which were written.
     */
    public Set<String>  getAttributesWritten();
    
    /**
        Get the Set of legal attributes which were NOT written.
     */
    public Set<String>  getAttributesNotWritten();
    
    /**
        Get the Set of legal operations which were invoked.
     */
    public Set<String>  getOperationsInvoked();
    
    /**
        Get the Set of legal operations which were NOT invoked.
     */
    public Set<String>  getOperationsNotInvoked();
    
    /**
        Get a Map&lt;attribute name, count&gt; of Attribute read failures
        for legal attributes.
     */
    public Map<String,Integer>  getAttributeGetFailures();
    
    /**
        Get a Map&lt;attribute name, count&gt; of Attribute write failures
        for legal attributes.
     */
    public Map<String,Integer>  getAttributeSetFailures();
    
    /**
        Get a Map&lt;attribute name, count&gt; of unknown Attribute accesses.
     */
    public Map<String,Integer>  getUnknownAttributes();
    
    /**
        Get a Map&lt;operation name, count&gt; of unknown operation accesses.
     */
    public Map<String,Integer>  getUnknownOperations();

    public Map<String,Integer>  getInvocationFailures();
    
    /**
        Produce a useful string for current information.
     */
    public String       toString( final boolean   verbose );
    
    /** 0-100%, based on legal readable Attributes */
    public  int         getAttributeReadCoverage();
    
    /** 0-100%, based on legal writeable Attributes */
    public  int         getAttributeWriteCoverage();
    
    /** 0-100%, based on legal operations */
    public  int         getOperationCoverage();
    
    /** @return true if 100% coverage, false otherwise */
    public  boolean         getFullCoverage();

//-------------------------------------------------------------------------------------------------------
   /** reset coverage data to empty */
    public void         clear();
    
    /**
        Set the current MBeanInfo.  Should be set prior to calling
        other routines because it is used to recognize unknown Attributes
        and operations.
      */
    public void         setMBeanInfo( final MBeanInfo mbeanInfo );
    public void         merge( final CoverageInfo info );
    
    /**
       Remove the Attribute from the list of unknown Attributes.
       Used to "clean up" the output for known transgressions.
     */
    public void         ignoreUnknownAttribute( final String name );
    
    /**
       Record the fact that a request was made to read the Attribute.
     */
    public void         attributeWasRead( final String name );
    
    /**
       Record the fact that a request was made to read the Attributes.
     */
    public void         attributesWereRead( final String[] names );
    
    /**
       Record the fact that a failure occurred while reading the Attribute.
     */
    public void         attributeGetFailure( final String name );
    
    /**
       Record the fact that a request was made to write the Attribute.
     */
    public void         attributeWasWritten( final String name );
    
    /**
       Record the fact that a failure occurred while writing the Attribute.
     */
    public void         attributeSetFailure( final String name );

    /**
       Record the fact that a request was made to invoke the operation.
     */
    public void         operationWasInvoked(final String name, final String[]  sig);
    
    /**
       Mark the operation as having been invoked.  Usually used to be able to ignore
       certain known cases of operations that can't be invoked remotely,
       such as add/removeNotificationListener (certain forms).
     */
    public void         markAsInvoked(final String fullname );
    
    /**
       Record the fact that a failure occurred while invoking the operation.
     */
    public void         operationFailed(final String name, final String[] sig);


}







































