/*
 * The contents of this file are subject to the terms
 * of the Common Development and Distribution License
 * (the "License").  You may not use this file except
 * in compliance with the License.
 *
 * You can obtain a copy of the license at
 * glassfish/bootstrap/legal/CDDLv1.0.txt or
 * https://glassfish.dev.java.net/public/CDDLv1.0.html.
 * See the License for the specific language governing
 * permissions and limitations under the License.
 *
 * When distributing Covered Code, include this CDDL
 * HEADER in each file and include the License file at
 * glassfish/bootstrap/legal/CDDLv1.0.txt.  If applicable,
 * add the following below this CDDL HEADER, with the
 * fields enclosed by brackets "[]" replaced with your
 * own identifying information: Portions Copyright [yyyy]
 * [name of copyright owner]
 *
 * Copyright 2005 Sun Microsystems, Inc. All rights reserved.
 */ 

package javax.el;

/**
 * Holds information about a method that a {@link MethodExpression} 
 * evaluated to.
 *
 * @since JSP 2.1
 */
public class MethodInfo {
    
    /** 
     * Creates a new instance of <code>MethodInfo</code> with the given
     * information.
     *
     * @param name The name of the method
     * @param returnType The return type of the method
     * @param paramTypes The types of each of the method's parameters
     */
    public MethodInfo(String name, Class<?> returnType, Class<?>[] paramTypes) {
        this.name = name;
        this.returnType = returnType;
        this.paramTypes = paramTypes;
    }

    /**
     * Returns the name of the method
     *
     * @return the name of the method
     */
    public String getName() {
        return this.name;
    }
    
    /**
     * Returns the return type of the method
     *
     * @return the return type of the method
     */
    public Class<?> getReturnType() {
        return this.returnType;
    }
    
    /**
     * Returns the parameter types of the method
     *
     * @return the parameter types of the method
     */
    public Class<?>[] getParamTypes() {
        return this.paramTypes;
    }
    
    private String name;
    private Class<?> returnType;
    private Class<?>[] paramTypes;
}
