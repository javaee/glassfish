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

package com.sun.enterprise.admin.server.core.mbean.meta;

//JDK imports
import java.util.Vector;
import java.util.Collection;
import java.lang.reflect.Method;
import java.lang.reflect.Constructor;

//JMX imports
import javax.management.MBeanAttributeInfo;
import javax.management.MBeanConstructorInfo;
import javax.management.MBeanOperationInfo;
import javax.management.MBeanParameterInfo;
import javax.management.MBeanNotificationInfo;
import javax.management.MBeanInfo;
import javax.management.IntrospectionException;

//admin imports
import com.sun.enterprise.admin.server.core.jmx.Introspector;
/**
	A Class to build the MBeanInfo by providing the class name. Does not
	validate whether the class passed is an MBean. Things to note is that it
	provides the data in the form of JMX *Info Data Structures.

	@author Kedar Mhaswade
	@version 1.0
*/

public class MBeanInfoBuilder
{
	public static final String			kSetterPrefix		= "set";
	public static final String			kGetterPrefix		= "get";
	private Class						mClass				= null;
	private MBeanOperationInfo[]		mOperations			= null;
	private MBeanAttributeInfo[]		mAttributes			= null;
	private MBeanConstructorInfo[]		mConstructors		= null;
	private MBeanNotificationInfo[]		mNotifications		= null;
	private MBeanInfo					mMBeanInfo			= null;

	/** 
		Creates new MBeanInfoBuilder
	*/
	
    public MBeanInfoBuilder(Class aClass) throws IntrospectionException
	{
		if(aClass == null)
		{
			throw new IllegalArgumentException();
		}
		mClass = aClass;
		createOperations();
		createConstructors();
		createAttributes();
		createMBeanInfo();
		createNotifications();
    }
	
	public MBeanInfoBuilder(String aClassName)
	{
	}
	
	public MBeanInfoBuilder(String aClassName, ClassLoader cl)
	{
	}
	
	/**
		Returns only the public methods in the given MBean. These are the
		operations that can be called on this MBean with invoke method.
	 
		@return array of MBeanOperationInfo instances. If there is no
		such method that can be called an array with 0 elements is returned.
	*/
	
	public MBeanOperationInfo[] getMBeanOperations()
	{
		return ( mOperations );
	}
	public MBeanConstructorInfo[] getMBeanConstructors()
	{
		return ( mConstructors );
	}
	public MBeanInfo getMBeanInfo()
	{
		return ( mMBeanInfo );
	}
	public MBeanAttributeInfo[] getMBeanAttributes()
	{
		return ( mAttributes );
	}
	private void createOperations()
	{
		Collection excludeList	= new Vector();
		excludeList.add("java.lang.Object");
		excludeList.add("com.sun.enterprise.admin.server.core.mbean.config.AdminBase");
		Introspector reflector = new Introspector(mClass);
		Method[] methods = reflector.getCallableInstanceMethods(excludeList);
		Vector oprVector = new Vector();
		for (int i = 0 ; i < methods.length ; i++)
		{
			String name		= methods[i].getName();
			boolean isGetter = name.startsWith(kGetterPrefix);
			boolean isSetter = name.startsWith(kSetterPrefix);
			if (!isGetter && !isSetter)
			{
				oprVector.add(new MBeanOperationInfo(name, methods[i]));
			}
		}
		mOperations = new MBeanOperationInfo[oprVector.size()];
		oprVector.toArray (mOperations);
	}
	private void createConstructors()
	{
		Constructor[] ctors = mClass.getConstructors();
		mConstructors = new MBeanConstructorInfo[ctors.length];
		for (int i = 0 ; i < ctors.length ; i++)
		{
			String					ctorDesc = ctors[i].getName();
			MBeanConstructorInfo	ctorInfo = 
				new MBeanConstructorInfo(ctorDesc, ctors[i]);
			mConstructors[i] = ctorInfo;
		}
	}

	private void createNotifications()
	{
	}
	
	private void createMBeanInfo()
	{
		mMBeanInfo = new MBeanInfo(mClass.getName(), 
			mClass.getName(),
			mAttributes, mConstructors, mOperations, mNotifications);
	}
	/**
		This will only give the attributes in getX or setX style.
		Inheritence is not supported in here.
	*/
	private void createAttributes() throws IntrospectionException
	{
		Vector attrVector		= new Vector();
		Method[] declMethods	= new Introspector(mClass).getDeclaredConcretePublicMethods();
		Method[] getters		= this.getGetters(declMethods);
		Method[] setters		= this.getSetters(declMethods);
		
		for (int i = 0 ; i < getters.length ; i ++)
		{
			MBeanAttributeInfo attr		= null;
			String methodName			= getters[i].getName();
			String attrName				= getAttributeNameFromGetter(methodName);
			Method getter				= findAttrIn(attrName, getters);
			Method setter				= findAttrIn(attrName, setters);
			boolean isReadable			= ( getter != null );
			boolean isWritable			= ( setter != null );
			boolean isIs				= false;
			
			attr = new MBeanAttributeInfo(attrName, null, attrName,	
				isReadable, isWritable, isIs);
			attrVector.add(attr);
		}
		mAttributes = new MBeanAttributeInfo[attrVector.size()];
		attrVector.toArray(mAttributes);
	}
	
	private Method[] getGetters(Method[] methods)
	{
		Vector getters = new Vector();
		for (int i = 0 ; i < methods.length ; i++)
		{
			String methodName = methods[i].getName();
			if (methodName.startsWith (kGetterPrefix))
			{
				getters.add (methods[i]);
			}
		}
		Method[] getterMethods = new Method[getters.size()];
		
		return ( (Method[]) getters.toArray (getterMethods) );
	}
	
	private Method[] getSetters(Method[] methods)
	{
		Vector setters = new Vector();
		for (int i = 0 ; i < methods.length ; i++)
		{
			String methodName = methods[i].getName();
			if (methodName.startsWith (kSetterPrefix))
			{
				setters.add (methods[i]);
			}
		}
		Method[] setterMethods = new Method[setters.size()];
		
		return ( (Method[]) setters.toArray (setterMethods) );
	}
	
	private String getAttributeNameFromGetter(String getterName)
	{
		int getStartsAt = getterName.indexOf(kGetterPrefix);
		int attributeStartsAt = getStartsAt + kGetterPrefix.length();

		return ( getterName.substring (attributeStartsAt) );
	}
	
	private Method findAttrIn(String attrName, Method[] methods)
	{
		Method matcher = null;
		boolean found = false;
		
		for (int i = 0 ; i < methods.length ; i++)
		{
			String methodName = methods[i].getName();
			if (methodName.endsWith(attrName))
			{
				matcher = methods[i];
				break;
			}
		}
		return ( matcher );
	}
}