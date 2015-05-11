/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2013 Oracle and/or its affiliates. All rights reserved.
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
package org.jvnet.testing.hk2testng;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.glassfish.hk2.api.ServiceLocator;
import org.glassfish.hk2.api.ServiceLocatorFactory;
import org.glassfish.hk2.extras.ExtrasUtilities;
import org.glassfish.hk2.utilities.Binder;
import org.glassfish.hk2.utilities.ServiceLocatorUtilities;
import org.testng.IConfigurable;
import org.testng.IConfigureCallBack;
import org.testng.IExecutionListener;
import org.testng.IHookCallBack;
import org.testng.IHookable;
import org.testng.ITestResult;

/**
 *
 * @author saden
 */
public class HK2TestListenerAdapter implements IExecutionListener, IHookable, IConfigurable {

    private static final Map<String, ServiceLocator> serviceLocators = new ConcurrentHashMap<String, ServiceLocator>();
    private static final Map<Class<?>, Object> testClasses = new ConcurrentHashMap<Class<?>, Object>();
    private static final Map<Class<?>, Binder> binderClasses = new ConcurrentHashMap<Class<?>, Binder>();

    @Override
    public void onExecutionStart() {
    }

    @Override
    public void onExecutionFinish() {
        for (Map.Entry<String, ServiceLocator> entry : serviceLocators.entrySet()) {
            ServiceLocatorFactory.getInstance().destroy(entry.getValue());
        }

        serviceLocators.clear();
        testClasses.clear();
        binderClasses.clear();
    }

    @Override
    public void run(IHookCallBack callBack, ITestResult testResult) {
        try {
            injectTestInstance(testResult);
            callBack.runTestMethod(testResult);
        } catch (InstantiationException e) {
            testResult.setThrowable(e);
        } catch (IllegalAccessException e) {
            testResult.setThrowable(e);
        }
    }

    @Override
    public void run(IConfigureCallBack callBack, ITestResult testResult) {
        try {
            injectTestInstance(testResult);
            callBack.runConfigurationMethod(testResult);
        } catch (InstantiationException e) {
            testResult.setThrowable(e);
        } catch (IllegalAccessException e) {
            testResult.setThrowable(e);
        }
    }

    private static void initializeServiceLocator(ServiceLocator locator, HK2 hk2) {
      if (hk2.enableImmediate()) {
        ServiceLocatorUtilities.enableImmediateScope(locator);
      }

      if (hk2.enablePerThread()) {
        ServiceLocatorUtilities.enablePerThreadScope(locator);
      }

        if (hk2.enableInheritableThread()) {
        ServiceLocatorUtilities.enableInheritableThreadScope(locator);
        }

      if (hk2.enableLookupExceptions()) {
        ServiceLocatorUtilities.enableLookupExceptions(locator);
      }

      if (hk2.enableEvents()) {
          ExtrasUtilities.enableTopicDistribution(locator);
        }

    }

    private void injectTestInstance(ITestResult testResult) throws InstantiationException, IllegalAccessException {
        ServiceLocator locator = null;
        Object testInstance = testResult.getMethod().getInstance();

        if (testInstance != null) {
            HK2 hk2 = testInstance.getClass().getAnnotation(HK2.class);

            if (hk2 != null) {
                String locatorName = hk2.value();
                if ("hk2-testng-locator".equals(locatorName)) {
                    locatorName = locatorName + "." + testInstance.getClass().getSimpleName();
                }

                ServiceLocator existingLocator = serviceLocators.get(locatorName);

                if (!testClasses.containsKey(testInstance.getClass())) {
                    Class<? extends Binder>[] hk2BinderClasses = hk2.binders();

                    if (hk2.populate()) {
                        if (existingLocator == null) {
                            locator = ServiceLocatorUtilities.createAndPopulateServiceLocator(locatorName);
                            initializeServiceLocator(locator, hk2);

                            serviceLocators.put(locator.getName(), locator);
                        }
                        else {
                            locator = existingLocator;
                        }
                    }

                    if (hk2BinderClasses.length > 0) {
                        Binder[] binders = new Binder[hk2BinderClasses.length];
                        int index = 0;
                        for (Class<? extends Binder> binderClass : hk2BinderClasses) {
                            Binder binder = binderClasses.get(binderClass);

                            if (binder == null) {
                                binder = binderClass.newInstance();
                                binderClasses.put(binderClass, binder);
                            }

                            binders[index++] = binder;
                        }

                        if (locator == null) {
                            if (existingLocator == null) {
                                locator = ServiceLocatorUtilities.bind(locatorName, binders);
                                initializeServiceLocator(locator, hk2);

                                serviceLocators.put(locator.getName(), locator);
                            }
                            else {
                                locator = existingLocator;
                                ServiceLocatorUtilities.bind(locator, binders);
                            }
                        } else {
                            ServiceLocatorUtilities.bind(locator, binders);
                        }
                    }

                    if (locator != null) {
                        locator.inject(testInstance);
                    }

                    testClasses.put(testInstance.getClass(), testInstance);
                }
            }
        }
    }

}
