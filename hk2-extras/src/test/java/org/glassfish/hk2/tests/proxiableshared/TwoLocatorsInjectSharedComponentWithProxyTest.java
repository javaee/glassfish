/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2015 Oracle and/or its affiliates. All rights reserved.
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
package org.glassfish.hk2.tests.proxiableshared;

import org.glassfish.hk2.api.ActiveDescriptor;
import org.glassfish.hk2.api.ServiceLocator;
import org.glassfish.hk2.extras.ExtrasUtilities;
import org.glassfish.hk2.tests.extras.internal.Utilities;
import org.glassfish.hk2.utilities.BuilderHelper;
import org.glassfish.hk2.utilities.ServiceLocatorUtilities;
import org.junit.Ignore;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;


/**
 * Ensure that two distinct service locators could be utilized to inject
 * a component that is managed by a 3rd-party component manager. The 3rd-party component behaves as a global
 * singleton. It is required that the component is injected with proxy instances.
 *
 * @author Jakub Podlesak (jakub.podlesak at oracle.com)
 */
public class TwoLocatorsInjectSharedComponentWithProxyTest {

    private final static String TEST_NAME = TwoLocatorsInjectSharedComponentWithProxyTest.class.getSimpleName();
    
    private static ServiceLocator newApplicationLocator(String name) {
        ServiceLocator retVal = Utilities.getCleanLocator(name,
                GlobalReqContext.class,
                GlobalComponent.class);
        
        ExtrasUtilities.enableOperations(retVal);
        
        return retVal;
    }

    private static ServiceLocator newComponentLocator(String name, ServiceLocator appLocator) {
        return Utilities.getCleanLocator(name, appLocator, ReqContext.class, ReqData.class);
    }
    
    private static <T> void tellAppLocatorAboutComponentService(ServiceLocator appLocator,
            ServiceLocator childLocator,
            ActiveDescriptor<T> requestScopedDescriptor) {
        ActiveDescriptor<T> componentService = new ForeignActiveDescriptor<T>(childLocator, requestScopedDescriptor);
        
        ServiceLocatorUtilities.addOneDescriptor(appLocator, componentService, false);
    }
    
    private static ActiveDescriptor<?> getReqDataDescriptor(ServiceLocator componentLocator) {
        return componentLocator.getBestDescriptor(BuilderHelper.createContractFilter(ReqData.class.getName()));
    }

    /**
     * Tests that the proxy application works with a single ServiceLocator
     */
    @Test // @Ignore
    public void testSingleAppWorksFine() {


        final ServiceLocator appLocator = newApplicationLocator(TEST_NAME + "_SingleApp");
        final ServiceLocator componentLocator = newComponentLocator(TEST_NAME + "_SingleComponent", appLocator);
        tellAppLocatorAboutComponentService(appLocator, componentLocator, getReqDataDescriptor(componentLocator));

        final ReqContext request = componentLocator.getService(ReqContext.class);
        assertThat(request, is(notNullValue()));

        // req one:
        request.startRequest();
        ReqData reqData = componentLocator.getService(ReqData.class);
        assertThat(reqData, is(notNullValue()));
        reqData.setRequestName("one");

        final GlobalComponent globalComponentOne = componentLocator.getService(GlobalComponent.class);
        assertThat(globalComponentOne.getRequestName(), is(equalTo("one")));
        request.stopRequest();

        // req two:
        request.startRequest();
        reqData = componentLocator.getService(ReqData.class);
        assertThat(reqData, is(notNullValue()));
        reqData.setRequestName("two");

        final GlobalComponent globalComponentTwo = componentLocator.getService(GlobalComponent.class);
        assertThat(globalComponentOne.getRequestName(), is(equalTo("two")));
        assertThat(globalComponentTwo, is(equalTo(globalComponentOne)));
        request.stopRequest();
    }

    /**
     * Tests the same app as above but working on two different ServiceLocators
     * at once.  The two locators should not interfere with each other
     */
    @Test
    @Ignore
    public void testTwoAppsWorkFine() {
        // create the application locator
        final ServiceLocator appLocator = newApplicationLocator(TEST_NAME + "_MultiApp");
        
        // create two "apps"
        final ServiceLocator adamAppLocator = newComponentLocator(TEST_NAME + "_AdamApp", appLocator);
        final ServiceLocator evaAppLocator = newComponentLocator(TEST_NAME + "_EvaApp", appLocator);
        
        // Ensure the global knows about the request services
        tellAppLocatorAboutComponentService(appLocator, adamAppLocator, getReqDataDescriptor(adamAppLocator));
        tellAppLocatorAboutComponentService(appLocator, evaAppLocator, getReqDataDescriptor(evaAppLocator));

        // get app context from both
        final ReqContext adamRequest = adamAppLocator.getService(ReqContext.class);
        assertThat(adamRequest, is(notNullValue()));

        final ReqContext evaRequest = evaAppLocator.getService(ReqContext.class);
        assertThat(evaRequest, is(notNullValue()));

        // req Adam/one:
        adamRequest.startRequest();
        ReqData reqData = adamAppLocator.getService(ReqData.class);
        assertThat(reqData, is(notNullValue()));
        reqData.setRequestName("adam/one");
        
        final GlobalComponent globalComponentOne = adamAppLocator.getService(GlobalComponent.class);
        assertThat(globalComponentOne.getRequestName(), is(equalTo("adam/one")));
        adamRequest.stopRequest();

        // req Adam/two:
        adamRequest.startRequest();
        reqData = adamAppLocator.getService(ReqData.class);
        assertThat(reqData, is(notNullValue()));
        reqData.setRequestName("adam/two");

        final GlobalComponent globalComponentTwo = adamAppLocator.getService(GlobalComponent.class);
        assertThat(globalComponentTwo.getRequestName(), is(equalTo("adam/two")));
        assertThat(globalComponentTwo, is(equalTo(globalComponentOne)));
        // uncomment this to see Adam request data "leaking" into the global component for Eva
        adamRequest.stopRequest();

        // req Eva/one:
        evaRequest.startRequest();
        reqData = evaAppLocator.getService(ReqData.class);
        assertThat(reqData, is(notNullValue()));
        reqData.setRequestName("eva/one");

        final GlobalComponent globalComponentEvaOne = evaAppLocator.getService(GlobalComponent.class);
        assertThat(globalComponentEvaOne.getRequestName(), is(equalTo("eva/one")));
        assertThat(globalComponentEvaOne, is(equalTo(globalComponentTwo)));
        evaRequest.stopRequest();
    }
}