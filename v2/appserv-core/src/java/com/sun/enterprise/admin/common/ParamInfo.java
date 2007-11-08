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


package com.sun.enterprise.admin.common;

import java.util.HashMap;

/**
	A class to create the signatures of various methods of known classes/interfaces
	from object instances. This is handy when the methods that are called
	involve introspection. The caller of various methods in this class just
	provides the <code> instances </code> of various parameters of methods on
	target classes/interfaces. The class in turn does basic introspection and
	derives the <code> class </code> objects from instances and creates the
	signature to relieve the callers of doing this task.
	<p>
	The most generic use of methods in this class requires callers to form
	the arrays of objects before calling them. But convenience methods are
	provided with 1, 2, 3 and 4 parameters (which covers 90 % of signatures). 
	This way, callers are not forced to create object arrays every time.
	<p>
	Note that this class only handles java.lang.Object and its subclasses. Not
	designed for primitives. So methods of this class are not useful for
	methods that contain parameters that contain both primitives and Objects.
	<p>
	An important thing to note is that since this class tries to get the class
	from the passed <code> instance </code> of a class, it will always generate
	exact class (and none of its super classes). Thus it is unlike (and
	less powerful) than the static method call where one can call a method with
	an Object parameter passing instances of *any* of its subclasses.
	@author Senthil Chidambaram
	@version 1.0
*/

public class ParamInfo
{

    protected String	mOperationName  	= null;
    protected String[]	mSignature      	= null;
    protected Object[]	mParams         	= null;
    protected boolean	mForcePrimitives	= false;

    private static final HashMap mPrimitives         = createPrimitivesMap();
    private static final HashMap mPrimitiveClasses   = createPrimitiveClassesMap();

    /** Creates a lookup table to get primitive class names corresponding to their
     *  wrapper objects. 
     *  @return HashMap Returns the HashMap of the Class Primitive type key/value pairs.
     */

    // create a lookup table to get primitive class names corresponding to their wrapper objects

    private static HashMap createPrimitivesMap()
    {
        HashMap primitives = new HashMap();
        primitives.put(Integer.class, "int");
        primitives.put(Boolean.class, "boolean");
        primitives.put(Float.class, "float");
        primitives.put(Double.class, "double");
        primitives.put(Byte.class, "byte");
        primitives.put(Character.class, "char");
        primitives.put(Short.class, "short");
        primitives.put(Long.class, "long");

        return primitives;
    }
    
    private static HashMap createPrimitiveClassesMap()
    {
        HashMap primitiveClassMap = new HashMap();
        primitiveClassMap.put("int", Integer.TYPE);
        primitiveClassMap.put("boolean", Boolean.TYPE);
        primitiveClassMap.put("float", Float.TYPE);
        primitiveClassMap.put("double", Double.TYPE);
        primitiveClassMap.put("byte", Byte.TYPE);
        primitiveClassMap.put("char", Character.TYPE);
        primitiveClassMap.put("short", Short.TYPE);
        primitiveClassMap.put("long", Long.TYPE);

        return primitiveClassMap;
    }

    public static Class getPrimitiveClass(String type)
    {
        return ( (Class) mPrimitiveClasses.get(type) );
    }

    /**
            Forces the primitive type conversion to false.
            So that certain classes are not covnerted.
    */

    public void initCoercionOptions()
    {
        mForcePrimitives = false;
    }

    /**
    * Constructor takes the operationName, and an array of Object Parameters.
    * This constructor calls the  paramstoClassNames to set the signature
    * of the params array object.
    */

    public ParamInfo( String operationName, Object[] params )
    {
        mOperationName	= operationName;
        mParams		= params;

        initCoercionOptions();
        mSignature	= paramsToClassNames( params );
    }

    public ParamInfo(String operationName)
    {
        this(operationName, new Object[0]);
    }


    /**
    * Construct a ParamInfo object for an operation with a single parameter
    * This constructor calls the ParamInfo array object constructor to
    * set the signature for the param object.
    * @param operationName name of the operation
    * @param param			Parameter for the operation
    */

    public ParamInfo( String operationName, Object param )
    {
        this(operationName, new Object[]{param} );
    }


    /**
    * Construct a ParamInfo object for an operation with two parameters.
    * Then it calls the ParamInfo array object constructor to set the
    * signature for the parameters.
    *
    * @param operationName name of the operation
    * @param param1		Parameter for the operation
    * @param param2 	Second parameter for the operation
    */

    public ParamInfo( String operationName, Object param1, Object param2 )
    {
        this(operationName, new Object[]{param1, param2});
    }


    /**
    * Construct a ParamInfo object for an operation with three parameters.
    * Then it calls the ParamInfo array object constructor to set the
    * signature for the parameters.
    *
    * @param operationName name of the operation
    * @param param1		Parameter for the operation
    * @param param2 	Second parameter for the operation
    * @param param3		Third parameter for the operation
    */


    public ParamInfo( String operationName, Object param1,
        Object param2, Object param3 )
    {
        this(operationName, new Object[]{param1, param2, param3});
    }


    /**
    * Construct a ParamInfo object for an operation with four parameters.
    * Then it calls the ParamInfo array object constructor to set the
    * signature for the parameters.
    *
    * @param operationName name of the operation
    * @param param1	Parameter for the operation
    * @param param2 	Second parameter for the operation
    * @param param3     Third parameter for the operation
    * @param param4     Fourth parameter
    */

    public ParamInfo(   String  operationName, 
                        Object  param1, 
                        Object  param2, 
                        Object  param3, 
                        Object param4   )
    {
        this(operationName, new Object[]{param1, param2, param3, param4});
    }


    /**
    * Method returns the operationName.
    * @return operationName.
    */

    public String getOperationName()
    {
        return mOperationName;
    }


    /**
    * Method returns an array of params Object.
    * @return Object array of params.
    */

    public Object[] getParams()
    {
        return mParams;
    }


    /**
    * Method returns the String array of signatures.
    * @return String array of signatures.
    */

    public String[] getSignature()
    {
        return mSignature;
    }


    /**
    * Sets the classname(signature) for the parametes in the Object array.
    * @return String array of signatures
    */

    public String[] paramsToClassNames(Object[] params)
    {
        String[] signature = new String[params.length];

        for(int ctr = 0; ctr < params.length; ctr++)
        {
            Object primitive = null;

            if(mForcePrimitives)    // see if this object's class represents a primitive
            {
                primitive = mPrimitives.get(params[ctr].getClass());
            }
            /* Certain Operations on the MBean take primitive types like "int", instead
            * of the Integer object. We can't pass primitive types across the wire.
            * So, we're setting the signature to int, and passing Integer parameter.
            * We do this for all primitive types if mForcePrimitives is true.
            */
            if (primitive != null)
            {
                signature[ctr] = (String) primitive;
            }
            else
            {
                signature[ctr] = params[ctr].getClass().getName();
            }
        }
        return signature;
    }
}