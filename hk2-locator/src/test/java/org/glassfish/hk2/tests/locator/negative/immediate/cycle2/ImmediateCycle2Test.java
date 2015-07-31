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
package org.glassfish.hk2.tests.locator.negative.immediate.cycle2;

import javax.inject.Singleton;

import org.glassfish.hk2.api.ActiveDescriptor;
import org.glassfish.hk2.api.ServiceLocator;
import org.glassfish.hk2.tests.locator.utilities.LocatorHelper;
import org.glassfish.hk2.utilities.ImmediateErrorHandler;
import org.glassfish.hk2.utilities.ServiceLocatorUtilities;
import org.glassfish.hk2.utilities.binding.AbstractBinder;
import org.junit.Assert;
import org.junit.Test;

/**
 * @author jwells
 *
 */
public class ImmediateCycle2Test {
    @Test
    public void testRawTripleCycle() throws Throwable {
        ServiceLocator locator = LocatorHelper.getServiceLocator(ImmediateErrorHandlerImpl.class);
        
        ServiceLocatorUtilities.enableImmediateScope(locator);
        
        ServiceLocatorUtilities.bind(locator, new AbstractBinder() {

            @Override
            protected void configure() {
                bind(ServiceClientImpl.class).to(ServiceClient.class).in(ServiceLocatorUtilities.getImmediateAnnotation());
                bind(MessageHandlerImpl.class).to(MessageHandler.class).in(ServiceLocatorUtilities.getImmediateAnnotation());
                bind(RepositoryClientImpl.class).to(RepositoryClient.class).in(ServiceLocatorUtilities.getImmediateAnnotation());
            }
        });
        
        Assert.assertTrue(locator.getService(ImmediateErrorHandlerImpl.class).gotException(20 * 1000));
    }
    
    @Singleton
    private static class ImmediateErrorHandlerImpl implements ImmediateErrorHandler {
        private boolean gotException = false;

        /* (non-Javadoc)
         * @see org.glassfish.hk2.utilities.ImmediateErrorHandler#postConstructFailed(org.glassfish.hk2.api.ActiveDescriptor, java.lang.Throwable)
         */
        @Override
        public synchronized void postConstructFailed(ActiveDescriptor<?> immediateService,
                Throwable exception) {
            gotException = true;
            notifyAll();
        }

        /* (non-Javadoc)
         * @see org.glassfish.hk2.utilities.ImmediateErrorHandler#preDestroyFailed(org.glassfish.hk2.api.ActiveDescriptor, java.lang.Throwable)
         */
        @Override
        public void preDestroyFailed(ActiveDescriptor<?> immediateService,
                Throwable exception) {
            System.out.println("JRW(20) exception=" + exception);
            exception.printStackTrace();
            
        }
        
        private boolean gotException(long timeout) throws InterruptedException {
            synchronized (this) {
                while (!gotException && timeout > 0) {
                    long elapsedTime = System.currentTimeMillis();
                    
                    this.wait(timeout);
                    
                    elapsedTime = System.currentTimeMillis() - elapsedTime;
                    timeout -= elapsedTime;
                }
                
                return gotException;
            }
            
        }
    }

}
