

/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
 * 
 * Portions Copyright Apache Software Foundation.
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

package org.apache.tomcat.util.mx;

import java.io.*;
import java.net.*;
import java.lang.reflect.*;
import java.util.*;
import javax.management.*;

/**
 * DynamicMBean implementation using introspection to manage any
 * component that follows the bean/ant/Interceptor/Valve/Jk2 patterns.
 *
 * The class will wrap any component conforming to those patterns.
 *
 * @deprecated The same functionality ( and more ) is now available in
 *         commons-modeler
 * @author Costin Manolache
 */
public class DynamicMBeanProxy implements DynamicMBean {
    Object real;
    String name;
    
    Method methods[]=null;

    Hashtable attMap=new Hashtable();

    // key: attribute val: getter method
    Hashtable getAttMap=new Hashtable();

    // key: attribute val: setter method
    Hashtable setAttMap=new Hashtable();

    // key: operation val: invoke method
    Hashtable invokeAttMap=new Hashtable();

    static MBeanServer mserver=null;

    static Hashtable instances=new Hashtable();
    
    /** Create a Dynamic proxy, using introspection to manage a
     *  real tomcat component.
     */
    public DynamicMBeanProxy() {
        
    }

    public void setName(String name ) {
        this.name=name;
    }

    public String getName() {
        if( name!=null ) return name;

        if( real==null ) return null;

        name=generateName(real.getClass());
        return name;
    }

    /** If a name was not provided, generate a name based on the
     *  class name and a sequence number.
     */
    public static String generateName(Class realClass) {
        String name=realClass.getName();
        name=name.substring( name.lastIndexOf( ".") + 1 );
        Integer iInt=(Integer)instances.get(name );
        int seq=0;
        if (iInt != null) {
            seq = iInt.intValue();
            seq++;
            instances.put(name, Integer.valueOf(seq));
        }
        else {
            instances.put(name, Integer.valueOf(0));
        }
        return "name=" + name + ",seq=" + seq;
    }

    public static String createMBean( Object proxy, String domain, String name ) {
        try {
            DynamicMBeanProxy mbean=new DynamicMBeanProxy();
            mbean.setReal( proxy );
            if( name!=null ) {
                mbean.setName( name );
            } else {
                mbean.setName( generateName( proxy.getClass() ));
            }

            return mbean.registerMBean( domain );
        } catch( Throwable t ) {
            log.error( "Error creating mbean ", t );
            return null;
        }
    }
    
    public String registerMBean( String domain ) {
        try {
            // XXX use aliases, suffix only, proxy.getName(), etc
            String fullName=domain + ": " +  getName();
            ObjectName oname=new ObjectName( fullName );

            if(  getMBeanServer().isRegistered( oname )) {
                log.info("Unregistering " + oname );
                getMBeanServer().unregisterMBean( oname );
            }
            getMBeanServer().registerMBean( this, oname );
            return fullName;
        } catch( Throwable t ) {
            log.error( "Error creating mbean ", t );
            return null;
        }
    }

    public static void unregisterMBean( Object o, String name ) {
        try {
            ObjectName oname=new ObjectName( name );

            getMBeanServer().unregisterMBean( oname );
        } catch( Throwable t ) {
            log.error( "Error unregistering mbean ", t );
        }
    }

    public static MBeanServer getMBeanServer() {
        if( mserver==null ) {
            if( MBeanServerFactory.findMBeanServer(null).size() > 0 ) {
                mserver=(MBeanServer)MBeanServerFactory.findMBeanServer(null).get(0);
            } else {
                mserver=MBeanServerFactory.createMBeanServer();
            }
        }
        
        return mserver;
    }

    private boolean supportedType( Class ret ) {
        return ret == String.class ||
            ret == Integer.class ||
            ret == Integer.TYPE ||
            ret == Long.class ||
            ret == Long.TYPE ||
            ret == java.io.File.class ||
            ret == Boolean.class ||
            ret == Boolean.TYPE 
            ; 
    }
    
    /** Set the managed object.
     *
     * @todo Read an XML ( or .properties ) file containing descriptions,
     *       generated from source comments
     * @todo Also filter methods based on config ( hide methods/attributes )
     * @todo Adapters for notifications ( Interceptor hooks, etc ). 
     */
    public void setReal( Object realBean ) {
        real=realBean;
    }

    private void init() {
        if( methods!=null ) return;
        methods = real.getClass().getMethods();
        for (int j = 0; j < methods.length; ++j) {
            String name=methods[j].getName();
            
            if( name.startsWith( "get" ) ) {
                if( methods[j].getParameterTypes().length != 0 ) {
                    continue;
                }
                if( ! Modifier.isPublic( methods[j].getModifiers() ) ) {
                    //log.debug("not public " + methods[j] );
                    continue;
                }
                Class ret=methods[j].getReturnType();
                if( ! supportedType( ret ) ) {
                    if( log.isDebugEnabled() )
                        log.debug("Unsupported " + ret );
                    continue;
                }
                name=unCapitalize( name.substring(3));

                getAttMap.put( name, methods[j] );
                // just a marker, we don't use the value 
                attMap.put( name, methods[j] );
            } else if( name.startsWith( "is" ) ) {
                // not used in our code. Add later

            } else if( name.startsWith( "set" ) ) {
                Class params[]=methods[j].getParameterTypes();
                if( params.length != 1 ) {
                    continue;
                }
                if( ! Modifier.isPublic( methods[j].getModifiers() ) )
                    continue;
                Class ret=params[0];
                if( ! supportedType( ret ) ) {
                    continue;
                }
                name=unCapitalize( name.substring(3));
                setAttMap.put( name, methods[j] );
                attMap.put( name, methods[j] );
            } else {
                if( methods[j].getParameterTypes().length != 0 ) {
                    continue;
                }
                if( methods[j].getDeclaringClass() == Object.class )
                    continue;
                if( ! Modifier.isPublic( methods[j].getModifiers() ) )
                    continue;
                invokeAttMap.put( name, methods[j]);
            }
        }
    }

    /**
     * @todo Find if the 'className' is the name of the MBean or
     *       the real class ( I suppose first )
     * @todo Read (optional) descriptions from a .properties, generated
     *       from source
     * @todo Deal with constructors
     *       
     */
    public MBeanInfo getMBeanInfo() {
        if( methods==null ) {
            init();
        }
        try {
            MBeanAttributeInfo attributes[]=new MBeanAttributeInfo[attMap.size()];

            Enumeration en=attMap.keys();
            int i=0;
            while( en.hasMoreElements() ) {
                String name=(String)en.nextElement();
                attributes[i++]=new MBeanAttributeInfo(name, "Attribute " + name ,
                                                       (Method)getAttMap.get(name),
                                                       (Method)setAttMap.get(name));
            }
            
            MBeanOperationInfo operations[]=new MBeanOperationInfo[invokeAttMap.size()];
            
            en=invokeAttMap.keys();
            i=0;
            while( en.hasMoreElements() ) {
                String name=(String)en.nextElement();
                Method m=(Method)invokeAttMap.get(name);
                if( m!=null && name != null ) {
                    operations[i++]=new MBeanOperationInfo(name, m);
                } else {
                    if (log.isDebugEnabled())
                        log.debug("Null arg " + name + " " + m );
                }
            }
            
            if( log.isDebugEnabled() )
                log.debug(real.getClass().getName() +  " getMBeanInfo()");
            
            return new MBeanInfo( real.getClass().getName(), /* ??? */
                                  "MBean for " + getName(),
                                  attributes,
                                  new MBeanConstructorInfo[0],
                                  operations,
                                  new MBeanNotificationInfo[0]);
        } catch( Exception ex ) {
            ex.printStackTrace();
            return null;
        }
    }

    static final Object[] NO_ARGS_PARAM=new Object[0];
    
    public Object getAttribute(String attribute)
        throws AttributeNotFoundException, MBeanException, ReflectionException
    {
        if( methods==null ) init();
        Method m=(Method)getAttMap.get( attribute );
        if( m==null ) throw new AttributeNotFoundException(attribute);

        try {
            if( log.isDebugEnabled() )
                log.debug(real.getClass().getName() +  " getAttribute " + attribute);
            return m.invoke(real, NO_ARGS_PARAM );
        } catch( IllegalAccessException ex ) {
            ex.printStackTrace();
            throw new MBeanException( ex );
        } catch( InvocationTargetException ex1 ) {
            ex1.printStackTrace();
            throw new MBeanException( ex1 );
        }
    }
    
    public void setAttribute(Attribute attribute)
        throws AttributeNotFoundException, InvalidAttributeValueException, MBeanException, ReflectionException
    {
        if( methods==null ) init();
        // XXX Send notification !!!
        Method m=(Method)setAttMap.get( attribute.getName() );
        if( m==null ) throw new AttributeNotFoundException(attribute.getName());

        try {
            log.info(real.getClass().getName() +  "setAttribute " + attribute.getName());
            m.invoke(real, new Object[] { attribute.getValue() } );
        } catch( IllegalAccessException ex ) {
            ex.printStackTrace();
            throw new MBeanException( ex );
        } catch( InvocationTargetException ex1 ) {
            ex1.printStackTrace();
            throw new MBeanException( ex1 );
        }
    }
    
    /**
     * Invoke a method. Only no param methods are supported at the moment
     * ( init, start, execute, etc ) ( that's the most common pattern we have
     *  in tomcat/ant/etc )
     *
     * @todo Implement invoke for methods with more arguments.
     */
    public Object invoke(String method, Object[] arguments, String[] params)
        throws MBeanException, ReflectionException
    {
        if( methods==null ) init();
        Method m=(Method)invokeAttMap.get( method );
        if( m==null ) return null;

        try {
            log.info(real.getClass().getName() +  "invoke " + m.getName());
            return m.invoke(real, NO_ARGS_PARAM );
        } catch( IllegalAccessException ex ) {
            throw new MBeanException( ex );
        } catch( InvocationTargetException ex1 ) {
            throw new MBeanException( ex1 );
        }
    }


    // -------------------- Auxiliary methods --------------------
    
    public AttributeList setAttributes(AttributeList attributes) {
        Iterator attE=attributes.iterator();
        while( attE.hasNext() ) {
            Attribute att=(Attribute)attE.next();

            try {
                setAttribute( att );
            } catch( Exception ex ) {
                ex.printStackTrace();
            }
        }
        return attributes;
    }

    public AttributeList getAttributes(String[] attributes) {
        AttributeList al=new AttributeList();
        if( attributes==null ) return null;
        
        for( int i=0; i<attributes.length; i++ ) {
            try {
                Attribute att=new Attribute( attributes[i], getAttribute( attributes[i] ));
                al.add( att );
            } catch( Exception ex ) {
                ex.printStackTrace();
            }
        }
        return al;
    }
    

    // -------------------- Utils --------------------

    public static String unCapitalize(String name) {
	if (name == null || name.length() == 0) {
	    return name;
	}
	char chars[] = name.toCharArray();
	chars[0] = Character.toLowerCase(chars[0]);
	return new String(chars);
    }

    private static com.sun.org.apache.commons.logging.Log log=
        com.sun.org.apache.commons.logging.LogFactory.getLog( DynamicMBeanProxy.class );

}
