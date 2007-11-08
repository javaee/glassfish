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
 *  ArchiveBean.java
 */

package com.sun.jbi.jsf.bean;

import com.sun.jbi.jsf.util.JBILogger;
import java.util.Properties;
import java.util.logging.Logger;
public class ArchiveBean
{

//Get Logger to log fine mesages for debugging
private static Logger sLog = JBILogger.getInstance();

public String getArchiveAbsolutePath()
{
    String result = mArchiveAbsolutePath;
    sLog.fine("ArchiveBean.getArchiveAbsolutePath(), result=" + result); 
    return result;
}

public String getArchiveDisplayName()
{
    String result = mArchiveDisplayName;
    sLog.fine("ArchiveBean.getArchiveDisplayName(), result=" + result); 
    return result;
}

public String getDescription()
{
    String result = mDescription;
    sLog.fine("ArchiveBean.getDescription(), result=" + result); 
    return result;
}

public String getJbiName()
{
    String result = mJbiName;
	sLog.fine("ArchiveBean.getJbiName(), result=" + result); 
    return result;
}

public String getJbiType()
{
    String result = mJbiType;
    sLog.fine("ArchiveBean.getJbiType(), result=" + result); 
    return result;
}

/**
 * Get true if the provided  zip archive is not readable else false
 * @return - true if there is zipFile reading/processing error
 
 */
public  boolean getZipFileReadError ()
{
    boolean zReadError = mZipFileReadError;
    sLog.fine("ArchiveBean.getZipFileReadError(), zReadError=" + zReadError); 
    return zReadError;
}

/**
 * Get true if the provided  archive file is readable 
 * @return - true if there is error in reading the provided file
 *         - false otherwise
 */

public boolean getFileReadError ()
{
    boolean fReadError = mFileReadError;
    sLog.fine("ArchiveBean.getFileReadError(), fReadError=" + fReadError); 
    return fReadError;
}

/**
 * Get if the provided zip archive has jbi xml
 * @return  true if archive has jbi.xml file
 */
public  boolean getHasJbiXml ()
{
    boolean archHasJbi = mHasJbiXml;
    sLog.fine("ArchiveBean.getHasJbiXml(), archHasJbi=" + archHasJbi); 
    return archHasJbi;
}


public boolean isArchiveValid()
{
    boolean result = mIsArchiveValid;
    sLog.fine("ArchiveBean.isArchiveValid), result=" + result); 
    return result;

}

public void setArchiveAbsolutePath(String anArchiveAbsolutePath)
{
    sLog.fine("ArchiveBean.setArchiveAbsolutePath(" + anArchiveAbsolutePath + ")"); 
    mArchiveAbsolutePath = anArchiveAbsolutePath;
}

public void setArchiveDisplayName(String anArchiveDisplayName)
{
    sLog.fine("ArchiveBean.setArchiveDisplayName(" + anArchiveDisplayName + ")"); 
    mArchiveDisplayName = anArchiveDisplayName;
}

public void setIsArchiveValid(boolean isArchiveValid)
{
    sLog.fine("ArchiveBean.setIsArchiveValid(" + isArchiveValid + ")"); 
    mIsArchiveValid = isArchiveValid;
}

public void setDescription(String aDescription)
{
    sLog.fine("ArchiveBean.setDescription(" + aDescription + ")"); 
    mDescription = aDescription;
}

public void setJbiName(String aJbiName)
{
    sLog.fine("ArchiveBean.setJbiName(" + aJbiName + ")"); 
    mJbiName = aJbiName;
}

public void setJbiType (String aJbiType)
{
    sLog.fine("ArchiveBean.setJbiType(" + aJbiType + ")"); 
    mJbiType = aJbiType;
}

/*
 * Set to true after archive entered by user is invalidated because it cannot be read
 * @pararm aFileReadError - true , if input archive file cannot be read
 */
public void setFileReadError (boolean aFileReadError)
{
    sLog.fine("ArchiveBean.setFileReadError(" + aFileReadError + ")"); 
    mFileReadError = aFileReadError;
}


/*
 * Set to true  after zip archive entered by user is invalidated when it cannot be unzipped
 *@pararm aFileReadError - true , if input archive file is not recognised as zip file by ZipInputStream
 */
public void setZipFileReadError(boolean aZipFileReadError)
{
    sLog.fine("ArchiveBean.setZipFileReadError(" + aZipFileReadError + ")"); 
    mZipFileReadError = aZipFileReadError;
}


/*
 * Set true after zip archive entered by user is invalidated because it does not have decriptor file
 * @param aHasJbiXml - true , if input archive file misses jbi.xml in its contents
 */
public void setHasJbiXml(boolean aHasJbiXml)
{
    sLog.fine("ArchiveBean.setHasJbiXml(" + aHasJbiXml + ")"); 
    mHasJbiXml = aHasJbiXml;
}

private String mArchiveAbsolutePath;
private String mArchiveDisplayName;
private String mDescription;
private boolean mIsArchiveValid;
private String mJbiName;
private String mJbiType;
private boolean mFileReadError = false;
private boolean mZipFileReadError = false;
private boolean mHasJbiXml = true;
   
}

