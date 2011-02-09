/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2010-2011 Oracle and/or its affiliates. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License.  You can
 * obtain a copy of the License at
 * https://glassfish.dev.java.net/public/CDDL+GPL_1_1.html
 * or packager/legal/LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at packager/legal/LICENSE.txt.
 *
 * GPL Classpath Exception:
 * Oracle designates this particular file as subject to the "Classpath"
 * exception as provided by Oracle in the GPL Version 2 section of the License
 * file that accompanied this code.
 *
 * Modifications:
 * If applicable, add the following below the License Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyright [year] [name of copyright owner]"
 *
 * Contributor(s):
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
package org.jvnet.hk2.junit;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.Description;
import org.junit.runner.Runner;
import org.junit.runner.notification.Failure;
import org.junit.runner.notification.RunNotifier;
import org.jvnet.hk2.component.*;

import com.sun.hk2.component.AbstractInhabitantImpl;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * JUnit runner for hk2 enabled tests. Life-cycle of the test will be managed by
 * this runner which will allow for dependency injection
 *
 * @author Jerome Dochez
 */
public class Hk2Runner extends Runner {

    final Class<?> testClass;
    final Description description;
    final Map<Description, Method> testMethods = 
          new LinkedHashMap<Description, Method>();
    final List<Method> beforeMethods =
          new ArrayList<Method>();
    final List<Method> afterMethods =
        new ArrayList<Method>();

    private Object instance;

    private final Hk2RunnerOptions options;

    
    public Hk2Runner(Class<?> testClass) {
        this.testClass = testClass;
        this.options = AbstractInhabitantImpl.getAnnotation(testClass, Hk2RunnerOptions.class, true);
        this.description = Description.createSuiteDescription(testClass);
        
        for (Method m : testClass.getDeclaredMethods()) {
            if (m.getAnnotation(Test.class)!=null) {
                Description testDescription = Description.createTestDescription(testClass, m.getName());
                description.addChild(testDescription);
                testMethods.put(testDescription, m);
            }
            
            if (m.getAnnotation(Before.class) != null) {
                beforeMethods.add(m);
            }
            
            if (m.getAnnotation(After.class) != null) {
                afterMethods.add(m);
            }
        }
    }

    @Override
    public Description getDescription() {
        return description;        
    }

    @Override
    public void run(RunNotifier notifier) {
        if (testClass.isAnnotationPresent(Ignore.class)) {
            notifier.fireTestIgnored(getDescription());
            return;
        }

        // Run the @BeforeClass methods.
        for (Method m : testClass.getMethods()) {
            int mod = m.getModifiers();
            if (Modifier.isStatic(mod) && m.getAnnotation(BeforeClass.class)!=null) {
                if (m.getAnnotation(Ignore.class)!=null) continue;
                try {
                    m.invoke(null);
                } catch (IllegalAccessException e) {
                    Failure failure = new Failure(Description.createTestDescription(testClass, m.getName()), e);
                    notifier.fireTestFailure(failure);
                } catch (InvocationTargetException e) {
                    Failure failure = new Failure(Description.createTestDescription(testClass, m.getName()), e);
                    notifier.fireTestFailure(failure);
                }
            }
        }

        
        boolean reinitPerTest = (null != options) ? options.reinitializePerTest() : false;
        try {
            creatorInit();
        } catch (ComponentException e) {
            notifier.fireTestFailure(new Failure(getDescription(),e));
            return;
        }

        Iterator<Description> iter = description.getChildren().iterator();
        while (iter.hasNext()) {
            Description testDescription = iter.next();
            
            Method m = testMethods.get(testDescription);
            if (m.isAnnotationPresent(Ignore.class)) {
                notifier.fireTestIgnored(testDescription);
                continue;
            }

            try {
                runBefores(notifier);
            } catch (Throwable e1) {
                throw new RuntimeException(e1);
            }
            
            notifier.fireTestStarted(testDescription);
            try {
                m.invoke(instance);
            } catch (IllegalAccessException e) {
                Failure failure = new Failure(testDescription, e);
                notifier.fireTestFailure(failure);
            } catch (InvocationTargetException e) {
                Failure failure = new Failure(testDescription, e.getTargetException());
                notifier.fireTestFailure(failure);
            }
            
            notifier.fireTestFinished(testDescription);

            try {
                runAfters(notifier);
            } catch (Throwable e1) {
                throw new RuntimeException(e1);
            }
            
            if (reinitPerTest && iter.hasNext()) {
                try {
                    creatorInit();
                } catch (ComponentException e) {
                    notifier.fireTestFailure(new Failure(getDescription(),e));
                    return;
                }
            }
        }

        // Run the @AfterClass methods.
        for (Method m : testClass.getMethods()) {
            int mod = m.getModifiers();
            if (Modifier.isStatic(mod) && m.getAnnotation(AfterClass.class)!=null) {
                if (m.getAnnotation(Ignore.class)!=null) continue;
                try {
                    m.invoke(null);
                } catch (IllegalAccessException e) {
                    Failure failure = new Failure(Description.createTestDescription(testClass, m.getName()), e);
                    notifier.fireTestFailure(failure);
                } catch (InvocationTargetException e) {
                    Failure failure = new Failure(Description.createTestDescription(testClass, m.getName()), e);
                    notifier.fireTestFailure(failure);
                }
            }
        }
    }

    private void runMethods(RunNotifier notifier, List<Method> methods) throws Throwable {
        for (Method m: methods) {
           if (m.getAnnotation(Ignore.class) != null) continue;
           
           try {
            m.invoke(instance);
           } catch (InvocationTargetException e) {
               Throwable cause = e.getCause();
               Failure failure = new Failure(Description.createTestDescription(testClass, m.getName()), cause);
               notifier.fireTestFailure(failure);
               throw cause;
           } 
        }
    }
    
    private void runBefores(RunNotifier notifier) throws Throwable {
        runMethods(notifier, beforeMethods);        
    }
    
    private void runAfters(RunNotifier notifier) throws Throwable {
        runMethods(notifier, afterMethods);
    }
    
    public static Habitat getHabitat() {
        return singleton.getHabitat();
    }

    public static Habitat createHabitat() {
      return singleton.createPopulatedHabitat();
    }

    @SuppressWarnings("unchecked")
    private void creatorInit() {
        singleton = new Hk2TestServices(
                null == options ? null : options.habitatFactory(),
                null == options ? null : options.inhabitantsParserFactory(),
                null == options ? true : options.enableDefaultRunLevelService(),
                null == options ? true : options.enableRunLevelConstraints());

        Habitat habitat = singleton.getHabitat();
        // so far we don't support extra meta-data on our tests.
        Creator creator = Creators.create(testClass, habitat, new MultiMap<String, String>());
        instance = creator.create(creator);
        try {
            Hk2Test.Populator populator = Hk2Test.Populator.class.cast(instance);
            populator.populate(getHabitat());
        } catch (ClassCastException e) {
            Logger.getAnonymousLogger().fine(instance + " is not a populator");
        } catch (Exception e) {
            e.printStackTrace();
            Logger.getAnonymousLogger().log(Level.SEVERE, e.getMessage(), e);
        }
        creator.initialize(instance, creator);
    }

    static Hk2TestServices singleton;
}
