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
 * Copyright 2005 The Apache Software Foundation.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at 
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the License is distributed on an "AS IS" BASIS, 
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. 
 * See the License for the specific language governing permissions and 
 * limitations under the License.
 */

package com.sun.org.apache.jdo.impl.enhancer.jdo.impl;

import java.util.Iterator;

import com.sun.org.apache.jdo.impl.enhancer.meta.EnhancerMetaData;
import com.sun.org.apache.jdo.impl.enhancer.util.Support;

import com.sun.org.apache.jdo.enhancer.classfile.AnnotaterCallback;
import com.sun.org.apache.jdo.enhancer.classfile.ClassInfo;
import com.sun.org.apache.jdo.enhancer.classfile.MethodDescriptor;
import com.sun.org.apache.jdo.enhancer.classfile.MethodInfo;
import com.sun.org.apache.jdo.impl.enhancer.jdo.ClassFileBuilder;


/**
 * Handles the augmentation actions for a method.
 */
class Annotater extends Support
	implements JDOConstants, AnnotaterCallback {//VMConstants, 
	
	/**
	 * The classfile's enhancement controller.
	 */
	private final Controller control;

	/**
	 * The class analyzer for this class.
	 */
	private final Analyzer analyzer;

	/**
	 * The classfile to be enhanced.
	 */
	private final ClassInfo classInfo;

	/**
	 * The class name in user ('.' delimited) form.
	 */
	private final String userClassName;

	/**
	 * Repository for the enhancer options.
	 */
	private final Environment env;

	/**
	 * Repository for JDO meta-data on classes.
	 */
	private final EnhancerMetaData meta;

	private boolean annotated = false;
    
	/**
	 * Constructor
	 */
	public Annotater(Controller control, Analyzer analyzer, Environment env) {
		affirm(control != null);
		affirm(analyzer != null);
		affirm(env != null);

		this.control = control;
		this.analyzer = analyzer;
		this.env = env;
		this.meta = env.getEnhancerMetaData();
		this.classInfo = control.getClassInfo();
		this.userClassName = classInfo.toJavaName();
        
		affirm(classInfo != null);
		affirm(userClassName != null);
		affirm(meta != null);
	}

	/**
	 * Performs necessary annotation actions on the class.
	 */
	public void annotate() {
        if (analyzer.isPropertyBasedPersistence()) {
            ClassFileBuilder builder = control.getClassFileBuilder();
            builder.annotateForPropertyBasedPersistence();
        } else {
            annotateForFieldBasedPersistence();
        }
    }
    
    private void annotateForFieldBasedPersistence() {
		affirm(analyzer.isAnnotateable() && !env.noAnnotate());
		env.message("annotating class " + userClassName);

		ClassFileBuilder builder = control.getClassFileBuilder();
		boolean annotated = false;
		for (final Iterator i = analyzer.getAnnotatableMethods().iterator(); i
				.hasNext();) {
			final MethodInfo method = (MethodInfo) i.next();
			builder.mediateFieldAccess(method, this);
		}

		// notify controller if class changed
		if (annotated) {
			control.noteUpdate();
		}

	}
	
	/** Methods for AnnotaterCallback **/
	
	public MethodDescriptor onStaticMethodInvoke(String currentMethodName,
			String ownerClassName, String fieldName, String fieldType,
			String returnType, String[] paramtypes)
	{
		return null;
	}
	
	public MethodDescriptor onFieldAccess(String currentMethodName,  boolean isRead,
			String qualifyingClassName,	String fieldName, String fieldType) {

		// get the field's declaring class from the model
		final String declClassName = meta.getDeclaringClass(
				qualifyingClassName, fieldName);
		affirm(declClassName != null, "Cannot get declaring class of "
				+ qualifyingClassName + "." + fieldName);
		// check if field is known to be non-managed
		if (meta.isKnownNonManagedField(declClassName, fieldName, fieldType)) {
			return null;
		}

		// never annotate a jdo field; such may occur in pre-enhanced clone()
		if (meta.isPersistenceCapableClass(declClassName)
				&& (fieldName
						.equals(JDO_PC_MemberConstants.JDO_PC_jdoStateManager_Name) || fieldName
						.equals(JDO_PC_MemberConstants.JDO_PC_jdoFlags_Name))) {
			return null;
		}
		
		this.annotated = true;
		if (isRead) {
			return new MethodDescriptor(declClassName, "jdoGet" + fieldName, "(L"
				+ declClassName + ";)" + fieldType);
		} else {
            return new MethodDescriptor(declClassName, "jdoSet" + fieldName, "(L"
					+ declClassName + ";" + fieldType + ")V");
		}
	}

}
