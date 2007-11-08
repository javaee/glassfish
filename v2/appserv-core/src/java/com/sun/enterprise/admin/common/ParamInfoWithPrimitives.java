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


/**
	A specialization of ParamInfo class that can handle both primitives and
	Objects. With this class, callers can form signatures of any methods.
	<p>
	Note a limitation of this class: It is not possible to use this class to
	call methods like <code> methodName(java.lang.Integer INTEGER, int integer)
	</code>. i.e. to say that the methods which contain both primitives and
	their object counterparts are not callable using this class.
	The reason for this is that when specifying the value of a primitive 
	in Object array, one has to create an instance of Object equivalent(e.g.
	int:java.lang.Integer, boolean:java.lang.Boolean and so on). It is not
	possible in Java to have Object[] params = new Object[]{10}.
	<p>
	Note that the names of primitives in signature will be: "int", "char", 
	"float", "double", "byte", "short" and "long".
 
	@author Senthil Chidambaram
	@version 1.0
*/

public class ParamInfoWithPrimitives extends ParamInfo
{

    public void initCoercionOptions()
    {
        mForcePrimitives = true;
    }

    /**
     * Constructor takes the operationName, and an array of params object.
     * This constructor calls the  paramstoClassNames to set the signature
     * of the params array object.
     */


	public ParamInfoWithPrimitives( String operationName, Object[] params )
	{
        super(operationName, params);
	}


    /**
     * Constructor takes the operationName, and a single param Object.
     * This constructor calls the ParamInfo array object constructor to
     * set the signature for the param object.
     */


	public ParamInfoWithPrimitives( String operationName, Object param )
	{
		this(operationName, new Object[]{param} );
	}


    /**
     * This constructor takes an operationName, and two param Objects.
     * Then it calls the ParamInfo array object constructor to set the
     * signature for the parameters.
     */


	public ParamInfoWithPrimitives( String operationName, Object param1, Object param2 )
	{
		this(operationName, new Object[]{param1, param2});
	}


    /**
     * This constructor takes an operationName, and three param Objects.
     * Then it calls the ParamInfo array object constructor like other
     * constructors to set the signature for the parameters.
     */


	public ParamInfoWithPrimitives( String operationName, Object param1,
            Object param2, Object param3 )
	{

		this(operationName, new Object[]{param1, param2,
                param3});
	}


    /**
     * This constructor takes an operationName, and four param objects.
     * Then it calls the array object constructor to set the signature
     * for the parameters.
     */


	public ParamInfoWithPrimitives( String operationName, Object param1,
            Object param2, Object param3, Object param4 )
	{

		this(operationName, new Object[]{param1, param2,
                param3, param4});
	}

}
