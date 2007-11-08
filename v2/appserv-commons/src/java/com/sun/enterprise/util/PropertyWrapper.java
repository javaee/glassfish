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

//	NOTE: Tabs are used instead of spaces for indentation. 
//  Make sure that your editor does not replace tabs with spaces. 
//  Set the tab length using your favourite editor to your 
//  visual preference. 

/* 
 * Filename: ServerChannel.java 
 * 
 * Copyright 2000-2001 by iPlanet/Sun Microsystems, Inc., 
 * 901 San Antonio Road, Palo Alto, California, 94303, U.S.A. 
 * All rights reserved. 
 * 
 * This software is the confidential and proprietary information 
 * of iPlanet/Sun Microsystems, Inc. ("Confidential Information"). 
 * You shall not disclose such Confidential Information and shall 
 * use it only in accordance with the terms of the license 
 * agreement you entered into with iPlanet/Sun Microsystems. 
 */ 
  
/** 
 * <BR> <I>$Source: /cvs/glassfish/appserv-commons/src/java/com/sun/enterprise/util/PropertyWrapper.java,v $</I> 
 * @author     $Author: tcfujii $ 
 * @version    $Revision: 1.3 $ $Date: 2005/12/25 04:12:03 $ 
 */ 

package com.sun.enterprise.util;

import java.util.Properties;
import java.io.IOException;
import java.io.InputStream;
//Bug 4677074 begin
import java.util.logging.Logger;
import java.util.logging.Level;
import com.sun.logging.LogDomains;
//Bug 4677074 end

public class PropertyWrapper extends java.lang.Object implements java.io.Serializable
{
//Bug 4677074 begin
    static Logger _logger=LogDomains.getLogger(LogDomains.UTIL_LOGGER);
//Bug 4677074 end
    /** try a defauly property file if env name fails? */
    protected boolean bTryDefaultFileOnEnvFailure = true;
    /** maintains a flag to know if the properties were loaded successfully */
    private boolean bLoaded = false;    
    /** debug flag */
    protected boolean bDebug=false;
	/** The name of the properties file. */
	protected String properties_file_name = null;	
	/**
	*	The bucket to hold the properties.
	*	@see 	java.util.Properties
	*/
	protected Properties props=null;

	/**
	*	The constructor loads the properties from a file. The name of the file 
	*   can be specified in two ways. If an environment property name exists, 
	*   it would be the first to be given a shot. if the properties are not 
	*   loaded and flag 'bTryDefaultFileOnEnvFailure' is true, a default filename 
	*   in the propertiesFileName parameter will be given a shot.
	*	@param	propertiesFileName	The name of the file holding the properties
	*   @param  envPropName Environment name for the property
	*   @param  bTryDefaultFileOnEnvFailure try a defauly property file if env name fails?
	*/
	public PropertyWrapper(String propertiesFileName, String envPropName, boolean bTryDefaultFileOnEnvFailure)
	{
	    // First try to see if you can load properties vai the env variable
	    try
	    {
	        if(null!=envPropName)
	        {
	            SecurityManager sm = System.getSecurityManager();
	            if(null!=sm)
	                sm.checkPropertyAccess(envPropName);
	            String envPropFile = System.getProperty(envPropName);
	            //if(bDebug) System.out.println("envname=" + envPropName + ",envPropName=" + envPropFile);
//Bug 4677074 begin
		    //if(com.sun.enterprise.util.logging.Debug.enabled) _logger.log(Level.FINE,"envname=" + envPropName + ",envPropName=" + envPropFile);
//Bug 4677074 end
	            if(null!=envPropFile)
	            {
	                properties_file_name = envPropFile;
                    try
		            {
			            loadProperties();
		            }catch(IOException ioe)
		            {
    		            if(com.sun.enterprise.util.logging.Debug.enabled)
//Bug 4677074    		                System.err.println("PropertyWrapper::PropertyWrapper() > " + ioe);
//Bug 4677074 begin
				_logger.log(Level.SEVERE,"enterprise_util.dbgcntl_ioexception",ioe);
//Bug 4677074 end
		            }	            
	            }
	        }
	    } catch(SecurityException se)
	    {
//Bug 4677074	        System.out.println("PropertyWrapper::PropertyWrapper() don not have security access to " + properties_file_name + " > " + se);
//Bug 4677074 begin
		_logger.log(Level.SEVERE,"iplanet_util.security_exception",new Object[]{properties_file_name,se});
//Bug 4677074 end
	    }
        // Try to load from a property file specified checking the 'bTryDefaultFileOnEnvFailure' flag
        if( !bLoaded && bTryDefaultFileOnEnvFailure && (null!=propertiesFileName) )
        {
	        try
	        {
		        properties_file_name = propertiesFileName;
		        loadProperties();
	        }catch(IOException ioe)
	        {
    		    if(com.sun.enterprise.util.logging.Debug.enabled)
//Bug 4677074    		        System.err.println("PropertyWrapper::PropertyWrapper() > " + ioe);
//Bug 4677074 begin
				_logger.log(Level.SEVERE,"enterprise_util.dbgcntl_ioexception",ioe);
//Bug 4677074 end
		    }
	    }

        if(bLoaded && bDebug)
//Bug 4677074            System.out.println("PropertyWrapper using " + properties_file_name);
//Bug 4677074 begin
	    _logger.log(Level.FINE,"PropertyWrapper using " + properties_file_name);
//Bug 4677074 end
        else if (!bLoaded && bDebug)
//Bug 4677074            System.out.println("PropertyWrapper reports properties could not be loaded for env=" + envPropName + " or filename=" + propertiesFileName);
//Bug 4677074 begin
	    _logger.log(Level.FINE,"PropertyWrapper reports properties could not be loaded for env=" + envPropName + " or filename=" + propertiesFileName);
//Bug 4677074 end
	}

	/**
	*	The constructor loads the properties from a file. The name of the file 
	*   can be specified in two ways. If an environment property name exists, 
	*   it would be the first to be given a shot, if the properties are not 
	*   loaded, a default filename in the propertiesFileName parameter will be 
	*   given a shot.
	*	@param	propertiesFileName	The name of the file holding the properties
	*   @param  envPropName Environment name for the property
    */
    public PropertyWrapper(String propertiesFileName, String envPropName)
    {
        this(propertiesFileName, envPropName, true);
    }
    
    /**
	*	The constructor loads the properties from a file from the classpath.
	*	@param	propertiesFileName	The name of the file holding the properties
    */
    public PropertyWrapper(String propertiesFileName)
    {
        this(propertiesFileName, null, true);
    }
    
	/**
	*	The constructor to initialize the object with a set of properties.
	*	@param	props	The property to init the object.
	*/
	public PropertyWrapper(Properties props)
	{
		this.props=props;
	}

	/**
	*	Return the filename of the properties file.
	*	@return	the name of the properties file for the instance.
	*/
	public String getPropertiesFile()
	{
		return properties_file_name;
	}

	/**
	*	This method allows to load properties into the instance object, from the filename
	*	specified in the parameter of the constructor. Set flag bLoaded to true if the 
	*   properties are loaded and are non empty.
	*	@exception	BaseException when an error is encountered in reading the
	*	file listing the properties.
	*/
	protected void loadProperties()
        throws IOException
	{
        props = new Properties();

		try
		{
			InputStream is = ClassLoader.getSystemResourceAsStream(properties_file_name);
			props.load(is);
			if(!props.isEmpty())
			    bLoaded = true;
		}catch(IOException e)
		{
			throw e;
		}
	}

	/**
	*	This method returns the value for a particular name of a property.
	*	@param	key	The name stored in the properties file associated with a value.
	*	@return	The value of the key in the properties,	null in case the key is not found.
	*/
	protected String getProperty(String key)
	{
		return getProperty(key, null);
	}

	/**
	*	This method returns the value for a particular name of a property with a default
	*	value supplied along.
	*	@param	key	The name stored in the properties file associated with a value.
	*	@param	defaultVal	The default value to be returned if the key is not found.
	*	@return	The value of the key.
	*	@see #getProperty(java.lang.String)
	*/
	protected String getProperty(String key, String defaultVal)
	{
		return props.getProperty(key, defaultVal);
	}
	
	/**
	* Read the string property and convert it to an int.
	* @return int -1 on failure
    */
	protected int getIntProperty(String key)
	{
	    String str = getProperty(key);
	    if(null==str)
	        return -1;	  
	    try
	    {
	        return Integer.parseInt(str);
	    }catch(NumberFormatException nfe)
	    {
	        return -1;
	    }
	}
	
	/**
	* Read the string property and convert it to an int.
	* @return int -1 on conversion failure, default if no property found, 
	* converted value otherwise
    */
	protected int getIntProperty(String key, int defaultVal)
	{
	    String str = getProperty(key);
	    if(null==str)
	        return defaultVal;
	    try
	    {
	        return Integer.parseInt(str);
	    }catch(NumberFormatException nfe)
	    {
	        return -1;
	    }
	}
	
	/**
	* Read the string property [true | false] and convert it into a boolean.
	* @return boolean true or false, default if no property found
	*/
	protected boolean getBooleanProperty(String key, boolean defaultVal)
	{
	    String str = getProperty(key);
	    if(null==str)
	        return defaultVal;
	    if(str.toLowerCase().startsWith("true"))
	        return true;
	    if(str.toLowerCase().startsWith("false"))
	        return false;
	    return defaultVal;
	}
	
	/**
	* Read the string property and convert it to an long.
	* @return long -1 on failure
    */
	protected long getLongProperty(String key)
	{
	    String str = getProperty(key);
	    if (str == null)
	        return -1;	  
	    try {
	        return Long.parseLong(str);
	    } catch(NumberFormatException nfe) {
	        return -1;
	    }
	}
	
	/**
	* Read the string property and convert it to an long.
	* @return long -1 on conversion failure, default if no property found, 
	* converted value otherwise
    */
	protected long getLongProperty(String key, long defaultVal)
	{
	    String str = getProperty(key);
	    if (str == null)
	        return defaultVal;
	    try {
	        return Long.parseLong(str);
	    } catch(NumberFormatException nfe) {
	        return -1;
	    }
	}
	
}
