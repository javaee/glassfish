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

package com.sun.org.apache.jdo.impl.enhancer.util;

import java.util.Collection;
import java.util.Iterator;
import java.util.Enumeration;
import java.util.List;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.IOException;
import java.io.DataInputStream;

import com.sun.org.apache.jdo.impl.enhancer.EnhancerFatalError;
import com.sun.org.apache.jdo.impl.enhancer.JdoMetaMain;
import com.sun.org.apache.jdo.impl.enhancer.meta.EnhancerMetaData;
import com.sun.org.apache.jdo.impl.enhancer.meta.EnhancerMetaDataFatalError;
import com.sun.org.apache.jdo.impl.enhancer.meta.EnhancerMetaDataUserException;

import com.sun.org.apache.jdo.enhancer.classfile.ClassFileParser;
import com.sun.org.apache.jdo.enhancer.classfile.ClassFileParserEventListener;
import com.sun.org.apache.jdo.enhancer.classfile.ClassFileParserEventListener.MethodInvocationType;
import com.sun.org.apache.jdo.enhancer.classfile.ClassInfo;
import com.sun.org.apache.jdo.enhancer.classfile.ClassInfoFactory;
import com.sun.org.apache.jdo.enhancer.classfile.ClassInfoFactoryFactory;
import com.sun.org.apache.jdo.enhancer.classfile.FieldInfo;
import com.sun.org.apache.jdo.enhancer.classfile.MethodDescriptor;
import com.sun.org.apache.jdo.enhancer.classfile.MethodInfo;

import com.sun.org.apache.jdo.enhancer.classfile.TypeHelper;


/**
 * Utility class for testing a class file for correct annotation.
 *
 * @author Martin Zaun
 */
public class AnnotationTest
    extends JdoMetaMain
    implements ClassFileParserEventListener
{
    // return values of internal test methods
    public static enum TestStatus {ERROR, OK, PASSED};

    // ----------------------------------------------------------------------

    private boolean verbose;
    private String className;
    private String classFileName;
	
	private ClassInfoFactory classInfoFactory = null;
    private ClassInfo classInfo = null;

	private PrintWriter strPW = new PrintWriter(new StringWriter());
	
    private TestStatus globalTestStatus = TestStatus.PASSED;
    private TestStatus currentMethodStatus = TestStatus.OK;
    
	private String methodNameAndArgsAsJavaString = null;
	private boolean skipMethod = false;
    
    public AnnotationTest(PrintWriter out,
                          PrintWriter err)
    {
        super(out, err);
    }

	public void onFieldDeclaration(FieldInfo fieldInfo) {
		//System.out.println("Field: " + fieldInfo);
	}

	public void onMethodBegin(MethodInfo methodInfo) {
        currentMethodStatus = TestStatus.OK;
		StringBuilder sbldr = new StringBuilder();
		sbldr.append(methodInfo.getName()).append("(");
        TypeHelper typeHelper = classInfoFactory.createTypeHelper();
		String[] types = typeHelper.getParamTypesAsJavaString(methodInfo.getDescriptor());
		String coma = "";
		for (String type : types) {
			sbldr.append(coma).append(type);
			coma = ", ";
		}
		sbldr.append(")");
		methodNameAndArgsAsJavaString = sbldr.toString();
		skipMethod = isJDOMethod(methodNameAndArgsAsJavaString);
	}

	public MethodDescriptor onFieldAccess(String currentMethodName, boolean isRead,
			String declClassName, String fieldName, String fieldType)
	{
		if (false) {
			System.out.println("\t" + (isRead ? "get" : "put")
				+ " " + declClassName + "." + fieldName + " " + fieldType);
		}
		
		TestStatus status = checkGetPutField(methodNameAndArgsAsJavaString, isRead,
				declClassName, fieldName, fieldType);	
        if (status == TestStatus.ERROR) {
            currentMethodStatus = TestStatus.ERROR;
        }
		
		return null;
	}
	
	public MethodDescriptor onMethodInvoke(MethodInvocationType invType,
            String currentMethodName,
			String declClassName, String methodName, String methodType,
			String returnType, String[] paramTypes)
	{
		if (false) {
			System.out.println("\t "
				+ declClassName + "." + methodName + " " + methodType);
		}
		
        TestStatus status = checkInvokeStatic(methodNameAndArgsAsJavaString, declClassName,
				methodName, methodType, returnType, paramTypes);

        if (status == TestStatus.ERROR) {
            currentMethodStatus = TestStatus.ERROR;
        } else if (status == TestStatus.PASSED) {
            if (currentMethodStatus == TestStatus.OK) {
                currentMethodStatus = TestStatus.PASSED;
            }
        }
		
		return null;
	}
	
	public void onMethodEnd(MethodInfo methodInfo) {
		String methodName = methodInfo.getName();
        if (currentMethodStatus == TestStatus.ERROR) {
            strPW.println("    !!! ERROR: incorrect annotation in: "
                        + methodName);
            //out.println(s.toString());
			globalTestStatus = TestStatus.ERROR;
        } else if (currentMethodStatus == TestStatus.OK) {
            if (verbose) {
				strPW.println("    --- not annotated: "
                            + methodName);
            }
        } else {
            if (verbose) {
				strPW.println("    +++ has correct annotation: "
                            + methodName);
            }
            if (globalTestStatus == TestStatus.OK) {
                globalTestStatus = TestStatus.PASSED;
            }
        }
	}
	

	
    private TestStatus checkGetPutField(String currentMethodName,
			boolean isRead, String declClassName, String fieldName, String fieldType) 
        throws EnhancerMetaDataUserException, EnhancerMetaDataFatalError
    {
        // check if field is known to be non-managed or not annotatable
        final TestStatus res;
        if (jdoMeta.isKnownNonManagedField(declClassName,
                                           fieldName, fieldType)) {
            if (false) { // verbose
				strPW.println("        --- unannotated field access: "
                            + declClassName + "." + fieldName);
            }
            res = TestStatus.OK;
        } else if (isJDOMethod(methodNameAndArgsAsJavaString)) {
            if (false) { // verbose
				strPW.println("        --- unannotated field access: "
                            + declClassName + "." + fieldName);
            } 
            res = TestStatus.OK;
        } else if (jdoMeta.isPersistenceCapableClass(declClassName)
                   && (fieldName.equals("jdoStateManager")
                       || fieldName.equals("jdoFlags"))) {
            if (false) { // verbose
				strPW.println("        --- unannotated field access: "
                            + declClassName + "." + fieldName);
            } 
            res = TestStatus.OK;
        } else {
			strPW.println("        !!! ERROR: missing annotation of field access: "
                        + methodNameAndArgsAsJavaString + ":: " + declClassName + "." + fieldName);
            res = TestStatus.ERROR;
        }

        return res;
    }

    private TestStatus checkInvokeStatic(String currentMethodName,
			String declClassName, String methodName, String methodType,
			String returnType, String[] paramTypes) 
        throws EnhancerMetaDataUserException, EnhancerMetaDataFatalError
    {
        if (!methodName.startsWith("jdoSet")
            && (!methodName.startsWith("jdoGet")
                || methodName.equals("jdoGetManagedFieldCount"))) {
            return TestStatus.OK;
        }
        final String fieldName = methodName.substring(6);

        final TestStatus res;
        final String fieldType;
        if (methodName.startsWith("jdoGet")) {
            fieldType = returnType;
        } else {
            fieldType = paramTypes[1];
        }
        affirm(fieldType != null);
        
        // check if field is known to be non-managed or non-annotable
        if (jdoMeta.isKnownNonManagedField(declClassName,
                                           fieldName, fieldType)) {
			strPW.println("        !!! ERROR: annotated access to non-managed field: "
                        + declClassName + "." + fieldName);
            res = TestStatus.ERROR;
        } else if (isJDOMethod(methodNameAndArgsAsJavaString)) {
			strPW.println("        !!! ERROR: annotated field access in JDO method: "
                        + declClassName + "." + fieldName);
            res = TestStatus.ERROR;
        } else {
            if (verbose) {
                strPW.println("        +++ annotated field access: "
                            + declClassName + "." + fieldName);
            }
            res = TestStatus.PASSED;
        }
        return res;
    }

    private TestStatus parseClass(PrintWriter out)
    {
        
		try {
			this.classInfoFactory = ClassInfoFactoryFactory.getClassInfoFactory();
		} catch (ClassNotFoundException cnfEx) {
			out.println("    !!! ERROR: Cannot get the ClassInfoFactory");
			return TestStatus.ERROR;
		} catch (IllegalAccessException illEx) {
			out.println("    !!! ERROR: Cannot cannot access constructor of ClassInfoFactory");
			return TestStatus.ERROR;
		} catch (InstantiationException instEx) {
			out.println("    !!! ERROR: Cannot instantiate ClassInfoFactory");
			return TestStatus.ERROR;
		}
		
        DataInputStream dis = null;
        try {
            affirm(className == null ^ classFileName == null);
            if (className != null) {
                dis = new DataInputStream(openClassInputStream(className));
            } else {
                dis = new DataInputStream(openFileInputStream(classFileName));
            }
			
            this. classInfo = classInfoFactory.createClassInfo(dis);

            // check user class name from ClassFile
            final String userClassName
                = classInfo.toJavaName();
            //^olsen: better throw user exception or error
            affirm(className == null || className.equals(userClassName));
            out.println("    +++ parsed classfile");
        } catch (ClassFormatError ex) {
            out.println("    !!! ERROR: format error when parsing class: "
                        + className);
            out.println("        error: " + err);
            return TestStatus.ERROR;
        } catch (IOException ex) {
            out.println("    !!! ERROR: exception while reading class: "
                        + className);
            out.println("        exception: " + ex);
            return TestStatus.ERROR;
        } finally {
            closeInputStream(dis);
        }

        affirm(classInfo);
        return TestStatus.OK;
    }

    private TestStatus test(PrintWriter out,
                     String className,
                     String classFileName)
        throws EnhancerMetaDataUserException, EnhancerMetaDataFatalError
    {
        this.className = className;
        this.classFileName = classFileName;
        affirm(className == null ^ classFileName == null);
        final String name = (className != null ? className : classFileName);

        if (verbose) {
            out.println("-------------------------------------------------------------------------------");
            out.println();
            out.println("Test class for correct annotation: "
                        + name + " ...");
        }
        
        globalTestStatus = TestStatus.ERROR;
        
        // check parsing class
        StringWriter s = new StringWriter();
        if (parseClass(new PrintWriter(s)) == TestStatus.ERROR) {
            out.println();
            out.println("!!! ERROR: failed parsing class: " + name);
            out.println(s.toString());
            return TestStatus.ERROR;
        }

        if (verbose) {
            out.println();
            out.println("+++ parsed class: " + name);
            out.println(s.toString());
        }
        
        // check annotation
        StringWriter sw = new StringWriter();
		strPW = new PrintWriter(sw);
		
		ClassFileParser parser = classInfoFactory.createClassFileParser();
		parser.registerClassFileParserEventListener(this);
		try {
            globalTestStatus = TestStatus.PASSED;
			parser.parse(classInfo);
		} catch (IOException ioEx) {
			//TODO
            globalTestStatus = TestStatus.ERROR;
		}
		
        strPW.flush();
        if (globalTestStatus == TestStatus.ERROR) {
            out.println();
            out.println("!!! ERROR: incorrect annotation: " + name);
            out.println(sw.toString());
            return TestStatus.ERROR;
        }
        
        if (globalTestStatus == TestStatus.OK) {
            out.println();
            out.println("--- class not annotated: " + name);
        } else {
            out.println();
            out.println("+++ class annotated: " + name);
        }
        if (verbose) {
            out.println(sw.toString());
        }

        return globalTestStatus;
    }

    protected int test(PrintWriter out,
                       boolean verbose,
                       List classNames,
                       List classFileNames)
    {
        affirm(classNames);
        this.verbose = verbose;

        out.println();
        out.println("AnnotationTest: Testing Classes for JDO Persistence-Capability Enhancement");

        int nofFailed = 0;
        final int all = classNames.size() + classFileNames.size();
        for (int i = 0; i < classNames.size(); i++) {
            if (test(out, (String)classNames.get(i), null) == TestStatus.ERROR) {
                nofFailed++;
            }
        }
        for (int i = 0; i < classFileNames.size(); i++) {
            if (test(out, null, (String)classFileNames.get(i)) == TestStatus.ERROR) {
                nofFailed++;
            }
        }
        final int nofPassed = all - nofFailed;

        out.println();
        out.println("AnnotationTest: Summary:  TESTED: " + all
                    + "  PASSED: " + nofPassed
                    + "  FAILED: " + nofFailed);
        return nofFailed;
    }
    
    // ----------------------------------------------------------------------

    /**
     * Run the annotation test.
     */
    protected int process()
    {
        //^olsen: to be extended for zip/jar file arguments
        return test(out, options.verbose.value,
                    options.classNames, options.classFileNames);
    }
	
	private static boolean isJDOMethod(String methodName) {
		return ((methodName.startsWith("jdo") 
                && !(methodName.equals("jdoPreStore()")
                     || methodName.equals("jdoPreDelete()")))
               || methodName.equals("readObject(java.io.ObjectInputStream)"));
	}

    static public void main(String[] args)
    {
        final PrintWriter out = new PrintWriter(System.out, true);
        out.println("--> AnnotationTest.main()");
        final AnnotationTest main = new AnnotationTest(out, out);
        int res = main.run(args);
        out.println("<-- AnnotationTest.main(): exit = " + res);
        System.exit(res);
    }
}
