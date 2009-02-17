/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 1997-2008 Sun Microsystems, Inc. All rights reserved.
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
package org.glassfish.admingui.util;

import com.sun.jsftemplating.annotation.Handler;
import com.sun.jsftemplating.annotation.HandlerInput;
import com.sun.jsftemplating.annotation.HandlerOutput;
import com.sun.jsftemplating.layout.descriptors.handler.HandlerContext;

import java.lang.reflect.Method;
import java.util.HashMap;


/**
 *  <p>	This class provides a fake implementation to any interface you
 *	provide to it.  You must access all data via it's get() method.</p>
 */
public class FakeImpl extends HashMap {
    /**
     *	Constructor.
     */
    public FakeImpl(Class cls) {
	theClass = cls;
    }

    /**
     *
     */
    @Override
    public Object get(Object key) {
	String getter = getGetterName(key.toString());
	try {
	    Method method = theClass.getMethod(getter);
	    Class returnType = method.getReturnType();

	    // Check Return type and decide what to return
	    if (String.class.isAssignableFrom(returnType)) {
		return key.toString() + STRING_DATA;
	    } else if (Integer.TYPE.isAssignableFrom(returnType)) {
		return INT_DATA;
	    } else if (Integer.class.isAssignableFrom(returnType)) {
		return INTEGER_DATA;
	    } else {
		return new FakeImpl(returnType);
	    }
	} catch (Exception ex) {
	    ex.printStackTrace();
	}

	// Failed!
	return null;
    }

    /**
     *	<p> This method changes <code>key</code> to a <code>getKey</code>
     *	    pattern.</p>
     */
    private String getGetterName(String key) {
	return "get" + ((char) (key.charAt(0) & 0xFFDF)) + key.substring(1);
    }

    /**
     *	<p> Outputs "&lt;classname&gt;.toString()".</p>
     */
    @Override
    public String toString() {
	return theClass.getName() + ".toString()";
    }

    /**
     *	<p> This handler creates a <code>FakeImpl</code> for the given class
     *	    (or Object).</p>
     */
    @Handler(id="getFakeImpl",
    	input={
	    @HandlerInput(name="class", type=Object.class, required=true)},
        output={
	    @HandlerOutput(name="fakeImpl", type=FakeImpl.class)})
    public static void getListElement(HandlerContext handlerCtx) {
	Object obj = (Object) handlerCtx.getInputValue("class");
	Class cls = null;
	if (obj instanceof Class) {
	    cls = (Class) obj;
	} else if ((obj instanceof String) && ((String) obj).endsWith(".class")) {
	    String className = (String) obj;
	    className = className.substring(0, className.lastIndexOf(".class"));
	    try {
		cls = Class.forName(className);
	    } catch (Exception ex) {
		ex.printStackTrace();
	    }
	} else {
	    cls = obj.getClass();
	}
	handlerCtx.setOutputValue("fakeImpl", new FakeImpl(cls));
    }

    private Class theClass;
    private static final String	    STRING_DATA	    = " - FakeData";
    private static final Integer    INTEGER_DATA    = 9;
    private static final int	    INT_DATA	    = 5;
}
