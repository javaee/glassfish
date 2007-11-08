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
 * ConfigAttributeManager.java
 *
 * Created on August 8, 2005, 6:04 PM
 *
 */

package com.sun.enterprise.ee.admin.hadbmgmt;

import java.util.*;
//import com.sun.enterprise.util.StringUtils;

/**
 *
 * @author bnevins
 */

class ConfigAttributeManager
{
	ConfigAttributeManager(Properties Props)
	{
		// convert to something easier to work with: Hashtable<String,String>
		// note that all of the names in props are made lowercase.
		props = HADBUtils.props2hash(Props);
		readonlyAttributes = new Properties();
		readwriteAttributes = new Properties();
		makeProps();
	}
		
	///////////////////////////////////////////////////////////////////////////

	Properties getReadOnlyAttributes()
	{
		return readonlyAttributes;
	}
		
	///////////////////////////////////////////////////////////////////////////

	Properties getReadWriteAttributes()
	{
		return readwriteAttributes;
	}
		
	///////////////////////////////////////////////////////////////////////////
	
	private void makeProps()
	{
		for(ConfigAttribute att : configAttributes)
		{
			String val = props.get(att.name.toLowerCase());

			if(val != null && val.length() > 0)
			{
				if(att.rw == ReadWriteEnum.READWRITE)
					readwriteAttributes.setProperty(att.name, val);
				else
					readonlyAttributes.setProperty(att.name, val);
			}
		}
	}
	
	///////////////////////////////////////////////////////////////////////////
		
	final static ConfigAttribute[] configAttributes = new ConfigAttribute[]
	{
		new ConfigAttribute("ConnectionTrace",			ReadWriteEnum.READWRITE),
		new ConfigAttribute("CoreFile",					ReadWriteEnum.READWRITE),
		new ConfigAttribute("DatabaseName",				ReadWriteEnum.READONLY),
		new ConfigAttribute("DataBufferPoolSize",		ReadWriteEnum.READWRITE),
		new ConfigAttribute("DataDeviceSize",			ReadWriteEnum.READWRITE),
		new ConfigAttribute("DevicePath",				ReadWriteEnum.READWRITE),
		new ConfigAttribute("EagerSessionThreshold",	ReadWriteEnum.READWRITE),
		new ConfigAttribute("EagerSessionTimeout",		ReadWriteEnum.READWRITE),
		new ConfigAttribute("EventBufferSize",			ReadWriteEnum.READWRITE),
		new ConfigAttribute("HistoryPath",				ReadWriteEnum.READWRITE),
		new ConfigAttribute("InternalLogBufferSize",	ReadWriteEnum.READWRITE),
		new ConfigAttribute("JdbcUrl",					ReadWriteEnum.READONLY),
		new ConfigAttribute("LogBufferSize",			ReadWriteEnum.READWRITE),
		new ConfigAttribute("MaxTables",				ReadWriteEnum.READWRITE),
		new ConfigAttribute("NumberOfDatadevices",		ReadWriteEnum.READWRITE),
		new ConfigAttribute("NumberOfLocks",			ReadWriteEnum.READWRITE),
		new ConfigAttribute("NumberOfSessions",			ReadWriteEnum.READWRITE),
		new ConfigAttribute("PackageName",				ReadWriteEnum.READWRITE),
		new ConfigAttribute("PortBase",					ReadWriteEnum.READONLY),
		new ConfigAttribute("RelalgDeviceSize",			ReadWriteEnum.READWRITE),
		new ConfigAttribute("SQLTraceMode",				ReadWriteEnum.READWRITE),
		new ConfigAttribute("SessionTimeout",			ReadWriteEnum.READWRITE),
		new ConfigAttribute("StartRepairDelay",			ReadWriteEnum.READWRITE),
		new ConfigAttribute("StatInterval",				ReadWriteEnum.READWRITE),
		new ConfigAttribute("SyslogFacility",			ReadWriteEnum.READWRITE),
		new ConfigAttribute("SyslogLevel",				ReadWriteEnum.READWRITE),
		new ConfigAttribute("SyslogPrefix",				ReadWriteEnum.READWRITE),
		new ConfigAttribute("TakeoverTime",				ReadWriteEnum.READWRITE),
	};
	
	////////////////////////////////////////////////////////////////////////////
	
	private Hashtable<String,String>	props;
	private Properties					readonlyAttributes;
	private Properties					readwriteAttributes;
	
	////////////////////////////////////////////////////////////////////////////

	private enum ReadWriteEnum
	{
		READWRITE,
		READONLY
	}
	
	////////////////////////////////////////////////////////////////////////////

	private static class ConfigAttribute
	{
		ConfigAttribute(String Name, ReadWriteEnum RW)
		{
			name = Name;
			rw = RW;
		}

		String			name;
		ReadWriteEnum	rw;
	}
	
	////////////////////////////////////////////////////////////////////////////

	public static void main(String[] notUsed)
	{
		
		Properties p = new Properties();
		p.setProperty("ConnectionTrace", "xxx");
		p.setProperty("CoreFile", "xxx");
		p.setProperty("DatabaseName", "xxx");
		p.setProperty("DataBufferPoolSize", "xxx");
		p.setProperty("DataDeviceSize", "xxx");
		p.setProperty("DevicePath", "xxx");
		p.setProperty("EagerSessionThreshold", "xxx");
		p.setProperty("EagerSessionTimeout", "xxx");
		p.setProperty("EventBufferSize", "xxx");
		p.setProperty("HistoryPath", "xxx");
		p.setProperty("InternalLogBufferSize", "xxx");
		p.setProperty("JdbcUrl", "xxx");
		p.setProperty("LogBufferSize", "xxx");
		p.setProperty("MaxTables", "xxx");
		p.setProperty("NumberOfDatadevices", "xxx");
		p.setProperty("NumberOfLocks", "xxx");
		p.setProperty("NumberOfSessions", "xxx");
		p.setProperty("PackageName", "xxx");
		p.setProperty("PortBase", "xxx");
		p.setProperty("RelalgDeviceSize", "xxx");
		p.setProperty("SQLTraceMode", "xxx");
		p.setProperty("SessionTimeout", "xxx");
		p.setProperty("StartRepairDelay", "xxx");
		p.setProperty("StatInterval", "xxx");
		p.setProperty("SyslogFacility", "xxx");
		p.setProperty("SyslogLevel", "xxx");
		p.setProperty("SyslogPrefix", "xxx");
		p.setProperty("TakeoverTime", "xxx");
		
		ConfigAttributeManager cam = new ConfigAttributeManager(p);
		
		System.out.println("Original Attributes:");
		System.out.println(p);
		System.out.println("\nREADONLY Attributes:");
		System.out.println(cam.getReadOnlyAttributes());
		System.out.println("\nREADWRITE Attributes:");
		System.out.println(cam.getReadWriteAttributes());
	}
}


/**
 


 *** == read-only
       Table 1 Readable Configuration Attributes

      Attribute                 Range            Default  Unit
      ConnectionTrace           true/false       false
      CoreFile                  true/false       false
      *** DatabaseName                               hadb
      DataBufferPoolSize        16-2047          200      MB
      DataDeviceSize            32-262144        1024     MB
      DevicePath                n/a              n/a
      EagerSessionThreshold     0-100            50  (% of NumberOfSessions)
      EagerSessionTimeout       0-2147483647     120 s
      EventBufferSize           0-2097152        0        MB
      HistoryPath               n/a
      InternalLogBufferSize     4-128            12       MB
      *** JdbcUrl
      LogBufferSize             4-2047           48       MB
      MaxTables                 100-1100         1100
      NumberOfDatadevices       1-8              1
      NumberOfLocks             20000-1073741824 50000
      NumberOfSessions          1-10000          100
      PackageName               n/a              V4.x.x.x
      *** PortBase                  10000-63000      15000
      RelalgDeviceSize          32-262144        128      MB
      SQLTraceMode              none/short/full  none
      SessionTimeout            0-2147483647     1800     s
      StartRepairDelay          0-100000         20       s
      StatInterval              0-600           600      s
      SyslogFacility            <facility>       local0
      SyslogLevel               <level>          warning
      SyslogPrefix              <string>         hadb-<dbname>
      TakeoverTime              500-16000        10000    ms
 
 ================================================================================
 
        Table 1 Writable Configuration Attributes

       Attribute                 Range            Default  Unit
       ConnectionTrace           true/false       false
       CoreFile                  true/false       false
       DataBufferPoolSize        16-2047          200      MB
       DataDeviceSize            32-262144        1024     MB
       DevicePath                n/a              n/a
       EagerSessionThreshold     0-100            50  (% of NumberOfSessions)
       EagerSessionTimeout       0-2147483647     120 s
       EventBufferSize           0-2097152        0        MB
       HistoryPath               n/a
       InternalLogBufferSize     4-128            12       MB
       LogBufferSize             4-2047           48       MB
       MaxTables                 100-1100         1100
       NumberOfDatadevices       1-8              1
       NumberOfLocks             20000-1073741824 50000
       NumberOfSessions          1-10000          100
       PackageName               n/a              V4.x.x.x
       RelalgDeviceSize          32-262144        128      MB
       SQLTraceMode              none/short/full  none
       SessionTimeout            0-2147483647     1800     s
       StartRepairDelay          0-100000         20       s
       StatInterval              0-600            600      s
       SyslogFacility            <facility>       local0
       SyslogLevel               <level>          warning
       SyslogPrefix              <string>         hadb-<dbname>
       TakeoverTime              500-16000        10000    ms

       Valid values for SyslogFacility (<facility>) are:
             local0/local1/local2/local3/local4/local5/
             local7/kern/mail/none
 
*/