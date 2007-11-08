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
package com.sun.enterprise.repository;

import java.util.*;
import java.io.*;
import com.sun.enterprise.util.FileUtil;
import com.sun.enterprise.util.Utility;
// START OF IASRI 4660742
import java.util.logging.*;
import com.sun.logging.*;
// END OF IASRI 4660742

/**
 * A Configuration object stores all the properties that are needed
 * by various components within the EJB server.    
 * @author Harish Prabandham
 */
public class ConfigurationImpl implements Configuration {
// IASRI 4660742 START
    private static Logger _logger=null;
    static{
       _logger=LogDomains.getLogger(LogDomains.ROOT_LOGGER);
        }
// IASRI 4660742 END   
    private static String OBJECT_STORE_DIR = "repository" + File.separator +
	Utility.getLocalHost() +File.separator + "objects"
	    +File.separator;

    /** IASRI 4672501 -- remove ri directory
    private static String SERVER_CONFIG_DIR = "repository" + File.separator +
	Utility.getLocalHost() + File.separator;
    **/
   
    private static final String OBJECT_FILE_EXT = ".ser";
    private Hashtable table;
    private Repository defaultRepository;
    
	/**
	 * Creates a new instance of a Configuration object.
	 * @param readwrite => true or false 
	 */
	public ConfigurationImpl() {
        /** IASRI 4672501 -- remove ri directory
	    File dir = new File(FileUtil.getAbsolutePath(OBJECT_STORE_DIR));
	    if(!dir.exists()) {
		dir.mkdirs();
	    }
        **/

	    table = new Hashtable();
	    defaultRepository = getRepository("default");
        /** IASRI 4672501 -- remove ri directory
	    getServerRepository();
        **/
	}

	private String getIndex(String key) {
	    // Everything before the first .
	    int index = key.indexOf(".");
	    
	    if(index < 0)
		return "default";
	    
	    return key.substring(0, index);
	}
    
	private String getEffectiveKey(String key) {
	    // Everything after the first .
	    int index = key.indexOf(".");
	    
	    if(index < 0)
		return key;

	    return key.substring(index + 1);
	}
    
	private Repository getRepository(String repName) {
	    Repository rep = (Repository) table.get(repName);
	    if(rep == null) {
		rep = new Repository(repName);
		if(rep.getName().equals(repName)) {
		    table.put(repName, rep);
		} else
		    rep = defaultRepository;
	    }

	    return rep;
	}

    /** IASRI 4672501 -- remove ri directory
	private Repository getServerRepository() {
// IASRI 4660742
	    // System.out.println("getServerRepository.....");
// START OF IASRI 4660742
		 _logger.log(Level.FINE,"getServerRepository.....");
// END OF IASRI 4660742
	    Repository rep = (Repository) table.get("server");
	    if(rep == null) {
		File dir = new File(FileUtil.getAbsolutePath(SERVER_CONFIG_DIR));
		if(!dir.exists()) {
		    dir.mkdirs();
		}

		File f = new File(dir, "server.properties");
		if(!f.exists()) {
		    try { f.createNewFile(); } catch(IOException e){
//IASRI 4660742			e.printStackTrace();
// START OF IASRI 4660742
			_logger.log(Level.SEVERE,"enterprise.ioexception",e);
// END OF IASRI 4660742
		    }
		}
		
		rep = new Repository("server",
				     FileUtil.getAbsolutePath(SERVER_CONFIG_DIR) + File.separator);
		table.put("server", rep);
	    }
	
	    return rep;
	}
    **/
	
	/**
	 * This method gets a property value associated with the given key.
	 * @return A property value corresponding to the key
	 */
	public String getProperty(String key) 
		throws java.rmi.RemoteException
	{
		String index = getIndex(key);
		String newKey = getEffectiveKey(key);

		Repository rep = getRepository(index);

		String val = null;
		
		if(rep.getName().equals(index)){
			val = rep.find(newKey);
		} else {
			val = rep.find(key);
		}


		return val;	
	}

	/**
	 * This method associates a property value with the given key.
	 */
	public void setProperty(String key, String value)
		throws java.rmi.RemoteException
	{
		String index = getIndex(key);
		String newKey = getEffectiveKey(key);
		Repository rep = getRepository(index);

		if(rep.getName().equals(index)){
			rep.add(newKey, value);
		} else {
			rep.add(key, value);
		}
	}

	/**
	 * This method removes a property value given the key.
	 */
	public void removeProperty(String key)
		throws java.rmi.RemoteException
	{
		String index = getIndex(key);
		String newKey = getEffectiveKey(key);
		Repository rep = getRepository(index);

		if(rep.getName().equals(index)){
			rep.remove(newKey);
		} else {
			rep.remove(key);
		}
	}


	public Object getObject(String key)
		throws java.rmi.RemoteException
	{
		String fname = getProperty(key);
		Object obj = null;

		// Use this fname to deserialize...
		if(fname != null) {
		    try{
		        FileInputStream fstream = new FileInputStream(fname);
		        ObjectInputStream objstream = 
			    new ObjectInputStream(fstream);
		        obj = objstream.readObject();
		        fstream.close();
		    }catch(Exception e){
//IASRI 4660742			e.printStackTrace(System.out);
// START OF IASRI 4660742
			_logger.log(Level.SEVERE,"enterprise.file_exception",e);
// END OF IASRI 4660742
		    }
		}

		return obj;
	}

	/**
	 * This method associates an Object with the given key.
	 */
	public void setObject(String key, Object obj)
		throws java.rmi.RemoteException
	{
		String className = obj.getClass().getName();
		String instanceId = String.valueOf(obj.hashCode());
		String fname =  OBJECT_STORE_DIR + className + instanceId + 
						OBJECT_FILE_EXT;

		// serialize obj and store it in the file .....

		try{
		String absFileName = FileUtil.getAbsolutePath(fname);
		FileOutputStream fstream = new FileOutputStream(absFileName);
		ObjectOutputStream objstream = new ObjectOutputStream(fstream);
		objstream.writeObject(obj);
		objstream.flush();
		fstream.close();
		setProperty(key, absFileName);
		}catch(Exception e){
//IASRI 4660742			e.printStackTrace(System.out);
// START OF IASRI 4660742
			_logger.log(Level.SEVERE,"enterprise.file_exception",e);
// END OF IASRI 4660742
		}
	}

	public void removeObject(String key)
		throws java.rmi.RemoteException
	{
		String fname = getProperty(key);

		// Use this fname & delete the file first..
		if(fname != null) {
		    try{
		    File file = new File(fname);
			if(file.exists())
				file.delete();
			removeProperty(key);
		    }catch(Exception e){
//IASRI 4660742			e.printStackTrace(System.out);
// START OF IASRI 4660742
			_logger.log(Level.SEVERE,"enterprise.file_exception",e);
// END OF IASRI 4660742
		    }
		}
	}

	/**
	 * This method returns all the keys for the given index.
	 * 
	 */
	public String[] getKeys(String index) throws java.rmi.RemoteException
	{
		Repository rep = getRepository(index);

		return rep.keys();
	}
}
