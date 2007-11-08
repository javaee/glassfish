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
package com.sun.enterprise.admin.server.core.jmx;

public class ServiceName
{
    /** 
	Private constructor to avoid creation.
    */
    private ServiceName()
    {
    }
    /**
	The object name of the MBeanServer delegate object
	<BR>
	The value is <CODE>JMImplementation:type=MBeanServerDelegate</CODE>.
     */
    public static final String DELEGATE        = "JMImplementation:type=MBeanServerDelegate" ;
        
    /**
	The default domain.
	<BR>
	The value is <CODE>DefaultDomain</CODE>.
    */
    public static final String DOMAIN       = "DefaultDomain";

    /**
	The name of the JMX specification implemented by this product.    
	<BR>
	The value is <CODE>Java Management Extensions</CODE>.
    */
    public static final String JMX_SPEC_NAME = "Java Management Extensions";
    
    /**
	The version of the JMX specification implemented by this product.
	The value is <CODE>1.0 Final Release</CODE>.
    */
    public static final String JMX_SPEC_VERSION = "1.0 Final Release";
    
    /**
	The vendor of the JMX specification implemented by this product.     
	<BR>
	The value is <CODE>Sun Microsystems </CODE>.
    */
    public static final String JMX_SPEC_VENDOR = "Sun Microsystems";
    
    /**
     * The name of this product implementing the  JMX specification.
     * <BR>
     * The value is <CODE>iPlanet Application Server</CODE>.
     */
    public static final String JMX_IMPL_NAME = "iPlanet Application Server";
    
    /**
     * The name of the vendor of this product implementing the  JMX specification.  
     * <BR>
     * The value is <CODE>iPlanet</CODE>.
     */
    public static final String JMX_IMPL_VENDOR = "iPlanet";
    
    /**
     * The version of this product implementing the  JMX specification.  
     * <BR>
     * The value is <CODE>1.0</CODE>.
     */
    public static final String JMX_IMPL_VERSION = "7.0";

    /**
     * The build number of the current product version 
     */
    public static final String BUILD_NUMBER = "1";
}
