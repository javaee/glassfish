/*
 * Copyright 2003 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
 
package com.sun.jdbcra.util;

import java.lang.reflect.Method;
import java.lang.reflect.InvocationTargetException;
import java.util.Vector;
import javax.resource.ResourceException;

import java.util.logging.Logger;
import java.util.logging.Level;
/**
 * Execute the methods based on the parameters.
 *
 * @version	1.0, 02/07/23
 * @author	Binod P.G
 */
public class MethodExecutor implements java.io.Serializable{

    private static Logger _logger;
    static {
        _logger = Logger.getAnonymousLogger();
    }
    private boolean debug = false;
    /**
     * Exceute a simple set Method.
     *
     * @param	value	Value to be set.
     * @param	method	<code>Method</code> object.
     * @param	obj	Object on which the method to be executed.
     * @throws  <code>ResourceException</code>, in case of the mismatch of parameter values or
     *		a security violation.     
     */
    public void runJavaBeanMethod(String value, Method method, Object obj) throws ResourceException{
    	if (value==null || value.trim().equals("")) {
    	    return;
    	}
    	try {
    	    Class[] parameters = method.getParameterTypes();
    	    if ( parameters.length == 1) {
    	        Object[] values = new Object[1];
    	    	values[0] = convertType(parameters[0], value);
    	    	method.invoke(obj, values);
    	    }
    	} catch (IllegalAccessException iae) {
	    _logger.log(Level.SEVERE, "jdbc.exc_jb_val", iae);
    	    throw new ResourceException("Access denied to execute the method :" + method.getName());
    	} catch (IllegalArgumentException ie) {
	    _logger.log(Level.SEVERE, "jdbc.exc_jb_val", ie);
    	    throw new ResourceException("Arguments are wrong for the method :" + method.getName());    	
    	} catch (InvocationTargetException ite) {
	    _logger.log(Level.SEVERE, "jdbc.exc_jb_val", ite);
    	    throw new ResourceException("Access denied to execute the method :" + method.getName());    	
    	}
    }
    
    /**
     * Executes the method.     
     *
     * @param	method <code>Method</code> object.
     * @param	obj	Object on which the method to be executed.
     * @param	values	Parameter values for executing the method.
     * @throws  <code>ResourceException</code>, in case of the mismatch of parameter values or
     *		a security violation.
     */
    public void runMethod(Method method, Object obj, Vector values) throws ResourceException{
    	try {
	    Class[] parameters = method.getParameterTypes();
	    if (values.size() != parameters.length) {
	        return;
	    }
    	    Object[] actualValues = new Object[parameters.length];
    	    for (int i =0; i<parameters.length ; i++) {
    	    	String val = (String) values.get(i);
    	    	if (val.trim().equals("NULL")) {
    	    	    actualValues[i] = null;
    	    	} else {
    	    	    actualValues[i] = convertType(parameters[i], val);
    	    	}
    	    }
    	    method.invoke(obj, actualValues);
    	}catch (IllegalAccessException iae) {
	    _logger.log(Level.SEVERE, "jdbc.exc_jb_val", iae);
    	    throw new ResourceException("Access denied to execute the method :" + method.getName());
    	} catch (IllegalArgumentException ie) {
	    _logger.log(Level.SEVERE, "jdbc.exc_jb_val", ie);
    	    throw new ResourceException("Arguments are wrong for the method :" + method.getName());    	
    	} catch (InvocationTargetException ite) {
	    _logger.log(Level.SEVERE, "jdbc.exc_jb_val", ite);
    	    throw new ResourceException("Access denied to execute the method :" + method.getName());    	
    	}
    }
    
    /**
     * Converts the type from String to the Class type.
     *
     * @param	type		Class name to which the conversion is required.
     * @param	parameter	String value to be converted.
     * @return	Converted value.
     * @throws  <code>ResourceException</code>, in case of the mismatch of parameter values or
     *		a security violation.     
     */
    private Object convertType(Class type, String parameter) throws ResourceException{
    	try {
    	    String typeName = type.getName();
    	    if ( typeName.equals("java.lang.String") || typeName.equals("java.lang.Object")) {
    	    	return parameter;
    	    }
    	
    	    if (typeName.equals("int") || typeName.equals("java.lang.Integer")) {
    	    	return new Integer(parameter);
    	    }
    	    
    	    if (typeName.equals("short") || typeName.equals("java.lang.Short")) {
    	    	return new Short(parameter);
    	    }    	    
    	    
    	    if (typeName.equals("byte") || typeName.equals("java.lang.Byte")) {
    	    	return new Byte(parameter);
    	    }    	        	    
    	    
    	    if (typeName.equals("long") || typeName.equals("java.lang.Long")) {
    	    	return new Long(parameter);
    	    }    	        	       	    
    	    
    	    if (typeName.equals("float") || typeName.equals("java.lang.Float")) {
    	    	return new Float(parameter);
    	    }   
    	    
    	    if (typeName.equals("double") || typeName.equals("java.lang.Double")) {
    	    	return new Double(parameter);
    	    }    	        	       	     	        	       	    
    	    
    	    if (typeName.equals("java.math.BigDecimal")) {
    	    	return new java.math.BigDecimal(parameter);
    	    }    	        	       	    
    	    
    	    if (typeName.equals("java.math.BigInteger")) {
    	    	return new java.math.BigInteger(parameter);
    	    }    	        	       	        	    
    	    
    	    if (typeName.equals("boolean") || typeName.equals("java.lang.Boolean")) {
    	    	return new Boolean(parameter);
            }		

    	    return parameter;
    	} catch (NumberFormatException nfe) {
	    _logger.log(Level.SEVERE, "jdbc.exc_nfe", parameter);
    	    throw new ResourceException(parameter+": Not a valid value for this method ");
    	}
    }
    
}

