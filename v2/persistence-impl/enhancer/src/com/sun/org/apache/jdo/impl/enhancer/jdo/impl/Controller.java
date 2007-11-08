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

import java.io.IOException;
import java.io.PrintWriter;
import java.io.ByteArrayOutputStream;

import com.sun.org.apache.jdo.impl.enhancer.EnhancerUserException;
import com.sun.org.apache.jdo.impl.enhancer.util.Support;
import com.sun.org.apache.jdo.enhancer.classfile.ClassInfo;
import com.sun.org.apache.jdo.enhancer.classfile.ClassInfoFactory;

import com.sun.org.apache.jdo.impl.enhancer.jdo.ClassFileBuilderFactory;
import com.sun.org.apache.jdo.impl.enhancer.jdo.ClassFileBuilder;

/**
 * Controls the enhancement of a class.
 */
public final class Controller
    extends Support
    implements EnhancerConstants
{
    /**
     * Repository for enhancer options.
     */
    private final Environment env;

    /**
     * The ClassInfo to be enhanced.
     */
    private final ClassInfo	classInfo;

    /**
     * The class name in user ('.' delimited) form.
     */
    private final String userClassName;

    /**
     * The analyzer for this class.
     */
    private final Analyzer analyzer;

    /**
     * The augmentation controller for this class.
     */
    private final Augmenter augmenter;

    /**
     * The method annotation controller for this class.
     */
    private final Annotater annotater;
	
	/**
	 * The class file builder for this class
	 */
	private ClassFileBuilder classFileBuilder;

    /**
     * If true, this class is believed to have been modified in some way.
     */
    private boolean classUpdated = false;

    private ClassInfoFactory classInfoFactory;
    
    private ClassFileBuilderFactory classFileBuilderFactory;
    
    /**
     * Constructor.
     */
    public Controller(ClassInfoFactory factory, ClassInfo classInfo,
                      ClassFileBuilderFactory builderFactory, Environment env)
    {
        affirm(classInfo != null);
        affirm(env != null);

        this.classInfoFactory = factory;
        this.classInfo = classInfo;
        this.classFileBuilderFactory = builderFactory;
        
        this.userClassName = classInfo.toJavaName();
        this.env = env;
        
        this.analyzer = new Analyzer(this, env);
        this.annotater = new Annotater(this, analyzer, env);
        this.augmenter = new Augmenter(this, analyzer, env);
		
        affirm(userClassName != null);
        affirm(analyzer != null);
        affirm(augmenter != null);
        affirm(annotater != null);
    }

    // ------------------------------------------------------------

    /**
     * Returns the class file which we are operating on.
     */
    public ClassInfo getClassInfo()
    {
        return classInfo;
    }

    /**
     * Returns true if the ClassInfo has been updated.
     */
    public boolean updated()
    {
        return classUpdated;
    }

    /**
     * Records a modification of the class.
     */
    void noteUpdate()
    {
        classUpdated = true;
    }
	
	ClassFileBuilder getClassFileBuilder() {
		return this.classFileBuilder;
	}

    // ------------------------------------------------------------

    public boolean isFieldBasedPersistence() {
        return true;
    }
    
    /**
     * Determines what modifications are needed and perform them.
     */
    public void enhanceClass()
    {
        try{
            if (env.doTimingStatistics()) {
                Support.timer.push("Controller.enhanceClass()");
            }

            // examine classes
            scan();
            if (env.errorCount() > 0)
                return;
            
            // augment class
            this.classFileBuilder
                = classFileBuilderFactory.createClassFileBuilder(classInfo, analyzer);
            affirm(classFileBuilder != null);
            augment();
            if (env.errorCount() > 0)
                return;
			
			// annotate class
            annotate();                
            if (env.errorCount() > 0)
                return;

            update();
        } finally {
            if (env.doTimingStatistics()) {
                Support.timer.pop();
            }
        }
    }

    // ------------------------------------------------------------

    /**
     * Notes the class characteristics.
     */
    private void scan()
    {
        if (analyzer.isAnalyzed()) {
            return;
        }

        try {
            if (env.doTimingStatistics()) {
                Support.timer.push("Controller.scan()");
            }

            if (env.dumpClass()) {
                dumpClass();
            }

            analyzer.scan();
        } finally {
            if (env.doTimingStatistics()) {
                Support.timer.pop();
            }
        }
    }

    /**
     * Performs necessary augmentation actions on the class.
     */
    private void augment()
    {
        if (!analyzer.isAugmentable() || env.noAugment()) {
            return;
        }

        try{
            if (env.doTimingStatistics()) {
                Support.timer.push("Controller.augment()");
            }
            augmenter.augment();

            if (env.dumpClass()) {
                dumpClass();
            }
        } finally {
            if (env.doTimingStatistics()) {
                Support.timer.pop();
            }
        }
    }

    /**
     * Performs necessary annotation actions on the class.
     */
    private void annotate()
    {
        if (!analyzer.isAnnotateable() || env.noAnnotate()) {
            return;
        }

        try{
            if (env.doTimingStatistics()) {
                Support.timer.push("Controller.annotate()");
            }
            annotater.annotate();

            if (env.dumpClass()) {
                dumpClass();
            }
        } finally {
            if (env.doTimingStatistics()) {
                Support.timer.pop();
            }
        }
    }

    /**
     * Marks the class being enhanced.
     */
    private void update()
    {
        if (!classUpdated) {
            return;
        }
    
        affirm((analyzer.isAugmentable() && !env.noAugment())
               || (analyzer.isAnnotateable() && !env.noAnnotate()));

		//TBD: Uncomment the following
		/*
        //^olsen: move non-modifying code to Analyzer
        final byte[] data = new byte[2];
        data[0] = (byte)(SUNJDO_PC_EnhancedVersion >>> 8);
        data[1] = (byte)(SUNJDO_PC_EnhancedVersion & 0xff);
        final ClassAttribute annotatedAttr
            = new GenericAttribute(
                classInfo.pool().addUtf8(SUNJDO_PC_EnhancedAttribute),
                data);
        classInfo.attributes().addElement(annotatedAttr);
        */
    }
    
    /**
     * Dumps a class' signature and byte-code (for debugging).
     */
    private void dumpClass()
    {
        final ByteArrayOutputStream bs = new ByteArrayOutputStream();
        final PrintWriter pw = new PrintWriter(bs);
        env.messageNL("dumping class " + userClassName + " {");
        classInfo.dump(pw);
        env.getOutputWriter().println(bs.toString());
        env.messageNL("} // end of class " + userClassName);
    }
}
