package com.sun.common.util.logging;

import org.jvnet.hk2.annotations.Contract;
import java.util.Map;
import java.util.Set;
import java.io.IOException;

@Contract
public interface LoggingConfig {
	/* set propertyName to be propertyValue.  The logManager 
	*  readConfiguration is not called in this method.
	*/ 
	String setLoggingProperty(String propertyName, String propertyValue) throws IOException;
	
	/* update the properties to new values.  properties is a Map of names of properties and 
	 * their cooresponding value.  If the property does not exist then it is added to the
	 * logging.properties file.  
	 * 
	 * The readConfiguration method is called on the logManager after updating the properties.  
	*/ 	
	Map<String, String> updateLoggingProperties(Map<String, String> properties) throws IOException;
	
	/* get the properties and corresponding values in the logging.properties file.
	*/	
	Map<String, String> getLoggingProperties() throws IOException;
	
	/* remove a set of properties from the logging.properties file.
	*/
	void removeLoggingProperties(Set<String> properties)throws IOException;

}