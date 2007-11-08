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

package com.sun.enterprise.admin.util;
import java.io.IOException;
import java.io.File;
import java.io.InputStream;
import java.io.FileInputStream;
import java.util.Properties;

/**
 * This class implements IStringSource for the case where the Strings are kept
 * in a properties file.  The file is read when instantiated and cached for
 * later usage.  There are numerous ways to specify a Properties file:
 * <b><br><br>Filename
 * <br>File Object
 * <br>Properties Object
 *
 * @see IStringSource
 */
public class PropertiesStringSource implements IStringSource 
{
	/** Create an instance from an existing Properties object
	 * @param p A Properties object to use as the source of Strings
	 */	
    public PropertiesStringSource(Properties p)
    {
        initialize(p);
    }

    public PropertiesStringSource(InputStream is) throws IOException
    {
        initialize(is);
    }
	
	/** Create an instance from a Properties file.
	 * @param f The File object.
	 * @throws IOException The file probably doesn't exist.
	 */	
    public PropertiesStringSource(File f) throws IOException
    {
        initialize(f);
    }
    
    
	/** Create an instance from the named Properties file
	 * @param filename The name of the Properties file
	 * @throws IOException The file probably doesn't exist
	 */	
    public PropertiesStringSource(String filename) throws IOException
    {
        initialize(filename);
    }
       
    
    /** Get a string referenced by the designated key.
	 *
	 * @param key the key to lookup the string
	 * @return the string, or null if string cannot be found 
	 */
    public String getString(String key) 
    {
		Assert.assertit((mProperties!=null), "invalid state:  mProperties is null");
		
		//ArgChecker.check(key, "key");
		
		return mProperties.getProperty(key);
	}
   
	
	/* In java, a derived class' constructor must call the base class constructor
	 * in the very first line.  Sometimes this can become an insurmountable
	 * problem because the derived class needs to assemble arguments first.
	 * So the real constructor functionality is broken into initialize() methods
	 * But outsiders shouldn't be able to create empty instances or to
	 * call initialize().  So all of these methods are protected.
	 * WBN
	 */

	/** Do-nothing constructor.
	 * This is protected so that derived classes can start with an
	 * empty instance -- and then call initialize() methods in a do-it-yourself
	 * fashion
	 */
	protected PropertiesStringSource()
	{
		/** This is protected so that derived classes can start with an
		 * empty instance -- and then call initialize() methods in a do-it-yourself
		 * fashion
		 */
	}
	
	
	/** Use the Properties arguments as the source of Strings
	 * @param p A Properties object to use as the source of Strings
	 */	
    protected void initialize(Properties properties) 
    {
        //ArgChecker.check(properties, "properties");
        mProperties = properties;
    }

	
	/** Load the Properties object from a file.
	 * @param f The File object
	 * @throws IOException The file probably doesn't exist
	 */	
	protected void initialize(File file) throws IOException
    {
        //ArgChecker.check(file != null && file.exists(), 
		//	"File object doesn't point to an existing file: " + file.getPath());

		//ArgChecker.check(file.canRead(), 
		//	"File object can't be read: " + file.getPath());
		
		//ArgChecker.check(!file.isDirectory(), 
		//	"File object is a directory: " + file.getPath());
        FileInputStream fis = new FileInputStream(file);
        initialize(fis);
    }
    
    
	/** Load the Properties object from a file.
	 * @param filename The name of the Properties file
	 */	
    protected void initialize(String filename) throws IOException
    {
		//ArgChecker.check(filename, "filename");
        initialize(new File(filename));
    }

	/** Load the Properties object from a file.
	 * @param filename The name of the Properties file
	 */	
    protected void initialize(InputStream is) throws IOException
    {
		//ArgChecker.check(filename, "filename");
		mProperties = new Properties();
		mProperties.load(is);
    }
	
    private Properties mProperties;
}

