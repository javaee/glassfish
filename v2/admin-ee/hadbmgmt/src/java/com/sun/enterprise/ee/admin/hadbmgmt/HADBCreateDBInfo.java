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
 * HADBCreateDBProps.java
 *
 * Created on April 10, 2004, 4:43 PM
 */

package com.sun.enterprise.ee.admin.hadbmgmt;

import java.util.*;
import java.util.logging.*;

import com.sun.enterprise.config.ConfigContext;
import com.sun.enterprise.config.ConfigException;
import com.sun.enterprise.config.serverbeans.ServerHelper;
import com.sun.enterprise.admin.util.JMXConnectorConfig;
import com.sun.enterprise.config.serverbeans.Config;
import com.sun.enterprise.util.OS;
import com.sun.enterprise.util.StringUtils;
import com.sun.enterprise.util.SystemPropertyConstants;
import com.sun.enterprise.util.io.FileUtils;
import java.io.*;
import java.util.Properties;
import javax.management.MBeanServer;

/**
 * HADBInfo was getting bloated with createdb-specific stuff.
 * This class localizes the create-db specific stuff.
 * @author  bnevins
 */
public class HADBCreateDBInfo extends HADBInfo
{
	////////////////////////////////////////////////////////////////////////////
	//////  Public Methods
	////////////////////////////////////////////////////////////////////////////
	
	public HADBCreateDBInfo(
		String hosts, 
		String agentPort, 
		String theAdminPassword, 
		String userPasswordFile, 
		Boolean autohadb,
		String portbase,
		String clusterOrDbName, 
		Logger logger, 
		ConfigContext configCtx, 
		MBeanServer mbeanServer
		) throws HADBSetupException
	{
		super(hosts, agentPort, theAdminPassword, userPasswordFile, clusterOrDbName, logger, configCtx, mbeanServer);
		setAutoHadb(autohadb);
		setPortBase(portbase);
	}
	
	////////////////////////////////////////////////////////////////////////////

	public final void setDeviceSize(String size) throws HADBSetupException
	{
		int newDeviceSize = 0;
		
		if(size == null || size.length() <= 0)
			return;
		
		try
		{
			newDeviceSize = Integer.parseInt(size);
			
			if(newDeviceSize < MIN_DEVICE_SIZE || newDeviceSize > MAX_DEVICE_SIZE)
				throw new NumberFormatException();
			
			deviceSize = newDeviceSize;
		}
		catch(NumberFormatException nfe)
		{
			throw new HADBSetupException("hadbmgmt-res.BadDeviceSize",
			new String[]
			{"" + MIN_DEVICE_SIZE, "" + MAX_DEVICE_SIZE, size});
		}
	}
	
	////////////////////////////////////////////////////////////////////////////

	public final void setProperties(Properties p) throws HADBSetupException
	{
		props = HADBUtils.props2hash(p);
		parseProps();
	}

	////////////////////////////////////////////////////////////////////////////
	//////  Package-Private Methods
	////////////////////////////////////////////////////////////////////////////

	final String[] getCreateCommands() throws HADBSetupException
	{
		List list = new ArrayList(32);
		
		list.add("create");
		list.add("--quiet");
		
		

		// October 2005
		// HADB's default is no-cleanup.  Our default is yes-cleanup!
		// January 2006 -- HADB version < 4.5 does not have a "--cleanup" option, it only
        // has a "--no-cleanup" option.
        
        double ver = getVersion().getMajor();
        boolean old = ver < 4.5;
        
        if(doCleanup) // by default this is true
        {
            if(old)
                list.add("--no-cleanup=false");
            else
                list.add("--cleanup=true");
        }
		else
        {
            if(old)
                list.add("--no-cleanup=true");
            else
                list.add("--cleanup=false");
        }
		
		if(getHistoryPath() != null)
			list.add("--historypath="				+ getHistoryPath());
		
		if(getDevicePath() != null)
			list.add("--devicepath="				+ getDevicePath());
		
		list.add("--datadevices="				+ "" + getDataDevices());
		list.add("--portbase="					+ "" + getPortBase());
		list.add("--spares="					+ "" + getSpares());
		list.add("--devicesize="				+ deviceSize);
		list.add(getJavaRootArg());
		list.add(getAdminPasswordManager().getArg());
		list.add(getDBPasswordManager().getArg());
		list.add("--hosts="						+ getHosts());
		list.add(getAgentURLArg());
		list.addAll(extraArgs);
		list.add(hadbmSetProps2String());
		list.add(getClusterName());

		String[] args = new String[list.size()];
		return (String[])list.toArray(args);
	}
	
	////////////////////////////////////////////////////////////////////////////

	@Override
	void validate() throws HADBSetupException
	{
		// make sure the device size is OK.
		boolean noModifyLogBufferSize = false;
		
		if(logBufferSize > 0)
		{
			// user set it -- do NOT adjust it
			noModifyLogBufferSize = true;
		}
		else
		{
			// user didn't set it...
			logBufferSize = DEFAULT_LOG_BUFFER_SIZE;
		}
		
		if(deviceSize < MIN_DEVICE_SIZE || deviceSize > MAX_DEVICE_SIZE)
			throw new HADBSetupException("hadbmgmt-res.BadDeviceSize",
				new String[] {"" + MIN_DEVICE_SIZE, "" + MAX_DEVICE_SIZE, "" + deviceSize});

		// devicesize must be at least 4*LogBufferSize + 16
			
		if(deviceSize < ((4 * logBufferSize) + 16))
		{
			if(noModifyLogBufferSize)
				throw new HADBSetupException("hadbmgmt-res.BadDeviceSizeFixedLogBufferSize",
					new String[] {"" + MIN_DEVICE_SIZE, "" + MAX_DEVICE_SIZE, "" + logBufferSize, "" + deviceSize});

			// this is GUARANTEED to be < min-LogBufferSize (4MB)		
			logBufferSize = (deviceSize - 16) / 4;
			LoggerHelper.warning("hadbmgmt-res.AdjustedLogBufferSize", DEFAULT_LOG_BUFFER_SIZE, logBufferSize);						
		}
	}
	
	////////////////////////////////////////////////////////////////////////////

	final int getLogBufferSize()
	{
		return logBufferSize;
	}
	
	////////////////////////////////////////////////////////////////////////////

	final int getPortBase()
	{
		return portBase;
	}
	
	////////////////////////////////////////////////////////////////////////////
	//////  Private Methods
	////////////////////////////////////////////////////////////////////////////

	private final void parseProps() throws HADBSetupException
	{
		if(props == null || props.size() <= 0)
			return;
		
		// first -- handle (and remove!) props that have an official "--" arg for hadbm create
		parseAndRemoveOfficalArgs();
		parseOtherArgs();
		checkLeftoverArgs();
	}
	
	////////////////////////////////////////////////////////////////////////////

	private final void parseAndRemoveOfficalArgs() throws HADBSetupException
	{
		if(props == null)
			return;
		
		String pathErr = "hadbmgmt-res.BadPathInProperty";
		
		String key = "historypath";
		String s = props.get(key);
		if(ok(s))
		{
			historyPath = checkPath(s, pathErr, key + "=" + s);
			props.remove(key);
		}

		key = "devicepath";
		s = props.get(key);
		if(ok(s))
		{
			devicePath = checkPath(s, pathErr, key + "=" + s);
			props.remove(key);
		}
		
		key = "datadevicesize";
		s = props.get(key);
		if(ok(s))
		{
			setDeviceSize(s);
			props.remove(key);
		}
		
		key = "datadevices";
		s = props.get(key);
		if(ok(s))
		{
			dataDevices = parseInt(s, 1, 1000, DEFAULT_DATA_DEVICES);
			props.remove(key);
		}
		
		/* 2 ways to specify data devices -- make sure they are both gone! */
		key = "numberofdatadevices";
		s = props.get(key);
		if(ok(s))
		{
			dataDevices = parseInt(s, 1, 1000, DEFAULT_DATA_DEVICES);
			props.remove(key);
		}
		
		key = "portbase";
		s = props.get(key);
		if(ok(s))
		{
			setPortBase(s);
			props.remove(key);
		}

		key = "spares";
		s = props.get(key);
		if(ok(s))
		{
			spares = parseInt(s, 0, 1000, DEFAULT_SPARES);
			props.remove(key);
		}
		
		key = "agent";
		s = props.get(key);
		if(ok(s))
		{
			agentURLOverride = s;
			props.remove(key);
		}
		
		key = "dbpassword";
		s = props.get(key);
		if(ok(s))
		{
			setDatabasePassword(s, true);
			props.remove(key);
		}
		//s = props.getProperty("adminpassword");
		//if(ok(s))
			//setAdminPassword(s, true);
		
		// WBN October 2005
		// HADB 4.5.0-2 has changed the default behavior.
		// Now HADB's default is to NOT cleanup.
		// In order to cleanup -- we have to supply the
		// --cleanup=true option
		// we automatically send in the "--cleanup" arg now for every create call.  By default it is "--cleanup=true"
		// unless the user overrides it by setting: "--no-cleanup=true or nocleanup=true or cleanup=false
		// I implemented it by setting an instance variable -- doCleanup -- default is true.
		// in this code we simply set the boolean, doCleanup.  getCreateCommands() will then
		// setup the option value correctly...
		
		key = "--no-cleanup";
		if(props.containsKey(key))
		{
			s = props.get(key);
			if(!ok(s) || s.equals("true"))
				// Oct 2005 change
				doCleanup = false;
			props.remove(key);
		}
		key = "nocleanup";
		if(props.containsKey(key))
		{
			// true or no-value == no-cleanup
			// false == cleanup
			s = props.get(key);
			if(!ok(s) || s.equals("true"))
				doCleanup = false;
			props.remove(key);
		}

		key = "cleanup";
		if(props.containsKey(key))
		{
			// true or no-value == no-cleanup
			// false == cleanup
			s = props.get(key);
			if(ok(s) && s.equals("false"))
				doCleanup = false;
			props.remove(key);
		}

		key = "--no-clear";
		if(props.containsKey(key))
		{
			// true or no-value == no-clear
			// false == clear - which is the default action
			s = props.get(key);
			if(!ok(s) || s.equals("true"))
				extraArgs.add("--no-clear");
			props.remove(key);
		}
		key = "noclear";
		if(props.containsKey(key))
		{
			// true or no-value == no-clear
			// false == clear - which is the default action
			s = props.get(key);
			if(!ok(s) || s.equals("true"))
				extraArgs.add("--no-clear");
			props.remove(key);
		}
	}

	/**
	 * Look for other legal "set" args for hadbm create
	 */
	private final void parseOtherArgs()
	{
		// this may look inefficient -- but there are probably just a few (zero one or two)
		// props here...
		
		if(props == null || props.size() <= 0)
			return;
		
		// important:  props keys have been converted to lowercase!
		for(String arg : HADBUtils.validCreateAttributes)
		{
			String key = arg.toLowerCase();
			String value = props.get(key);
			
			if(ok(value))
			{
				if(key.equals("logbuffersize"))
				{
					// treated specially -- don't add to --set command now.  
					// We may change it later...
					logBufferSize = parseInt(value, 4, 2047, 0);
					
					if(logBufferSize == 0)
						LoggerHelper.warning("hadbmgmt-res.BadLogBufferSize", value);						
				}
				else
					hadbmSetProps.put(arg, value);
				
				props.remove(key);
			}
		}
	}

	/**
	 * Look for other legal "set" args for hadbm create
	 */
	private final String hadbmSetProps2String()
	{
		// we ALWAYS set LogBufferSize...
		StringBuilder setstr = new StringBuilder("--set=LogBufferSize=" + logBufferSize);
		Set<Map.Entry<String,String>> set = hadbmSetProps.entrySet();
		
		for(Map.Entry<String,String> entry : set)
			setstr.append(",").append(entry.getKey()).append("=").append(entry.getValue());
		
		return setstr.toString();
	}

	/**
	 * It is an error to have other unknown args
	 */
	private final void checkLeftoverArgs() throws HADBSetupException
	{
		if(props.size() > 0)
		{
			// FIXME -- temporarily make this a warning until GUI is fixed-up
			// set to an error June 24, 2005
			
			throw new HADBSetupException("hadbmgmt-res.UnknownProperties", props);
			
			//LoggerHelper.warning("hadbmgmt-res.UnknownProperties", props);
		}
	}
	
	////////////////////////////////////////////////////////////////////////////

	private final String getHistoryPath()
	{
		/* WBN -- no default is now the default
		if(historyPath == null)
		{
			File f = new File(getHadbRoot(), DEFAULT_HISTORY_SUBDIR);
			f.mkdirs();
			historyPath = f.getAbsolutePath();
		}
		*/
		return historyPath;
	}
	
	////////////////////////////////////////////////////////////////////////////

	private final String getDevicePath()
	{
		/* WBN -- no default is now the default
		if(devicePath == null)
		{
			File f = new File(getHadbRoot(), DEFAULT_DEVICE_SUBDIR);
			f.mkdirs();
			devicePath = f.getAbsolutePath();
		}
		*/
		return devicePath;
	}


	////////////////////////////////////////////////////////////////////////////

	String getAgentURLArg() throws HADBSetupException
	{
		if(agentURLOverride != null)
			return "--agent=" + agentURLOverride;
		else
			return super.getAgentURLArg();
	}

	////////////////////////////////////////////////////////////////////////////

	private final int getDataDevices()
	{
		return dataDevices;
	}
	
	////////////////////////////////////////////////////////////////////////////

	private final int getSpares()
	{
		return spares;
	}
	
	////////////////////////////////////////////////////////////////////////////

	private final boolean ok(String s)
	{
		return (s != null) && (s.length() > 0); 
	}
	
	////////////////////////////////////////////////////////////////////////////

	private final int parseInt(String s, int min, int max, int defaultValue)
	{
		try
		{
			int num = Integer.parseInt(s);
			
			if(num >= min && num <= max)
				return num;
		}
		catch(Exception e)
		{
			// drop through...
		}
		
		return defaultValue;
		
	}
	
	////////////////////////////////////////////////////////////////////////////

	private final String checkPath(String path, String errorMessage, String errorArg) throws HADBSetupException
	{
		if(path == null)
			throw new HADBSetupException("hadbmgmt-res.InternalError", "null path argument sent to checkPath()");

		File f = new File(path);
		f.mkdirs();
		
		if(!f.isDirectory())
			throw new HADBSetupException(errorMessage, errorArg);

		return f.getAbsolutePath();
	}
	
	///////////////////////////////////////////////////////////////////////////

	private final void setPortBase(String s)
	{
		portBase = DEFAULT_PORT_BASE;

		if(ok(s))
			portBase = parseInt(s, 1, 65535, DEFAULT_PORT_BASE);
	}
	
	///////////////////////////////////////////////////////////////////////////

	private					boolean						doCleanup		= true;
	private					ArrayList					extraArgs		= new ArrayList();
	private					Hashtable<String,String>	hadbmSetProps	= new Hashtable<String,String>();
	private					String						historyPath;
	private					String						devicePath;
	private					String						agentURLOverride;
	private					int							deviceSize			= DEFAULT_DEVICE_SIZE;
	private					int							dataDevices			= DEFAULT_DATA_DEVICES;
	private					int							portBase			= DEFAULT_PORT_BASE;
	private					int							spares				= DEFAULT_SPARES;
	private					int							logBufferSize		= 0;
	private					Hashtable<String,String>	props;
	private static final	int							MAX_DEVICE_SIZE			= 262144;
	private static final	int							MIN_DEVICE_SIZE			= 32;
	private static final	int							DEFAULT_DEVICE_SIZE		= 512;
	private static final	int							DEFAULT_DATA_DEVICES	= 1;
	private static final	int							DEFAULT_PORT_BASE		= 15200;
	private static final	int							DEFAULT_SPARES			= 0;
	private static final	String						DEFAULT_DEVICE_SUBDIR	= "device";
	private static final	String						DEFAULT_HISTORY_SUBDIR	= "history";
	private static final	int							DEFAULT_LOG_BUFFER_SIZE = 48; // MBytes
}
