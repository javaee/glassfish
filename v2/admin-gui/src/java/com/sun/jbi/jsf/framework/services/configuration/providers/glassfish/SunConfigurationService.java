/*
 * The contents of this file are subject to the terms
 * of the Common Development and Distribution License
 * (the License).  You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the license at
 * https://glassfish.dev.java.net/public/CDDLv1.0.html or
 * glassfish/bootstrap/legal/CDDLv1.0.txt.
 * See the License for the specific language governing
 * permissions and limitations under the License.
 *
 * When distributing Covered Code, include this CDDL
 * Header Notice in each file and include the License file
 * at glassfish/bootstrap/legal/CDDLv1.0.txt.
 * If applicable, add the following below the CDDL Header,
 * with the fields enclosed by brackets [] replaced by
 * you own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Copyright 2007 Sun Microsystems, Inc. All rights reserved.
 */

/*
 * SunConfigurationService.java
 */
package com.sun.jbi.jsf.framework.services.configuration.providers.glassfish;

import com.sun.jbi.jsf.framework.common.GenericConstants;
import com.sun.jbi.jsf.framework.common.Util;
import com.sun.jbi.jsf.framework.connectors.ServerConnector;
import com.sun.jbi.jsf.framework.services.BaseServiceProvider;
import com.sun.jbi.jsf.framework.services.configuration.ConfigurationService;
import java.io.Serializable;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.logging.Logger;
import javax.management.Attribute;
import javax.management.AttributeList;
import javax.management.MBeanAttributeInfo;
import javax.management.MBeanInfo;
import javax.management.MBeanServerConnection;
import javax.management.ObjectName;

/**
 *
 * @author Sun Microsystems
 */
public class SunConfigurationService extends BaseServiceProvider implements Serializable, ConfigurationService {
    
    private Logger logger = Logger.getLogger(SunConfigurationService.class.getName());
    
    private static final String DOMAIN_NAME_PREFIX = "com.sun.ebi:";                    //$NON-NLS-1$
    private static final String SERVICE_TYPE_PREFIX="ServiceType=Configuration";        //$NON-NLS-1$
    private static final String INSTALLATION_TYPE_PREFIX = "InstallationType=";         //$NON-NLS-1$
    private static final String IDENTIFICATION_NAME_PREFIX = "IdentificationName=";     //$NON-NLS-1$
    private static final String IDENTIFICATION_SERVICE_UNIT = "ServiceUnitID=";         //$NON-NLS-1$
    
    private static final String SCHEMA_RETRIEVAL_OPERATION = "retrieveConfigurationDisplaySchema";   //$NON-NLS-1$
    private static final String DATA_RETRIEVAL_OPERATION = "retrieveConfigurationDisplayData";       //$NON-NLS-1$

    
    /** Creates a new instance of SunConfigurationService */
    public SunConfigurationService(ServerConnector connector,String targetName) {
        super(connector,targetName);
    }
    
    
    public Map<String,Object> getConfigurationProperties(String componentName, String componentType) {
        String name = getMBeanName(componentName,componentType);
        return getProperties(name);
    }
    
    public void setConfigurationProperties(String componentName, String componentType, Map<String,Object> props) {
        String name = getMBeanName(componentName,componentType);
        setProperties(name,props);
    }
    
     public Map<String,Object> getSUConfigurationProperties(String componentName, 
             String componentType, String serviceUnitId) {
        String name =
                    DOMAIN_NAME_PREFIX +
                    SERVICE_TYPE_PREFIX  + GenericConstants.COMMA_SEPARATOR +
                    INSTALLATION_TYPE_PREFIX  + Util.mapType(componentType) + GenericConstants.COMMA_SEPARATOR +
                    IDENTIFICATION_NAME_PREFIX + componentName +  GenericConstants.COMMA_SEPARATOR +
                    IDENTIFICATION_SERVICE_UNIT + serviceUnitId;
        return getProperties(name);
    }
     
     private String getMBeanName(String componentName, String componentType) {
         String name =
                 DOMAIN_NAME_PREFIX +
                 SERVICE_TYPE_PREFIX  + GenericConstants.COMMA_SEPARATOR +
                 INSTALLATION_TYPE_PREFIX  + Util.mapType(componentType) + GenericConstants.COMMA_SEPARATOR +
                 IDENTIFICATION_NAME_PREFIX + componentName;
         return name;
     }
   
    /**
     * get configuration properties
     * @param mbeanName
     * @return
     */
    private Map<String,Object> getProperties(String mbeanName) {
        
        Map<String,Object> properties = new LinkedHashMap<String,Object>();

        try {
            
            ObjectName objectName = new ObjectName(mbeanName);
            if ( serverConnection != null && serverConnection.isRegistered(objectName) ) {
                
                MBeanInfo mbeanInfo = serverConnection.getMBeanInfo(objectName);
                MBeanAttributeInfo[] attrs = mbeanInfo.getAttributes();
                //String[] attrNames = new String[attrs.length];
                for (int attrCount = 0; attrCount < attrs.length; attrCount++) {
                    String attrName = attrs[attrCount].getName();
                    try {
                        Object value = serverConnection.getAttribute(objectName, attrName);                
                        //if ( value != null ) {
                            properties.put(attrName, value);
                        //}
                    } catch(Exception e) {
                        e.printStackTrace();
                    }
                }
                
            }                

        } catch(Exception e) {
            e.printStackTrace();
        }

        return properties;
    }    

    /**
     * set configuration properties values
     * @param mbeanName -   name of configuration mbean
     * @param props     -   Map containing modified properties (key/value pairs)
     */ 
    public void setProperties(String mbeanName, Map<String,Object> props) {
         try {
            
            ObjectName objectName = new ObjectName(mbeanName);
            if ( serverConnection != null && serverConnection.isRegistered(objectName) ) {
                
                MBeanInfo mbeanInfo = serverConnection.getMBeanInfo(objectName);
                MBeanAttributeInfo[] attrs = mbeanInfo.getAttributes();
                //String[] attrNames = new String[attrs.length];
                for (int attrCount = 0; attrCount < attrs.length; attrCount++) {
                    String attrName = attrs[attrCount].getName();
                    String attrType = attrs[attrCount].getType();
                    Object attrValue =  props.get(attrName);
                    Attribute attr = createAttribute(attrName,attrType,attrValue);
                    if ( attr!=null ) {
                        try {
                            serverConnection.setAttribute(objectName,attr);
                        } catch(Exception e) {
                            logger.warning("Attr:" +attrName+" value:"+attrValue+" cannot be set!");
                            e.printStackTrace();
                        }
                    } else {
                        logger.warning("Attr:" +attrName+" value:"+attrValue+" cannot be set!");
                    }
                }     
                
           }                

        } catch(Exception e) {
            e.printStackTrace();
        }       
    }
    
    /**
     * create a new Attribute object based on the attribute type
     * Note: supported primitive types:
     *   Boolean
     *   Integer
     *   Long
     *   String
     */ 
    private Attribute createAttribute(String attrName,String attrType,Object attrValue) {
        
        Attribute attr = null;
        if ( attrValue != null ) {
            Object value = attrValue;
            try {
                if ( "java.lang.Boolean".equals(attrType) ) {
                    value = new Boolean(attrValue.toString());
                } else if ( "java.lang.Integer".equals(attrType) ) {
                    if ( attrValue==null || attrValue.equals("") ) {
                        value = new Integer(0);
                    } else {
                        value = new Integer(attrValue.toString());
                    }
                } else if ( "java.lang.Long".equals(attrType) ) {
                    if ( attrValue==null || attrValue.equals("") ) {
                        value = new Long(0);
                    } else {
                        value = new Long(attrValue.toString());
                    }
                } else if ( "java.lang.String".equals(attrType) ) {
                    value = new String(attrValue.toString());
                } else {
                    value = attrValue;
                }
                attr = new Attribute(attrName,value);
               
            } catch(Exception e) {
                e.printStackTrace();
            }
        }
        
        return attr;
    }
    
    
    public void setProperty(String name, Object value) {
        
    }

    public Object getProperty(String name) {
        return "";
    }

    public String getSchema(String componentName, String componentType) {
        String schema = null;
        try {
            String mbeanName = getMBeanName(componentName,componentType);
            ObjectName objectName = new ObjectName(mbeanName);
            if ( serverConnection != null && serverConnection.isRegistered(objectName) ) {
                schema = (String)serverConnection.invoke(objectName,SCHEMA_RETRIEVAL_OPERATION, null, null);
            }                
        } catch(Exception e) {
            //e.printStackTrace();
        }        
        return schema;
    }
    
    public String getXmlData(String componentName, String componentType) {
        String xmlData = null;
        try {
            String mbeanName = getMBeanName(componentName,componentType);
            ObjectName objectName = new ObjectName(mbeanName);
            if ( serverConnection != null && serverConnection.isRegistered(objectName) ) {
                xmlData = (String)serverConnection.invoke(objectName,DATA_RETRIEVAL_OPERATION, null, null);
            }                
        } catch(Exception e) {
            //e.printStackTrace();
        }           
        return xmlData;
    }
 
    public String[] getAttributeNames(String componentName, String componentType) {
        String[] attrNames = null;
        try {
            String mbeanName = getMBeanName(componentName,componentType);
            ObjectName objectName = new ObjectName(mbeanName);
            if ( serverConnection != null && serverConnection.isRegistered(objectName) ) {
                MBeanInfo mbeanInfo = serverConnection.getMBeanInfo(objectName);
                MBeanAttributeInfo[] attrs = mbeanInfo.getAttributes();
                attrNames = new String[attrs.length];
                for (int attrCount = 0; attrCount < attrs.length; attrCount++) {
                    attrNames[attrCount] = attrs[attrCount].getName();
                }                
            }                
        } catch(Exception e) {
            //e.printStackTrace();
        }           
        return attrNames;
    }   
}
