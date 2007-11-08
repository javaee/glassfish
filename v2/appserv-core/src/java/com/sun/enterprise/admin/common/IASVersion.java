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

package com.sun.enterprise.admin.common;

//JDK imports
import java.io.Serializable;
import java.util.Date;

/**
	Holder class for IAS version & licensing info
*/
public class IASVersion implements Serializable
{
    /* javac 1.4 generated serialVersionUID */
	public static long serialVersionUID =  -6600350216728544568L;

    //VERSION
    private String m_ProductName;
	private String m_Version;
	private String m_MajorVersion;
	private String m_MinorVersion;
	private String m_BuildVersion;
    
    //LICENSE
    private String m_LicenseDescription;
    private String m_LicenseType;
    private Date   m_LicenseExpirationDate;
    private Date   m_LicenseStartDate;
    
        
    /** 
            Creates new Status.
    */
    public IASVersion(  String ProductName, 
                        String Version,
                        String MajorVersion,
                        String MinorVersion,
                        String BuildVersion,
                        String LicenseDescription,
                        String LicenseType,
                        Date   LicenseExpirationDate,
                        Date   LicenseStartDate )
    {
        m_ProductName   = ProductName;
        m_Version       = ProductName;
        m_MajorVersion  = ProductName;
        m_MinorVersion  = MinorVersion;
        m_BuildVersion  = BuildVersion;
        
        m_LicenseDescription = LicenseDescription;
        m_LicenseType = LicenseType;
        m_LicenseExpirationDate = LicenseExpirationDate;
        m_LicenseStartDate = LicenseStartDate;
    }

    public String getProductName() 
    {
        return m_ProductName;
    }
	public String getVersion() 
    {
        return m_Version;
    }
	public String getMajorVersion() 
    {
        return m_MajorVersion;
    }
	public String getMinorVersion() 
    {
        return m_MinorVersion;
    }
	public String getBuildVersion() 
    {
        return m_BuildVersion;
    }
    
    
    public String getLicenseDescription() 
    {
        return m_LicenseDescription;
    }
    public String getLicenseType() 
    {
        return m_LicenseType;
    }
    public Date   getLicenseExpirationDate() 
    {
        return m_LicenseExpirationDate;
    }
    public Date   getLicenseStartDate() 
    {
        return m_LicenseStartDate;
    }
}