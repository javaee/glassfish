/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2003-2017 Oracle and/or its affiliates. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License.  You can
 * obtain a copy of the License at
 * https://oss.oracle.com/licenses/CDDL+GPL-1.1
 * or LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at LICENSE.txt.
 *
 * GPL Classpath Exception:
 * Oracle designates this particular file as subject to the "Classpath"
 * exception as provided by Oracle in the GPL Version 2 section of the License
 * file that accompanied this code.
 *
 * Modifications:
 * If applicable, add the following below the License Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyright [year] [name of copyright owner]"
 *
 * Contributor(s):
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

package com.sun.jdbcra.common;

import java.lang.reflect.Method;
import java.util.Hashtable;
import java.util.Vector;
import java.util.StringTokenizer;
import java.util.Enumeration;
import com.sun.jdbcra.util.MethodExecutor;
import javax.resource.ResourceException;

import java.util.logging.Logger;
import java.util.logging.Level;
/**
 * Utility class, which would create necessary Datasource object according to the 
 * specification.
 *
 * @version	1.0, 02/07/23
 * @author	Binod P.G
 * @see		com.sun.jdbcra.common.DataSourceSpec
 * @see		com.sun.jdbcra.util.MethodExcecutor
 */
public class DataSourceObjectBuilder implements java.io.Serializable{

    private DataSourceSpec spec;
    
    private Hashtable driverProperties = null;
    
    private MethodExecutor executor = null;    
    
    private static Logger _logger;
    static {
        _logger = Logger.getAnonymousLogger();
    }
    private boolean debug = false;
    /**
     * Construct a DataSource Object from the spec.
     * 
     * @param	spec	<code> DataSourceSpec </code> object.
     */
    public DataSourceObjectBuilder(DataSourceSpec spec) {
    	this.spec = spec;
    	executor = new MethodExecutor();
    }
    
    /**
     * Construct the DataSource Object from the spec.
     *
     * @return	Object constructed using the DataSourceSpec.
     * @throws	<code>ResourceException</code> if the class is not found or some issue in executing
     *		some method.
     */
    public Object constructDataSourceObject() throws ResourceException{
    	driverProperties = parseDriverProperties(spec);
        Object dataSourceObject = getDataSourceObject();
        System.out.println("V3-TEST : "  + dataSourceObject);
        Method[] methods = dataSourceObject.getClass().getMethods();
        for (int i=0; i < methods.length; i++) {
            String methodName = methods[i].getName();
	    if (methodName.equalsIgnoreCase("setUser")){	    	
	    	executor.runJavaBeanMethod(spec.getDetail(DataSourceSpec.USERNAME),methods[i],dataSourceObject);
	    	
	    } else if (methodName.equalsIgnoreCase("setPassword")){
	    	executor.runJavaBeanMethod(spec.getDetail(DataSourceSpec.PASSWORD),methods[i],dataSourceObject);
	    	
	    } else if (methodName.equalsIgnoreCase("setLoginTimeOut")){
	    	executor.runJavaBeanMethod(spec.getDetail(DataSourceSpec.LOGINTIMEOUT),methods[i],dataSourceObject);
	    	
	    } else if (methodName.equalsIgnoreCase("setLogWriter")){
	    	executor.runJavaBeanMethod(spec.getDetail(DataSourceSpec.LOGWRITER),methods[i],dataSourceObject);
	    	
	    } else if (methodName.equalsIgnoreCase("setDatabaseName")){
	    	executor.runJavaBeanMethod(spec.getDetail(DataSourceSpec.DATABASENAME),methods[i],dataSourceObject);
	    	
	    } else if (methodName.equalsIgnoreCase("setDataSourceName")){
	    	executor.runJavaBeanMethod(spec.getDetail(DataSourceSpec.DATASOURCENAME),methods[i],dataSourceObject);	    	
	    	
	    } else if (methodName.equalsIgnoreCase("setDescription")){
	    	executor.runJavaBeanMethod(spec.getDetail(DataSourceSpec.DESCRIPTION),methods[i],dataSourceObject);	
	    	
	    } else if (methodName.equalsIgnoreCase("setNetworkProtocol")){
	    	executor.runJavaBeanMethod(spec.getDetail(DataSourceSpec.NETWORKPROTOCOL),methods[i],dataSourceObject);
	    	
	    } else if (methodName.equalsIgnoreCase("setPortNumber")){
	    	executor.runJavaBeanMethod(spec.getDetail(DataSourceSpec.PORTNUMBER),methods[i],dataSourceObject);
	    	
	    } else if (methodName.equalsIgnoreCase("setRoleName")){
	    	executor.runJavaBeanMethod(spec.getDetail(DataSourceSpec.ROLENAME),methods[i],dataSourceObject);	    		    	
	    	
	    } else if (methodName.equalsIgnoreCase("setServerName")){
	    	executor.runJavaBeanMethod(spec.getDetail(DataSourceSpec.SERVERNAME),methods[i],dataSourceObject);	    
	    	
	    } else if (methodName.equalsIgnoreCase("setMaxStatements")){
	    	executor.runJavaBeanMethod(spec.getDetail(DataSourceSpec.MAXSTATEMENTS),methods[i],dataSourceObject);
	    	
	    } else if (methodName.equalsIgnoreCase("setInitialPoolSize")){
	    	executor.runJavaBeanMethod(spec.getDetail(DataSourceSpec.INITIALPOOLSIZE),methods[i],dataSourceObject);	    	
	    	
	    } else if (methodName.equalsIgnoreCase("setMinPoolSize")){
	    	executor.runJavaBeanMethod(spec.getDetail(DataSourceSpec.MINPOOLSIZE),methods[i],dataSourceObject);
	    	
	    } else if (methodName.equalsIgnoreCase("setMaxPoolSize")){
	    	executor.runJavaBeanMethod(spec.getDetail(DataSourceSpec.MAXPOOLSIZE),methods[i],dataSourceObject);	    	
	    	
	    } else if (methodName.equalsIgnoreCase("setMaxIdleTime")){
	    	executor.runJavaBeanMethod(spec.getDetail(DataSourceSpec.MAXIDLETIME),methods[i],dataSourceObject);	    	
	    	
	    } else if (methodName.equalsIgnoreCase("setPropertyCycle")){
	    	executor.runJavaBeanMethod(spec.getDetail(DataSourceSpec.PROPERTYCYCLE),methods[i],dataSourceObject);	    	
	    	
	    } else if (driverProperties.containsKey(methodName.toUpperCase())){
	    	Vector values = (Vector) driverProperties.get(methodName.toUpperCase());
	        executor.runMethod(methods[i],dataSourceObject, values);
	    }
        }
        return dataSourceObject;
    }
    
    /**
     * Get the extra driver properties from the DataSourceSpec object and
     * parse them to a set of methodName and parameters. Prepare a hashtable
     * containing these details and return.
     *
     * @param	spec	<code> DataSourceSpec </code> object.
     * @return	Hashtable containing method names and parameters,
     * @throws	ResourceException	If delimiter is not provided and property string
     *					is not null.
     */
    private Hashtable parseDriverProperties(DataSourceSpec spec) throws ResourceException{
    	String delim = spec.getDetail(DataSourceSpec.DELIMITER);

        String prop = spec.getDetail(DataSourceSpec.DRIVERPROPERTIES);
	if ( prop == null || prop.trim().equals("")) {
	    return new Hashtable();
	} else if (delim == null || delim.equals("")) {
	    throw new ResourceException ("Delimiter is not provided in the configuration");
	}
	
	Hashtable properties = new Hashtable();
	delim = delim.trim();	
	String sep = delim+delim;
	int sepLen = sep.length();
	String cache = prop;
	Vector methods = new Vector();
	
	while (cache.indexOf(sep) != -1) {
	    int index = cache.indexOf(sep);
	    String name = cache.substring(0,index);
	    if (name.trim() != "") {
	        methods.add(name);
	    	cache = cache.substring(index+sepLen);
	    }
	}
	
    	Enumeration allMethods = methods.elements();
    	while (allMethods.hasMoreElements()) {
    	    String oneMethod = (String) allMethods.nextElement();    	    
    	    if (!oneMethod.trim().equals("")) {
    	    	String methodName = null;
    	    	Vector parms = new Vector();
    	    	StringTokenizer methodDetails = new StringTokenizer(oneMethod,delim);
		for (int i=0; methodDetails.hasMoreTokens();i++ ) {
		    String token = (String) methodDetails.nextToken();
		    if (i==0) {
		    	methodName = token.toUpperCase();
		    } else {
		    	parms.add(token);		    	
		    }
		}
		properties.put(methodName,parms);
    	    }    	    
    	}
    	return properties;
    }
    
    /**
     * Creates a Datasource object according to the spec.
     *
     * @return	Initial DataSource Object instance.
     * @throws	<code>ResourceException</code> If class name is wrong or classpath is not set
     *		properly.
     */
    private Object getDataSourceObject() throws ResourceException{
    	String className = spec.getDetail(DataSourceSpec.CLASSNAME);
        try {            
            Class dataSourceClass = Class.forName(className);
            Object dataSourceObject = dataSourceClass.newInstance();
            System.out.println("V3-TEST : "  + dataSourceObject);
            return dataSourceObject;
        } catch(ClassNotFoundException cfne){
	    _logger.log(Level.SEVERE, "jdbc.exc_cnfe_ds", cfne);
	
            throw new ResourceException("Class Name is wrong or Class path is not set for :" + className);
        } catch(InstantiationException ce) {
	    _logger.log(Level.SEVERE, "jdbc.exc_inst", className);
            throw new ResourceException("Error in instantiating" + className);
        } catch(IllegalAccessException ce) {
	    _logger.log(Level.SEVERE, "jdbc.exc_acc_inst", className);
            throw new ResourceException("Access Error in instantiating" + className);
        }
    }
  
}
