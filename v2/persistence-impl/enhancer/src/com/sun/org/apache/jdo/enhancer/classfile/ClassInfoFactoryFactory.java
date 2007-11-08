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

package com.sun.org.apache.jdo.enhancer.classfile;

/**
 * A Factory for creating a ClassInfoFactory. The getClassInfoFactory() method
 * 	looks for com.sun.persistence.enhancer.classinfo.factory system property. This property must
 * 	contains the name of the class that implements the ClassInfoFactory interface.
 * 
 * @see ClassInfoFactory
 * 
 * @author Mahesh Kannan
 *
 */
public class ClassInfoFactoryFactory {

	private static ClassInfoFactory _factory;
	
	private static final String FACTORY_CLASS_NAME_PROPERTY =
		"com.sun.persistence.enhancer.classinfo.factory";
	
	private static final String DEFAULT_FACTORY_CLASS_NAME =
		"com.sun.org.apache.jdo.impl.enhancer.classfile.asm.ClassInfoFactoryImpl";
	
	public static synchronized ClassInfoFactory getClassInfoFactory()
		throws IllegalAccessException, ClassNotFoundException, InstantiationException
	{
		if (_factory == null) {
			String className = System.getProperty(FACTORY_CLASS_NAME_PROPERTY);
			if (className == null) {
				className = DEFAULT_FACTORY_CLASS_NAME;
			}
			_factory = (ClassInfoFactory) Class.forName(className).newInstance();
		}
		
		return _factory;
	}
	
	public static void main(String[] args)
		throws Exception
	{
		ClassInfoFactory factory = ClassInfoFactoryFactory.getClassInfoFactory();
		System.out.println("Got Factory instance: " + factory);
	}
}
