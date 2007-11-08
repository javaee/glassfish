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
package com.sun.enterprise;

import java.rmi.RemoteException;
import com.sun.enterprise.util.ORBManager;
import com.sun.enterprise.util.Utility;
import com.sun.enterprise.repository.Configuration;
import com.sun.enterprise.repository.ConfigurationImpl;
import com.sun.enterprise.repository.J2EEResourceFactory;
/* IASRI #4626188
import com.sun.enterprise.repository.J2EEResourceFactoryImpl;
 */
// START OF IASRI #4626188
import com.sun.enterprise.repository.IASJ2EEResourceFactoryImpl;
// END OF IASRI #4626188
import javax.rmi.CORBA.Tie;
import java.util.Properties;
// IASRI 4660742 START
import java.util.logging.*;
import com.sun.logging.*;
// IASRI 4660742 END

/**
 * A Server Configuration is a singleton object that stores all the 
 * properties that are needed by various components.
 * @author Harish Prabandham
 */
public class ServerConfiguration {

// IASRI 4660742 START
    private static Logger _logger=null;
    static{
       _logger=LogDomains.getLogger(LogDomains.ROOT_LOGGER);
        }
// IASRI 4660742 END
    public static final String JNDI_NAME = "ServerConfiguration";

    private static final boolean debug = false;
    private static ServerConfiguration serverconfig = null;

    private Configuration config;
    private boolean remote = true;

    public static ServerConfiguration getConfiguration() {
        if(serverconfig == null) {
            serverconfig = new ServerConfiguration();
        }
        return serverconfig;
    }

    public static J2EEResourceFactory getJ2EEResourceFactory() {
        /*   IASRI #4626188
        return new J2EEResourceFactoryImpl();
         */
        // START OF IASRI #4626188
        return new IASJ2EEResourceFactoryImpl();
        // END OF IASRI #4626188
    }

    private ServerConfiguration() {
        try{
            config = (Configuration) Utility.lookupObject(
                    JNDI_NAME, Configuration.class);
        } catch (Exception ne) {
            // we could not connect to the naming .. lets use a local copy
// IASRI 4660742
            // ne.printStackTrace(System.err);
// START OF IASRI 4660742
            // _logger.log(Level.WARNING,"enterprise.error_connecting",ne);
// END OF IASRI 4660742
            try{
                config = new ConfigurationImpl();
		/* not needed for local copy
		javax.rmi.PortableRemoteObject.exportObject(config);
		if(ORBManager.getORB() != null) {
		    Tie servantsTie = javax.rmi.CORBA.Util.getTie(config);
		    servantsTie.orb(ORBManager.getORB());
		}
		*/
                remote = false;
            } catch(Exception e) {
		if ( debug )
// IASRI 4660742 		    e.printStackTrace(System.err);
// START OF IASRI 4660742
            _logger.log(Level.WARNING,"enterprise.config_create_error",e);
// END OF IASRI 4660742
            }
        }
    }

    /**
     * This method gets a property value associated with the given key.
     * @return A property value corresponding to the key
     */
    public String getProperty(String key) {
        String val = null;

        try {
            val = config.getProperty(key);
        } catch (Exception e) {
// IASRI 4660742            e.printStackTrace(System.err);
// START OF IASRI 4660742
            _logger.log(Level.SEVERE,"enterprise.getpropertry_exception",e);
// END OF IASRI 4660742
        } 
        return val;
        
    }

    /**
     * This method gets a property value associated with the given key.
     * @param The key 
     * @param The default Value 
     * @return A property value corresponding to the key
     */
    public String getProperty(String key, String defaultvalue) {
        String val = defaultvalue;

        try {
            val = config.getProperty(key);
        } catch (Exception e) {
// IASRI 4660742            e.printStackTrace(System.err);
// START OF IASRI 4660742
            _logger.log(Level.SEVERE,"enterprise.getpropertry_exception",e);
// END OF IASRI 4660742
        }
	    if(val != null) {
		return val;
	    } else {
		return defaultvalue;
	    }
        
    }


    /**
     * This method associates a property value with the given key.
     */
    public void setProperty(String key, String value) {
// IASRI 466074
	// System.out.println("Setting Property: " + key + " value =" + value +
	//	   " " + remote);
// START OF IASRI 4660742
        //  _logger.log(Level.FINE,"Setting Property: " + key + " value =" + value +
        //         " " + remote);
// END OF IASRI 4660742
        try {
            if (remote) {
                config.setProperty(key, value);
            } else {
                throw new IllegalStateException("Cannot set properties n local mode");
            }
        } catch (Exception e) {
// IASRI 4660742            e.printStackTrace(System.err);
// START OF IASRI 4660742
            _logger.log(Level.SEVERE,"enterprise.setpropertry_exception",e);
// END OF IASRI 4660742
        }
    }

    /**
     * This method gets an Object associated with the given key.
     * @return An Object corresponding to the key
     */
    public Object getObject(String key) {
        Object obj = null;

        try {
            obj = config.getObject(key);
        } catch (Exception e) {
// IASRI 4660742            e.printStackTrace(System.err);
// START OF IASRI 4660742
            _logger.log(Level.SEVERE,"enterprise.getobject_exception",e);
// END OF IASRI 4660742
        }
        return obj;
       
    }

    /**
     * This method associates an Object with the given key.
     */
    public void setObject(String key, Object obj) {
        try {
            if (remote) {
                config.setObject(key, obj);
	    }  else {
                throw new IllegalStateException("Cannot set objects in local mode");
            }
        } catch(Exception e) {
// IASRI 4660742            e.printStackTrace(System.err);
// START OF IASRI 4660742
            _logger.log(Level.SEVERE,"enterprise.setobject_exception",e);
// END OF IASRI 4660742
        }
    }

    /**
     * This method returns all the keys for a given index.
     * 
     */
    public String[] getKeys(String index) {
        String[] keys = null;

        try {
            keys = config.getKeys(index);
        } catch(Exception e) {
// IASRI 4660742            e.printStackTrace(System.err);
// START OF IASRI 4660742
            _logger.log(Level.SEVERE,"enterprise.getkey_exception",e);
// END OF IASRI 4660742
        }
        return keys;
        
    }

    /**
     * Return the subset of properties for a given index.
     *
     */
    public Properties getProperties(String index) {
        Properties props = new Properties();
        try {
            String[] keys = config.getKeys(index);
            for (int i=0; i<keys.length; i++) {
                props.put(keys[i], getProperty(keys[i]));
            }
        } catch (Exception e) {
// IASRI 4660742            e.printStackTrace(System.err);
// START OF IASRI 4660742
            _logger.log(Level.SEVERE,"enterprise.get_put_key_exception",e);
// END OF IASRI 4660742
        } 
        return props;
        
    }

    public void removeProperty(String key) {
        try {
            if (remote) {
                config.removeProperty(key);
            }  else {
                throw new IllegalStateException("Cannot remove properties in local mode");
            }
        } catch(Exception e) {
// IASRI 4660742            e.printStackTrace(System.err);
// START OF IASRI 4660742
            _logger.log(Level.SEVERE,"enterprise.remove_property_exception",e);
// END OF IASRI 4660742
        }
    }

    public void removeObject(String key) {
        try {
            if (remote) {
                config.removeObject(key);
	    } else {
                throw new IllegalStateException("Cannot remove objects in local mode");
            }
        } catch(Exception e) {
// IASRI 4660742            e.printStackTrace(System.err);
// START OF IASRI 4660742
            _logger.log(Level.SEVERE,"enterprise.remove_object_exception",e);
// END OF IASRI 4660742
        }
    }
}



