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
 * JavaBeanConfigurator.java
 *
 */

package com.sun.enterprise.admin.selfmanagement.configuration;

import java.lang.reflect.Method;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.logging.ErrorManager;

import com.sun.enterprise.config.serverbeans.ElementProperty;


/**
 *
 * @author Sun Micro Systems, Inc
 */
public class JavaBeanConfigurator {
    
    private static final JavaBeanConfigurator singleton = 
            new JavaBeanConfigurator( );
    
    /** Creates a new instance of JavaBeanConfigurator */
    private  JavaBeanConfigurator() {
    }
    
    public static JavaBeanConfigurator getInstance( ) {
        return singleton;
    }
    
    
     /**
     *  A Utility method to instantiate a class and set the properties.
     */
    public Object configureBean( Object bean, ElementProperty[] properties ){
         setProperties( bean, properties );
         return bean;
    }
    
        
    /**
     * Utility method to get the declared fields.
     */
    private final Method[] getDeclaredMethods(final Class clz) {
        return (Method[]) AccessController.doPrivileged(new PrivilegedAction() {
       public Object run() {
                return clz.getDeclaredMethods();
            }
        });
    }
    
    /**
     *  Utility method to set properties to the instantiated Object.
     *  _REVISIT_: We can just do java.beans.statement possibly
     */
    private void setProperties( Object o, ElementProperty[] properties ) {
        if( properties == null ) return;
        Method[] methods = null;
        try {
            methods = getDeclaredMethods( o.getClass( ) );
            for( int i = 0; i < properties.length; i++ ) {
                ElementProperty property = properties[i];
                String propertyName = property.getName( ).toLowerCase( );
                String propertyValue = property.getValue( );
                for( int j = 0; j < methods.length; j++ ) {
                    String methodName = methods[j].getName().toLowerCase();
                    if ( ( methodName.startsWith( "set" ) )
                       && ( methodName.endsWith( propertyName ) ) )
                    {
                        Class[] parameterTypes = methods[j].getParameterTypes( );
                        if( parameterTypes.length != 1 ) {
                            new ErrorManager().error(
                                "Only one Parameter is allowed for the setter " +
                                " Method: " + methodName +
                                " has invalid signature", new Exception(),
                                ErrorManager.GENERIC_FAILURE );
                        }
                                                                                     
                        String parameterType = parameterTypes[0].getName();
                        Object[] parameters = new Object[1];
                                                                                     
                        if( parameterType.equals( "java.lang.String") ) {
                            parameters[0] = propertyValue;
                        } else if( parameterType.equals( "byte" ) ) {
                            parameters[0] =
                                new Byte( propertyValue.getBytes()[0]);
                        } else if( parameterType.equals( "int" ) ) {
                            parameters[0] = new Integer(propertyValue);
                        } else if( parameterType.equals( "float" ) ) {
                            parameters[0] = new Float(propertyValue);
                        } else if( parameterType.equals( "double") ) {
                            parameters[0] = new Double(propertyValue);
                        } else if( parameterType.equals( "char" ) ) {
                            parameters[0] =
                                new Character(propertyValue.charAt(0));
                        } else if( parameterType.equals("boolean") ) {
                            parameters[0] = new Boolean(propertyValue);
                        } else if( parameterType.equals("long") ) {
                            parameters[0] = new Long(propertyValue);
                        } else if( parameterType.equals("short") ) {
                             parameters[0] = new Short(propertyValue);
                        } else {
                            new ErrorManager().error(
                                "Only the basic primitive types can be set " +
                                "as properties to NotificationListener and " +
                                " NotificationFilter ", new Exception(),
                                ErrorManager.GENERIC_FAILURE );
                            continue;
                        }
                        methods[j].invoke( o,  parameters );
                    }
                }
            }
        } catch( Exception e ) {
            new ErrorManager().error(
                "Error While Setting properties to Notification Listener or " +
                " Filter ", e, ErrorManager.GENERIC_FAILURE );
        }
    }
}
