/*
 * The contents of this file are subject to the terms 
 * of the Common Development and Distribution License 
 * (the License).  You may not use this file except in
 * compliance with the License.
 * 
 * You can obtain a copy of the license at 
 * https://glassfish.dev.java.net/public/CDDLv1.0.html or
 * glassfish/bootstrap/legal/CDDLv1.0.txt.
 * See the License for the specific language governing 
 * permissions and limitations under the License.
 * 
 * When distributing Covered Code, include this CDDL 
 * Header Notice in each file and include the License file 
 * at glassfish/bootstrap/legal/CDDLv1.0.txt.  
 * If applicable, add the following below the CDDL Header, 
 * with the fields enclosed by brackets [] replaced by
 * you own identifying information: 
 * "Portions Copyrighted [year] [name of copyright owner]"
 * 
 * Copyright 2006 Sun Microsystems, Inc. All rights reserved.
 */

/* Version.java
 * $Id: Version.java,v 1.3 2005/12/25 04:26:35 tcfujii Exp $
 * $Revision: 1.3 $
 * $Date: 2005/12/25 04:26:35 $
 * Indentation Information:
 * 0. Please (try to) preserve these settings.
 * 1. Tabs are preferred over spaces.
 * 2. In vi/vim -
 *		:set tabstop=4 :set shiftwidth=4 :set softtabstop=4
 * 3. In S1 Studio -
 *		1. Tools->Options->Editor Settings->Java Editor->Tab Size = 4
 *		2. Tools->Options->Indentation Engines->Java Indentation Engine->Expand Tabs to Spaces = False.
 *		3. Tools->Options->Indentation Engines->Java Indentation Engine->Number of Spaces per Tab = 4.
 */


package com.sun.enterprise.admin.jmx.remote.protocol;

/**
 * Provides the basic interface for Version. The corresponding VersionMatcher will
 * only match for this interface. Though the implementing classes are actually
 * created from a template, it is better to have them implement a known
 * interface and this is the reason why this interface is created.
 * @author  <a href="mailto:Kedar.Mhaswade@sun.com">Kedar Mhaswade</a>
 * @since S1AS8.0
 * @version $Revision: 1.3 $
 */
public interface Version extends java.io.Serializable {
	
	public final String CLASS_NAME = "com.sun.enterprise.admin.jmx.remote.protocol.SunOneVersion";

    /**
     * Returns Major Version as an int.
     */ 
    public int getMajorVersion();
	
    /**
     * Returns Minor Version as an int.
     */
    public int getMinorVersion();

    /** 
     * Returns the Upgrade Data as a String[]. The Strings packed in this array
     * are processed by the Server in a particular manner.
     */
    public String[] getUpgradeData();
}
