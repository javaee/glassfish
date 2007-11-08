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

package com.sun.enterprise.admin.server.core.jmx;

//JDK imports
import java.util.Collection;
import java.util.Iterator;
import java.util.HashMap;
import java.util.ArrayList;
import java.lang.reflect.Modifier;
import java.lang.reflect.Method;
import java.lang.reflect.InvocationTargetException;

//Admin imports
import com.sun.enterprise.admin.common.ParamInfo;

//JMX imports
import javax.management.ReflectionException;
import javax.management.MBeanException;


/**
    A class to provide various introspection routines. This class can be
    enhanced to make it more specific for other types of objects like MBeans.
    Provides basic capabilities to introspect any class.
 
    @author Kedar Mhaswade
    @version
*/
public class Introspector
{

    protected Class mClassReflected = null;
    
    /** 
		Creates new Introspector for given class. The argument may not be
		null.
    */
    public Introspector (Class aClass)
    {
		if (aClass == null)
		{
			throw new IllegalArgumentException();
		}
		mClassReflected = aClass;
    }
    
    /**
		Tests whether this class implements an interface represented by
		the given Class Object. The Parameter may not be null. This class 
		implements an interface represented by parameter Class iff
		<li> parameter is not null &&
		<li> this class or any of its super classes implement the interface
			represented by parameter.

		@param aClass the Class that represents the class or interface for
			which the test is carried out.
		@return true if this Class implements given interface, false otherwise.
    */
	
    public boolean implementsInterface(Class aClass)
    {
		boolean implInterface	= false;
		Class	thisClass	= mClassReflected;

		while (aClass != null && thisClass != null && !implInterface)
		{
			Class[] interfaces = thisClass.getInterfaces();
			for (int i = 0 ; i < interfaces.length ; i++)
			{
				Class anInterface = interfaces[i];
				if (anInterface.getName().equals(aClass.getName()))
				{
					implInterface = true;
					break;
				}
			}
			thisClass = thisClass.getSuperclass();
		}
		return ( implInterface );
    }
    
    /**
		Tests whether this class extends the given class.
		Note that no class extends itself.
	 
		@param aClass a class 
		@return true if this class is a subclass of passed class, false otherwise.
    */

	public boolean extendsClass(Class aClass)
    {
		if (aClass == null)
		{
			return false;
		}

		boolean extendsClass	= false;
		Class	superClass	= mClassReflected.getSuperclass();

		while (superClass != null && !extendsClass)
		{
			if (superClass.getName().equals(aClass.getName()))
			{
			extendsClass = true;
			}
			superClass	= superClass.getSuperclass();
		}

		return ( extendsClass );
    }
    
	/**
		Returns a method in this class with given name and signature.
		<p>
		Note that the signature for primitive parameters is "int", "boolean",
		"char" and so on.
	 
		@param operationName String representing the name of method.
		@param signature array of Strings, each of which represents fully
			qualified class name of parameters in order.
		@return instance of Method if present, null otherwise.
		@throws ReflectionException if the class represented by any of the
			signature elements can't be loaded or there is no such method
			or there is a security constraint.
	*/
	
    public Method getMethod(String operationName, String[] signature)
        throws ClassNotFoundException, NoSuchMethodException, SecurityException
    {
        Method  method                  = null;
        Class[] parameterTypes          = null;
        if (signature != null)
        {
            parameterTypes = new Class[signature.length];
            for (int i = 0 ; i < signature.length ; i++)
            {
                String parameterName = signature[i];
                Class primitiveClass = ParamInfo.getPrimitiveClass(parameterName);
                boolean parameterIsPrimitive = ( primitiveClass != null );
                if (parameterIsPrimitive)
                {
                    parameterTypes[i] = primitiveClass;
                }
                else
                {
                    parameterTypes[i] = Class.forName(parameterName);
                }
            }
        }
        method = mClassReflected.getMethod(operationName, parameterTypes);
        return ( method );
    }
    
	/**
		Invokes the given method on this class with given runtime instance
		of target object and parameter instances of the method.
	 
		@param method instance of Method object representing operation to invoke.
		@param targetObject an instance of this class.
		@param actualParams an array of actual parameters to this method.
		@return object representing the return value of the method.
		@throws ReflectionException if the method can't be invoked.
		@throws MBeanException if the method ifself throws some exception.
	*/
    
	public Object invokeMethodOn(Method method, Object targetObject, 
                                     Object[] actualParams)
            throws java.lang.IllegalAccessException, 
                   java.lang.reflect.InvocationTargetException
    {
        Object result = null;

        if (method == null || targetObject == null)// || actualParams == null)
        {
            throw new IllegalArgumentException();
        }
        result = method.invoke(targetObject, actualParams);
        return ( result );
    }

	/*
		Returns instance of Class that represents the primitive type represented
		by String like "int", "char", "boolean", "short", "long", "byte",
		"float" and "double".
		
		@return the class as given by various TYPE fields in java.lang wrappers
			over primitives, null if there is no match.
	
	
    public static Class getPrimitiveClass(String name)
    {
        /*
            This code contains getting the key mapped onto known value
            in a hashmap. 
        Class primitiveClass = null;
        HashMap primitiveMap = ParamInfo.mPrimitives;
        Iterator primitiveClasses = primitiveMap.keySet().iterator();
        while (primitiveClasses.hasNext())
        {
            Class aClass            = (Class) primitiveClasses.next();
            String className        = (String) primitiveMap.get(aClass);
            if (className.equals(name))
            {
                primitiveClass = aClass;
                break;
            }
        }
        return ( primitiveClass );
    }
    */
	/**
		Returns an array of Methods that are <code> callable </callable>
		instance methods of this class/interface. Thus the returned array contains
		<li>
		declared non-abstract public methods
		<li>
		declared public methods of superclasses/superinterfaces excluding
		the classes/interfaces from excludeList param
	 
		@param excludeList Collection of complete classnames of those classes
		whose methods should not be included in the returned array. If null,
		all the methods will be returned. The excludeList can contain
		the name of this class itself.
		@return array of Method objects. It will contain zero elements, if
		there is no method.
	*/
	public Method[] getCallableInstanceMethods(Collection excludeList)
	{
		boolean includeAll = false;
		if (excludeList == null || excludeList.isEmpty())
		{
			includeAll = true;
		}
		
		ArrayList	methodList	= new ArrayList();
		Class		aClass		= mClassReflected;
		Method[]	methods		= null;	
		while (aClass != null)
		{
			boolean shouldInclude = 
				includeAll || ! excludeList.contains(aClass.getName());
			if (shouldInclude)
			{
				Method[] declMethods = aClass.getDeclaredMethods();
				for (int i = 0 ; i < declMethods.length ; i++)
				{
					int		modifiers					= declMethods[i].getModifiers();
					boolean isCallableInstanceMethod	= false;
					boolean isPublicMethod				= Modifier.isPublic(modifiers);
					boolean isAbstractMethod			= Modifier.isAbstract(modifiers);
					boolean isStaticMethod				= Modifier.isStatic(modifiers);
					
					isCallableInstanceMethod = isPublicMethod && !isAbstractMethod
						&& !isStaticMethod;
					if (isCallableInstanceMethod)
					{
						methodList.add(declMethods[i]);
					}
				}
			}
			aClass = aClass.getSuperclass();
		}
		methods = new Method[methodList.size()];
		return ((Method[])methodList.toArray(methods));
	}
	
	public Method[] getDeclaredConcretePublicMethods()
	{
		Method[]	declMethods		= mClassReflected.getDeclaredMethods();
		ArrayList	publicMethods	= new ArrayList();
		for (int i = 0 ; i < declMethods.length ; i++)
		{
			int		modifiers	= declMethods[i].getModifiers();
			boolean isPublic	= Modifier.isPublic (modifiers);
			boolean isAbstract	= Modifier.isAbstract(modifiers);
			if (isPublic && !isAbstract)
			{
				publicMethods.add(declMethods[i]);
			}
		}
		Method[] pMethods = new Method[publicMethods.size()];
		return ( (Method[])publicMethods.toArray (pMethods) );
	}
}