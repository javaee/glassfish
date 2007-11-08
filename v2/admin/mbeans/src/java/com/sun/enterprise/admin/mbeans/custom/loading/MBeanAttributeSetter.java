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


package com.sun.enterprise.admin.mbeans.custom.loading;

import com.sun.enterprise.admin.mbeans.custom.CMBStrings;
import com.sun.enterprise.admin.server.core.CustomMBeanException;
import java.lang.reflect.Constructor;
import java.util.*;
import com.sun.enterprise.admin.common.constant.AdminConstants;
import javax.management.Attribute;
import javax.management.AttributeNotFoundException;
import javax.management.InstanceNotFoundException;
import javax.management.InvalidAttributeValueException;
import javax.management.MBeanAttributeInfo;
import javax.management.MBeanException;
import javax.management.MBeanServer;
import javax.management.ObjectName;
import javax.management.ReflectionException;
import java.util.logging.Logger;

/** Class to set the attributes of given MBean taking into account the types of the
 * attributes.
 */
public class MBeanAttributeSetter {
    private static final Logger logger  = Logger.getLogger(AdminConstants.kLoggerName);
    private final MBeanServer mbs;
    private final ObjectName on;
    public MBeanAttributeSetter(final MBeanServer mbs, final ObjectName on) {
        this.mbs    = mbs;
        this.on     = on;
    }
    public void setIt(final String name, final String value) throws CustomMBeanException {
        
        try
        {
            final String at         = getAttributeType(name);
            final Object atv        = getAttributeValue(at, name, value); // construct the attribute from "String" value
            final Attribute atr     = new Attribute(name, atv);
            mbs.setAttribute(on, atr);
        }
        catch(CustomMBeanException cmbe)
        {
            throw cmbe;
        }
        catch(Exception e)
        {
            throw new CustomMBeanException(e);
        }
    }
    
    ///// Private Methods /////
    private String getAttributeType(final String name) throws CustomMBeanException { //gets it from MBeanInfo
        String type = null;
        List<String> attNames = new ArrayList<String>(); // just in case we need to log this info in the event of an error...
        try {
            MBeanAttributeInfo[] mais = mbs.getMBeanInfo(on).getAttributes();
            for (MBeanAttributeInfo mai : mais) {
                final String an = mai.getName();
                attNames.add(an);
                if (an.equals(name)) {
                    type = mai.getType();
                    break;
                }
            }
        } catch (final Exception e) { //TODO
            //even though one can land here, it is okay to squelch this exception
            // Log it and move on
            logger.info(CMBStrings.get("getAttributeTypeNonFatal", name));
        }
        if(type == null){
            // this is an error!  They probably misspelled the Attribute Name.
            // this code is NEVER called unless we are expecting to *find* the attribute
            String names = new String();
            for(int i = 0; i < attNames.size(); i++){
                if(i != 0)
                    names += ", ";
            
                names += attNames.get(i);
            }
            
            String mesg = CMBStrings.get("cmb.badAttribute", name, names);
            logger.warning(mesg);
            throw new CustomMBeanException(mesg);
        }
        return ( type );
    }
    /** Method to construct an instance of a  type from its String representation. Note that for
     this method to succeed, the type class has to have such a constructor that
     accepts a String and creates the instance of that type. Most of the primitive
     wrappers have such a constructor. This requirement has to be satisfied because
     the attributes of an MBean will be persisted to a config file and can only
     be read back as Strings.
     */
    private Object getAttributeValue(final String type, final String name, final String value) throws CustomMBeanException{
        Object valueObject = null;
        try {
            if (isPrimitive(type)) {
                //recursive call
                return ( getAttributeValue(toWrapper(type), name, value) );
            }
            final Class c                   = Class.forName(type);
            final Object[] params           = new Object[]{value};
            
            // special case...
            if(c.equals(Character.class)){
                if(value.length() != 1){
                    String mesg = CMBStrings.get("cmb.badCharAttribute", name, value);
                    logger.warning(mesg);
                    throw new CustomMBeanException(mesg);
                }
                valueObject = Character.valueOf(value.charAt(0));
            }
            else{
                final Constructor ctor          = c.getConstructor(new Class[]{java.lang.String.class});
                valueObject                     = ctor.newInstance(params);
            }
            
        } catch (final Throwable t) {
            final String msg = CMBStrings.get("attributeValueError", name, type, value);
            throw new CustomMBeanException (msg, t);
        }
        return ( valueObject );
    }
    
    private boolean isPrimitive(final String type) {
        boolean primitive = int.class.getName().equals(type) ||
			    char.class.getName().equals(type) ||
			    short.class.getName().equals(type) ||
			    byte.class.getName().equals(type) ||
			    boolean.class.getName().equals(type) ||
			    double.class.getName().equals(type) ||
			    float.class.getName().equals(type) ||
                            long.class.getName().equals(type);
        return ( primitive );
    }
    private String toWrapper(final String primitive) {
        return ( P2W.get(primitive) );
        
    }
    private static final Map<String, String> P2W = new HashMap<String, String>();
    static {
        P2W.put(int.class.getName(), java.lang.Integer.class.getName());
        P2W.put(char.class.getName(), java.lang.Character.class.getName());
        P2W.put(short.class.getName(), java.lang.Short.class.getName());
        P2W.put(byte.class.getName(), java.lang.Byte.class.getName());
        P2W.put(boolean.class.getName(), java.lang.Boolean.class.getName());
        P2W.put(double.class.getName(), java.lang.Double.class.getName());
        P2W.put(float.class.getName(), java.lang.Float.class.getName());
        P2W.put(long.class.getName(), java.lang.Long.class.getName());
    }
    ///// Private Methods /////
}