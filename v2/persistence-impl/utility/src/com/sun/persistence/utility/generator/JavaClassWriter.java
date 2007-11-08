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
 */

/*
 * JavaClassWriter.java
 *
 * Created on November 9, 2001, 2:27 PM
 */


package com.sun.persistence.utility.generator;

import java.io.IOException;

/**
 * This interface can be used to describe a java class -- either top level or
 * inner.  The resulting class definition can be viewed by calling {@link
 * java.lang.Object#toString}, but the entire source definition is only
 * available when used with a JavaFileWriter. <p> The order of the generated
 * code is up to the implementation.  For example, one implementation may accept
 * fields and methods in any order and generate all fields together and all
 * methods together, but maintain the order within the list of fields and
 * methods.  Another implementation may take the same input and generate fields
 * and methods interspersed among each other in exactly the order they were
 * added.  Still another implementation may make no guarantee as to the order of
 * members at all.
 * @author raccah
 */
public interface JavaClassWriter {
    /**
     * Sets the information for the class declaration including modifiers, name,
     * and comments.  Note that the name must not be fully qualified.
     * @param modifiers The modifier flags for this class.
     * @param className The (non-qualified) name of this class.
     * @param comments The comments shown just above the class declaration. The
     * comments are passed as an array so the line separators can be added by
     * the implementation.  Note that not all implementations will choose to
     * make use of this comment.
     * @throws IOException If the class declaration information cannot be set.
     * @see java.lang.reflect.Modifier
     */
    public void setClassDeclaration(int modifiers, String className,
            String[] comments) throws IOException;

    /**
     * Sets the superclass of this class.  Note that the name format must be
     * package style (that is - it can contain . but not / or $).
     * @param name The name of the superclass.
     * @throws IOException If the superclass cannot be set.
     */
    public void setSuperclass(String name) throws IOException;

    /**
     * Adds an interface to the list of those implemented by this class.
     * @param name The name of the interface.
     * @throws IOException If the interface cannot be added.
     */
    public void addInterface(String name) throws IOException;

    /**
     * Adds a field to the list of those declared by this class.  Note that the
     * type format must be package style (that is - it can contain . but not /
     * or $).
     * @param name The name of the field.
     * @param modifiers The modifier flags for this field.
     * @param type A string representing the type of this field.
     * @param initialValue A string representing the initial value of this
     * field.
     * @param comments The comments shown just above the declaration of this
     * field.  The comments are passed as an array so the line separators can be
     * added by the implementation.  Note that not all implementations will
     * choose to make use of this comment.
     * @throws IOException If the field information cannot be added.
     * @see java.lang.reflect.Modifier
     */
    public void addField(String name, int modifiers, String type,
            String initialValue, String[] comments) throws IOException;

    /**
     * Adds an initializer to this class.
     * @param isStatic True if this is a static initializer, false otherwise.
     * @param body The implementation block of the initializer.  The body of the
     * implementation is passed as an array so the line separators can be added
     * by the implementation.
     * @param comments The comments shown just above the initializer block. The
     * comments are passed as an array so the line separators can be added by
     * the implementation.  Note that not all implementations will choose to
     * make use of this comment.
     * @throws IOException If the initializer information cannot be added.
     */
    public void addInitializer(boolean isStatic, String[] body,
            String[] comments) throws IOException;

    /**
     * Adds a constructor to this class.  Note that the type format in the
     * parameter type strings must be package style (that is - it can contain .
     * but not / or $).
     * @param name The name of the constructor - should be the same as the name
     * of the class.
     * @param modifiers The modifier flags for this constructor.
     * @param parameterNames A list of parameter names.
     * @param parameterTypes A list of parameter types.
     * @param exceptions A list of exceptions.
     * @param body The implementation block of the constructor.  The body of the
     * implementation is passed as an array so the line separators can be added
     * by the implementation.
     * @param comments The comments shown just above the constructor.  The
     * comments are passed as an array so the line separators can be added by
     * the implementation.  Note that not all implementations will choose to
     * make use of this comment.
     * @throws IOException If the constructor information cannot be added.
     * @see java.lang.reflect.Modifier
     */
    public void addConstructor(String name, int modifiers,
            String[] parameterNames, String[] parameterTypes,
            String[] exceptions, String[] body, String[] comments)
            throws IOException;

    /**
     * Adds a method to this class.  Note that the type format in the return
     * type and parameter type strings must be package style (that is - it can
     * contain . but not / or $).
     * @param name The name of the method.
     * @param modifiers The modifier flags for this method.
     * @param returnType A string representing the return type of this method.
     * @param parameterNames A list of parameter names.
     * @param parameterTypes A list of parameter types.
     * @param exceptions A list of exceptions.
     * @param body The implementation block of the method.  The body of the
     * implementation is passed as an array so the line separators can be added
     * by the implementation.
     * @param comments The comments shown just above the method.  The comments
     * are passed as an array so the line separators can be added by the
     * implementation.  Note that not all implementations will choose to make
     * use of this comment.
     * @throws IOException If the method information cannot be added.
     * @see java.lang.reflect.Modifier
     */
    public void addMethod(String name, int modifiers, String returnType,
            String[] parameterNames, String[] parameterTypes,
            String[] exceptions, String[] body, String[] comments)
            throws IOException;

    /**
     * Adds an inner class to this class.
     * @param classWriter The definition of the inner class.
     * @throws IOException If the class information cannot be added.
     */
    public void addClass(JavaClassWriter classWriter) throws IOException;
}
