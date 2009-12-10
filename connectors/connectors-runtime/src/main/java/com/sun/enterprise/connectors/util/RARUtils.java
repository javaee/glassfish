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

package com.sun.enterprise.connectors.util;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;


import com.sun.enterprise.connectors.ConnectorRuntime;
import com.sun.enterprise.deployment.EjbMessageBeanDescriptor;
import com.sun.enterprise.deployment.ConnectorConfigProperty ;
import com.sun.enterprise.deployment.EnvironmentProperty;
import com.sun.logging.LogDomains;
import com.sun.enterprise.util.i18n.StringManager;

/**
 * This is a utility class to obtain the properties of a 
 * RA JavaBean housed in a RAR module/deployment dir, without exploding the RAR
 * contents. This method would be used by the admin-gui to configure
 * RA properties during RA deployment to a cluster.
 *  
 * @author Sivakumar Thyagarajan
 */
public class RARUtils {
    static Logger _logger = LogDomains.getLogger(ConnectorRuntime.class, LogDomains.RSR_LOGGER);
    private static StringManager localStrings = 
        StringManager.getManager( RARUtils.class );

    /**
     * Finds the properties of a RA JavaBean bundled in a RAR
     * without exploding the RAR
     * 
     * @param pathToDeployableUnit a physical,accessible location of the connector module.
     * [either a RAR for RAR-based deployments or a directory for Directory based deployments] 
     * @return A Map that is of <String RAJavaBeanPropertyName, String defaultPropertyValue>
     * An empty map is returned in the case of a 1.0 RAR 
     */
/* TODO V3
    public static Map getRABeanProperties (String pathToDeployableUnit) throws ConnectorRuntimeException {
        File f = new File(pathToDeployableUnit);
        if (!f.exists()){
            String i18nMsg = localStrings.getString(
                "rar_archive_not_found", pathToDeployableUnit);
            throw new ConnectorRuntimeException( i18nMsg );
        }
        if(f.isDirectory()) {
            return getRABeanPropertiesForDirectoryBasedDeployment(pathToDeployableUnit);
        } else {
            return getRABeanPropertiesForRARBasedDeployment(pathToDeployableUnit);
        }
    }
*/

/*
    private static Map getRABeanPropertiesForRARBasedDeployment(String rarLocation){
        ConnectorRARClassLoader jarCL = 
                            (new ConnectorRARClassLoader(rarLocation,  
                             ApplicationServer.getServerContext().getCommonClassLoader()));
        String raClassName = ConnectorDDTransformUtils.
                                    getResourceAdapterClassName(rarLocation);
        _logger.finer("RA class :  " + raClassName);
        Map hMap = new HashMap();
        try {
           hMap = extractRABeanProps(raClassName, jarCL);
        } catch (ClassNotFoundException e) {
            _logger.info(e.getMessage());
            _logger.log(Level.FINE, "Error while trying to find class " 
                            + raClassName + "in RAR at " + rarLocation, e);
        }
        return hMap;
    }
*/

/*
    private static Map getRABeanPropertiesForDirectoryBasedDeployment(
                    String directoryLocation) {
        Map hMap = new HashMap();
        //Use the deployment APIs to get the name of the resourceadapter
        //class through the connector descriptor
        try {
            ConnectorDescriptor cd = ConnectorDDTransformUtils.
                                getConnectorDescriptor(directoryLocation);
            String raClassName = cd.getResourceAdapterClass();
            
            File f = new File(directoryLocation);
            URLClassLoader ucl = new URLClassLoader(new URL[]{f.toURI().toURL()}, 
                                  ApplicationServer.getServerContext().getCommonClassLoader());
            hMap = extractRABeanProps(raClassName, ucl);
        } catch (IOException e) {
            _logger.info(e.getMessage());
            _logger.log(Level.FINE, "IO Error while trying to read connector" +
                   "descriptor to get resource-adapter properties", e);
        } catch (ClassNotFoundException e) {
            _logger.info(e.getMessage());
            _logger.log(Level.FINE, "Unable to find class while trying to read connector" +
                   "descriptor to get resource-adapter properties", e);
        } catch (ConnectorRuntimeException e) {
            _logger.info(e.getMessage());
            _logger.log(Level.FINE, "Error while trying to read connector" +
                   "descriptor to get resource-adapter properties", e);
        } catch (Exception e) {
            _logger.info(e.getMessage());
            _logger.log(Level.FINE, "Error while trying to read connector" +
                   "descriptor to get resource-adapter properties", e);
        }
        return hMap;
    }
*/

    /**
     * Extracts RA Bean properties via reflection.
     *   
     * @param raClassName The RA Bean class name.
     * @param classLoader the classloader to use to find the class.
     */
    private static Map extractRABeanProps(String raClassName, ClassLoader classLoader) 
                                    throws ClassNotFoundException {
        Map hMap = new HashMap();
        //Only if RA is a 1.5 RAR, we need to get RA JavaBean properties, else
        //return an empty map.
        if(raClassName.trim().length() != 0) {
            Class c = classLoader.loadClass(raClassName);
            if(_logger.isLoggable(Level.FINER)) printClassDetails(c);
            hMap = getJavaBeanProperties(c);
        }
        return hMap;
    }

/*
    public static void main(String[] args) {
        if(!(args.length >= 1)){
            System.out.println("<Usage> java RARUtils directory-path ");
            return;
        }
        
        Map hMap = RARUtils.getRABeanPropertiesForDirectoryBasedDeployment(args[0]);
        System.out.println("RA JavaBean Properties");
        System.out.println(hMap);
    }
*/

    private static Map getJavaBeanProperties(Class c) {
        Method[] m = c.getMethods();
        Map hMap = new HashMap();
        for (int i = 0; i < m.length; i++) {
            _logger.finer(m[i].getName());
            if(m[i].getName().startsWith("get") 
                    && isValidRABeanConfigProperty(m[i].getReturnType())) {
                hMap.put(m[i].getName().substring(3), m[i].getReturnType());
            }
        }
        
        //remove Object's Class attribute.
        hMap.remove("Class");
        return hMap;
    }

    /**
     * A valid resource adapter java bean property should either be one of the
     * following  
     * 1. A Java primitive or a primitve wrapper
     * 2. A String
     */
    public static boolean isValidRABeanConfigProperty(Class clz) {
        return (clz.isPrimitive() || clz.equals(String.class) 
                        || isPrimitiveWrapper(clz));
    }

    /**
     * Determines if a class is one of the eight java primitive wrapper classes
     */
    private static boolean isPrimitiveWrapper(Class clz) {
        return (clz.equals(Boolean.class) || clz.equals(Character.class) 
                 || clz.equals(Byte.class) || clz.equals(Short.class) 
                 || clz.equals(Integer.class) || clz.equals(Long.class)
                 || clz.equals(Float.class) || clz.equals(Double.class));
    }

    
    private static void printClassDetails(Class c) {
        Method[] m = c.getMethods();
        _logger.finer("Methods in " + c.getName());
        for (int i = 0; i < m.length; i++) {
            _logger.finer(m[i].toString());
        }
    }
    
   /**
     * Prepares the name/value pairs for ActivationSpec. <p>
     * Rule: <p>
     * 1. The name/value pairs are the union of activation-config on 
     *    standard DD (message-driven) and runtime DD (mdb-resource-adapter) 
     * 2. If there are duplicate property settings, the value in runtime 
     *    activation-config will overwrite the one in the standard 
     *    activation-config.
     */
    public static Set getMergedActivationConfigProperties(EjbMessageBeanDescriptor msgDesc) {
        
        Set mergedProps = new HashSet();
        Set runtimePropNames = new HashSet();
        
        Set runtimeProps = msgDesc.getRuntimeActivationConfigProperties();
        if(runtimeProps != null){
            Iterator iter = runtimeProps.iterator();
            while (iter.hasNext()) {
                EnvironmentProperty entry = (EnvironmentProperty) iter.next();
                mergedProps.add(entry);
                String propName = (String) entry.getName();
                runtimePropNames.add(propName);
            }
        }
        
        Set standardProps = msgDesc.getActivationConfigProperties();
        if(standardProps != null){
            Iterator iter = standardProps.iterator();
            while (iter.hasNext()) {
                EnvironmentProperty entry = (EnvironmentProperty) iter.next();
                String propName = (String) entry.getName();
                if (runtimePropNames.contains(propName))
                    continue;
                mergedProps.add(entry);
            }
        }
        
        return mergedProps;
        
    }
    
}
