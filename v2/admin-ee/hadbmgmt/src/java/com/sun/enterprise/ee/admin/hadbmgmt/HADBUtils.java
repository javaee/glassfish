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
 * HADBUtils.java
 *
 * Created on June 1, 2004, 10:02 PM
 */

package com.sun.enterprise.ee.admin.hadbmgmt;

import java.io.File;
import java.net.*;
import com.sun.enterprise.util.SystemPropertyConstants;
import com.sun.enterprise.util.io.FileUtils;
import java.util.*;

/**
 *
 * @author  bnevins
 */
public class HADBUtils
{
    private HADBUtils()
    {
    }
    
    ///////////////////////////////////////////////////////////////////////////
    
    static int waitForProcess(Process p, int msec)
    {
        int ret = 1;
        
        try
        {
            ProcessWaiter pt = new ProcessWaiter(p);
            Thread t = new Thread(pt, "ProcessWaiter");
            t.start();
            t.join(msec);
            ret = pt.ret;

            if(t.isAlive())
                p.destroy();
        }
        catch(Exception e)
        {
            // nothing to do
        }
        return ret;
    }
    
    ///////////////////////////////////////////////////////////////////////////
    
    static boolean useIP()
    {
        return USE_IP_ONLY;
    }
    
    ///////////////////////////////////////////////////////////////////////////
    
    static boolean nativeSchema()
    {
        return NATIVE_SCHEMA;
    }
    
    ///////////////////////////////////////////////////////////////////////////
    
    static boolean noDelete()
    {
        return NO_DELETE;
    }
    
    ///////////////////////////////////////////////////////////////////////////
    
    static boolean noHADB()
    {
        return NO_HADB;
    }
    
    ///////////////////////////////////////////////////////////////////////////
    
    static boolean yesHADB()
    {
        return !NO_HADB;
    }
    
    ///////////////////////////////////////////////////////////////////////////
    
    static int getPhonyReturnValue()
    {
        return phonyReturnValue;
    }
    
    ///////////////////////////////////////////////////////////////////////////
    
    static void setPhonyReturnValue(int i)
    {
        phonyReturnValue = i;
    }
    
    ///////////////////////////////////////////////////////////////////////////
    
    static boolean pingWithJMX()
    {
        return PING_WITH_JMX;
    }
    
    ///////////////////////////////////////////////////////////////////////////
    
    static String getIP(String host) throws HADBSetupException
    {
        try
        {
            InetAddress add = InetAddress.getByName(host);
            String ip = add.getHostAddress();
            
            // we don't ever want to return the loopback address!
            if(ip.equals(LOOPBACK))
            {
                add = InetAddress.getLocalHost();
                ip = add.getHostAddress();
            }
            
            // if we still have the loopback -- that's it, thrown in the towel...
            if(ip.equals(LOOPBACK))
            {
                throw new HADBSetupException("hadbmgmt-res.BadHostNameGotLoopback");
            }
            
            return add.getHostAddress();
        }
        catch(Exception e)
        {
            throw new HADBSetupException("hadbmgmt-res.BadHostName", host);
        }
    }
    
    ///////////////////////////////////////////////////////////////////////////
    
    static File getPasswordFileDir(HADBInfo info) throws HADBSetupException
    {
        File f = null;
        //String s = System.getProperty(SystemPropertyConstants.DOMAINS_ROOT_PROPERTY);
        String s = System.getProperty(SystemPropertyConstants.INSTANCE_ROOT_PROPERTY) + "/config";
        try
        {
            f = new File(s);
            
            if(FileUtils.safeIsDirectory(f) && f.canWrite())
                return f;
        }
        catch(Exception e)
        {
            // fall through, we handle the error below...
        }
        
        throw new HADBSetupException("hadbmgmt-res.NoPasswordWritePermission", new Object[] {SystemPropertyConstants.DOMAINS_ROOT_PROPERTY, s} );
    }
    
    ///////////////////////////////////////////////////////////////////////////
    
    // Developmnent Hack.
    // hadbm will not be called for DB and Schema creation if this is set to true...
    private static final	boolean NO_HADB;
    
    // don't delete the password files
    private static final	boolean NO_DELETE;
    
    // call the schema creation from java, instead of as a sub-process
    private static final	boolean NATIVE_SCHEMA;
    
    // use IP addresses instead of hostnames
    private static final	boolean USE_IP_ONLY;
    
    // used in conjunction with NO_HADB to simulate hadb
    private static			int		phonyReturnValue	= 0;
    
    // use either hadbm or jmxmp to ping HADB-Management Agent
    private static final	boolean PING_WITH_JMX;
    
    private static final	String	LOOPBACK			= "127.0.0.1";
    static
    {
        //Note -- this looks ugly because, since they are all final, I can'se the default'
        // and then reset here to non-default.  I.e. every case requires both an
        // if and an else.
        
        String prop = System.getProperty("hadbnohadb");
        if(prop != null && prop.equals("true"))
            NO_HADB = true;
        else
            NO_HADB = false;
        
        prop = System.getProperty("hadbmnodeletepasswordfile", "false");
        
        if(prop != null && prop.equals("true"))
            NO_DELETE = true;
        else
            NO_DELETE = false;
        
        prop = System.getProperty("hadbnativeschema", "false");
        
        if(prop != null && prop.equals("false"))
            NATIVE_SCHEMA = false;
        else
            NATIVE_SCHEMA = true;
        
        prop = System.getProperty("hadbusehostname", "false");
        
        if(prop != null && prop.equals("true"))
            USE_IP_ONLY = false;
        else
            USE_IP_ONLY = true;
        
        prop = System.getProperty("hadbpingwithjmx", "false");
        
        if(prop != null && prop.equals("true"))
            PING_WITH_JMX = true;
        else
            PING_WITH_JMX = false;
    }
    
    /**
     * Convert a Properties object into a Hashtable<String,String>.
     * This makes processing MUCH less painful.
     * Note: Properties is declared as Hashtable<Object,Object>
     * Also -- all keys are made lowercase so that we can make the
     * input props case-insensitive
     */
    
    static Hashtable<String,String> props2hash(Properties props)
    {
        Hashtable<String,String> ht = new Hashtable<String,String>();
        
        // return null if props is null
        if(props == null)
            return null;
        
        // return an empty List if no props...
        if(props.size() <= 0)
            return ht;
        
        Set<Map.Entry<Object,Object>> set = props.entrySet();
        
        for(Map.Entry<Object,Object> entry : set)
        {
            String key = ((String)entry.getKey()).toLowerCase();
            String value = (String)entry.getValue();
            ht.put(key, value);
        }
        
        return ht;
    }
    
    ///////////////////////////////////////////////////////////////////////////
    
    final static String[] validCreateAttributes = new String[]
    {
        // noclear, nocleanup are also allowed as props.
        //Attribute							Range            Default  Unit
        "ConnectionTrace",		//          true/false       false
        "CoreFile",				//          true/false       false
        "DataBufferPoolSize",	//			16-2047          200      MB
        "DataDeviceSize",		//           32-262144        1024     MB
        "DevicePath",			//           n/a              n/a
        "EagerSessionThreshold",	//    0-100            50  (% of NumberOfSessions)
        "EagerSessionTimeout",	//      0-2147483647     120 s
        "EventBufferSize",		//          0-2097152        0        MB
        "HistoryPath",			//              n/a
        "InternalLogBufferSize",	//    4-128            12       MB
        "LogBufferSize",			//            4-2047           48       MB
        "MaxTables",				//                100-1100         1100
        /* R- */"NumberOfDatadevices",	//      1-8              1
        "NumberOfLocks",			//            20000-1073741824 50000
        "NumberOfSessions",		//         1-10000          100
        "PortBase",				//                 10000-63000      15000
        "RelalgDeviceSize",		//         32-262144        128      MB
        "SQLTraceMode",			//             none/short/full  none
        "SessionTimeout",		//           0-2147483647     1800     s
        "StartRepairDelay",		//         0-100000         20       s
        "StatInterval",			//             0-600            600      s
        "SyslogFacility",		//           <facility>       local0
        "SyslogLevel",			//              <level>          warning
        "SyslogPrefix",			//             <string>         hadb-<dbname>
        "TakeoverTime",			//             500-16000        10000    ms
    };
    private static class ProcessWaiter implements Runnable
    {
        ProcessWaiter(Process p)
        {
            this.p = p;
        }

        public void run()
        {
            try
            {
                ret = p.waitFor();
            } 
            catch (InterruptedException ex)
            {
            }
        }
        
        Process p;
        int ret = 1;
    }

}

