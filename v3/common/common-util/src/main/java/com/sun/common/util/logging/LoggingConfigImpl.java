package com.sun.common.util.logging;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.Properties;
import java.util.Enumeration;
import java.lang.IllegalArgumentException;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import java.util.Hashtable;
import java.util.Set;
import java.util.Iterator;
import java.util.Map;
import java.util.HashMap;
import java.util.logging.Logger;
import java.util.logging.LogManager;
import java.util.logging.Level;

import org.glassfish.server.ServerEnvironmentImpl;
import org.jvnet.hk2.annotations.Inject;
import org.jvnet.hk2.component.Injectable;

import org.jvnet.hk2.annotations.Service;
import org.jvnet.hk2.component.*;

import java.util.logging.LogManager;

@Service
public class LoggingConfigImpl implements LoggingConfig, PostConstruct{
		 
		@Inject
    	Logger logger; 
    	 
	 	@Inject
    	ServerEnvironmentImpl env;
    	
		Properties props = null;
		FileInputStream fis;
		String loggingPropertiesName;
		LogManager logMgr = null;
        File loggingConfigDir = null;

   /**
     * Constructor
     *
     */
	
	 public void postConstruct() { 
		// set logging.properties filename				
        setupConfigDir(env.getConfigDirPath());
          
	}

    // this is so the launcher can pass in where the dir is since 
    public void setupConfigDir(File file){
        loggingConfigDir=file;
        loggingPropertiesName = ServerEnvironmentImpl.kLoggingPropertiesFileNAme;
        logMgr = LogManager.getLogManager();
    }
	
	private void openPropFile() throws IOException{
		try {
			fis = new java.io.FileInputStream (new File(loggingConfigDir, loggingPropertiesName));
			props = new java.util.Properties();
        	props.load(fis);
            fis.close();            
		} catch (FileNotFoundException e ) {
			Logger.getAnonymousLogger().log(Level.SEVERE, "Cannot read logging.properties file : ", e);
			throw new IOException();
		} catch (IOException e) {
			Logger.getAnonymousLogger().log(Level.SEVERE, "Cannot read logging.properties file : ", e);
			throw new IOException();
		}
	}
	
	private void closePropFile() throws IOException{
		try {
			FileOutputStream ois = new FileOutputStream ( new File(loggingConfigDir, loggingPropertiesName));
			props.store(ois,"GlassFish logging.properties list");
			ois.close();
		} catch (FileNotFoundException e ) {
			Logger.getAnonymousLogger().log(Level.SEVERE, "Cannot close logging.properties file : ", e);
			throw new IOException();
		} catch (IOException e) {
			//System.out.println("some other exception");
			Logger.getAnonymousLogger().log(Level.SEVERE, "Cannot close logging.properties file : ", e);
			throw new IOException();			
		}
	}

    private void setWebLoggers(String value) {
        // set the rest of the web loggers to the same level
        // these are only accessible via the web-container name so all values should be the same
        String property= null;
        property = (String) props.setProperty("org.apache.catalina.level", value);
        property = (String) props.setProperty("org.apache.coyote.level", value);
        property = (String) props.setProperty("org.apache.jasper.level", value);
    }
			
    /**
     *  setLoggingProperty() sets an existing propertyName to be propertyValue	
     *  if the property doesn't exist the property will be added.  The logManager 
	 *  readConfiguration is not called in this method.
     
     * @param propertyName Name of the property to set
     * @param propertyValue  Value to set
     *
	 * @throws  IOException     
     */		
	public String setLoggingProperty(String propertyName, String propertyValue) throws IOException
		{
		try {		
			openPropFile();		
			// update the property			
            if (propertyValue == null ) return null;
            // may need to map the domain.xml name to the new name in logging.properties file
            String key = LoggingXMLNames.xmltoPropsMap.get(propertyName);
			if(key == null) {
		        key = propertyName;
            }
			String property = (String) props.setProperty(key, propertyValue);
            if (propertyName.equals(LoggingXMLNames.webcontainer)) {
                setWebLoggers(propertyValue);
            }
	
			closePropFile();
			return  property;
		} catch (IOException e) {
			throw e;
		}
	}

	/* update the properties to new values.  properties is a Map of names of properties and 
	 * their cooresponding value.  If the property does not exist then it is added to the
	 * logging.properties file.  
	 * 	 	
	 * @param properties Map of the name and value of property to add or update
	 *
	 * @throws  IOException
	 */ 	
	public Map<String, String> updateLoggingProperties(Map<String, String> properties) throws IOException
	{
        Map<String, String> m = new HashMap<String, String>();
		try {
			openPropFile();
		
	    	// need to map the name given to the new name in logging.properties file
			String key = null;
	    	for (Map.Entry<String, String> e : properties.entrySet()) {
                if (e.getValue() == null) continue;
                key = LoggingXMLNames.xmltoPropsMap.get(e.getKey());
				if(key == null) {
					key = e.getKey();
                }
    			String property = (String) props.setProperty(key, e.getValue());
                if (e.getKey().equals(LoggingXMLNames.webcontainer)) {
                    setWebLoggers(e.getValue());
                }

    			//build Map of entries to return
                m.put(key, property);
                
	    	}
			closePropFile();

		} catch (IOException ex) {
			throw ex;
		} catch (Exception e) {
           // e.printStackTrace();
        }
        return m;        
	}	
	/* Return a Map of all the properties and corresponding values in the logging.properties file.
	 * @throws  IOException
	 */ 	
    

	public Map<String, String> getLoggingProperties() throws IOException {
		Map<String, String> m = new HashMap<String, String>(); 
		try {
			openPropFile();
			Enumeration e = props.propertyNames();
		
			while (e.hasMoreElements()) {
				String key = (String)e.nextElement();
				// convert the name in domain.xml to the name in logging.properties if needed
				if(LoggingXMLNames.xmltoPropsMap.get(key) != null) {
					key = LoggingXMLNames.xmltoPropsMap.get(key);
				}
        		//System.out.println("Debug "+key+ " " + props.getProperty(key));
        		m.put(key, props.getProperty(key));
			}

			closePropFile();
			return m;
		} catch (IOException ex) {
			throw ex;
		}
	}
	/*
	 * remove the listed properties from the logging.properties file.
	 * The readConfiguration method is called on the logManager after updating the properties.  
	 *	
	 * @param properties Set of the names of properties to remove
	 *
	 * @throws  IOException
	 */ 	
	
	public void removeLoggingProperties(Set<String> properties) throws IOException
	{
		try {
	 		openPropFile();
	 	
	 		Iterator i = properties.iterator();
	 		while (i.hasNext()) {
	 			try {
	 				Object p = i.next();
	 				logger.log(Level.FINER, "Remove from logging.properties file property ", p);

	 				props.remove((String)p);
	 			} catch (java.util.NoSuchElementException e) {
	 				//System.out.println("Attempt to remove nonexistent property "+e);
	 				Logger.getAnonymousLogger().log(Level.WARNING, "Attempt to remove nonexistent property ", e);
					// continue;
	 			}
	 		}
	 		closePropFile();

			try {
				logMgr.readConfiguration();
			} catch (java.io.IOException e) {
				Logger.getAnonymousLogger().log(Level.SEVERE, "Cannot reconfigure LogManager : ", e);
				throw new IOException();
			}
		} catch (IOException ex) {
			throw ex;
		}		 
	 }

}