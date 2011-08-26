/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2011 Oracle and/or its affiliates. All rights reserved.
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
package org.jvnet.hk2.component;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.easymock.EasyMock;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.jvnet.hk2.test.contracts.DummyContract;
import org.jvnet.hk2.test.contracts.Simple;
import org.jvnet.hk2.test.impl.OneSimple;

public class ContractLocatorImplTest {
    static LogHandler logHandler = new LogHandler();
    static Level prevLogLevel;

    @BeforeClass
    public static void setUp() {
        Logger logger = Logger.getLogger(ContractLocatorImpl.class.getName());
        prevLogLevel = logger.getLevel();
        logger.setLevel(Level.FINEST);
        logger.addHandler(logHandler);
    }

    @AfterClass
    public static void tearDown() {
        Logger logger = Logger.getLogger(ContractLocatorImpl.class.getName());
        logger.removeHandler(logHandler);
        logger.setLevel(prevLogLevel);
    }

    @After
    public void resetLogger() {
        logHandler.clear();
    }

    @Test
    public void resolution_byClassAndByContract() {
        SimpleServiceLocator mock = EasyMock.createNiceMock(SimpleServiceLocator.class);
        Simple simple = EasyMock.createMock(Simple.class);
        Inhabitant<Simple> provider = EasyMock.createMock(Inhabitant.class);
        EasyMock.expect(provider.get()).andReturn(simple).atLeastOnce();
        EasyMock.expect(provider.metadata()).andReturn(new MultiMap<String, String>()).atLeastOnce();
        EasyMock.expect(mock.<Simple>getProvider(Simple.class, null)).andReturn(provider).atLeastOnce();
        OneSimple oneSimple = new OneSimple();
        Inhabitant<Simple> oneProvider = EasyMock.createMock(Inhabitant.class);
        EasyMock.expect(oneProvider.get()).andReturn(oneSimple).atLeastOnce();

        EasyMock.expect(mock.<Simple>getProvider(Simple.class, "one")).andReturn(oneProvider).atLeastOnce();
        EasyMock.replay(mock, provider, oneProvider);

        ContractLocatorImpl<Simple> locator = new ContractLocatorImpl<Simple>(mock, Simple.class, true);
        runContractKind(mock, simple, oneSimple, locator);
    }

    private void runContractKind(SimpleServiceLocator mock, Simple simple,
                                 OneSimple oneSimple, ContractLocatorImpl<Simple> locator) {
        assertSame(simple, locator.get());

        locator.named("one");
        assertSame(oneSimple, locator.get());

        EasyMock.verify(mock);

        logHandler.assertIsEmpty();
    }

    @Test
    public void resolution_byClassAndByType() {
        SimpleServiceLocator mock = EasyMock.createNiceMock(SimpleServiceLocator.class);
        Simple simple = EasyMock.createMock(Simple.class);
        Inhabitant<Simple> provider = EasyMock.createMock(Inhabitant.class);
        EasyMock.expect(provider.metadata()).andReturn(new MultiMap<String, String>()).atLeastOnce();
        EasyMock.expect(provider.get()).andReturn(simple).times(1);
        EasyMock.expect(mock.<Simple>getProvider(Simple.class, null)).andReturn(provider).times(1);
        EasyMock.replay(mock, provider);

        ContractLocatorImpl<Simple> locator = new ContractLocatorImpl<Simple>(mock, Simple.class, false);
        runTypeKind(mock, simple, locator);
    }

    private void runTypeKind(SimpleServiceLocator mock, Simple simple,
                             ContractLocatorImpl<Simple> locator) {
        assertSame(simple, locator.get());

        logHandler.assertIsEmpty();

//        locator.named(Simple.class.getName());
//        assertSame(simple, locator.get());

//        logHandler.assertMessage(0, Level.WARNING, "name and scope are currently only appropriate for byContract usage");

        EasyMock.verify(mock);
    }

    @Test
    public void resolution_byClassNameAndByContract() {
        SimpleServiceLocator mock = EasyMock.createNiceMock(SimpleServiceLocator.class);
        Simple simple = EasyMock.createMock(Simple.class);
        Inhabitant<Simple> provider = EasyMock.createMock(Inhabitant.class);
        EasyMock.expect(provider.get()).andReturn(simple).atLeastOnce();
        EasyMock.expect(provider.metadata()).andReturn(new MultiMap<String, String>()).atLeastOnce();
        EasyMock.expect(mock.<Simple>getProvider(Simple.class.getName(), null)).andReturn(provider).atLeastOnce();
        OneSimple oneSimple = new OneSimple();
        Inhabitant<Simple> oneProvider = EasyMock.createMock(Inhabitant.class);
        EasyMock.expect(oneProvider.get()).andReturn(oneSimple).atLeastOnce();
        EasyMock.expect(mock.<Simple>getProvider(Simple.class.getName(), "one")).andReturn(oneProvider).atLeastOnce();
        EasyMock.replay(mock, provider, oneProvider);

        ContractLocatorImpl<Simple> locator = new ContractLocatorImpl<Simple>(mock, Simple.class.getName(), true);
        runContractKind(mock, simple, oneSimple, locator);
    }

    @Test
    public void resolution_byClassNameAndByType() {
        SimpleServiceLocator mock = EasyMock.createNiceMock(SimpleServiceLocator.class);
        Simple simple = EasyMock.createMock(Simple.class);
        Inhabitant<Simple> provider = EasyMock.createMock(Inhabitant.class);
        EasyMock.expect(provider.get()).andReturn(simple).atLeastOnce();
        EasyMock.expect(provider.metadata()).andReturn(new MultiMap<String, String>()).atLeastOnce();
        EasyMock.expect(mock.<Simple>getProvider(Simple.class.getName(), null)).andReturn(provider).times(1);
        EasyMock.replay(mock,provider);

        ContractLocatorImpl<Simple> locator = new ContractLocatorImpl<Simple>(mock, Simple.class.getName(), false);
        runTypeKind(mock, simple, locator);
    }
    
    /**
     * Ensure that warning messages are not issued when there is no contracts found in habitat.
     * Motivated by http://java.net/jira/browse/HK2-22
     */
    @Test
    public void serviceImplNotFound() {
        Habitat h = new Habitat();
        ContractLocatorImpl<DummyContract> locator = new ContractLocatorImpl<DummyContract>(h, DummyContract.class, true);
        DummyContract service = locator.get();
        assertNull(service);
        assertEquals(logHandler.publishedRecords.toString(),
                0, logHandler.publishedRecords.size());
        
        boolean isContractExt = h.isContractExt(DummyContract.class);
        assertTrue("a weaker test - see javadoc", isContractExt);
        assertEquals(logHandler.publishedRecords.toString(),
                0, logHandler.publishedRecords.size());
    
        locator = new ContractLocatorImpl<DummyContract>(h, DummyContract.class, isContractExt);
        locator.named("whatever");
        service = locator.get();
        assertEquals(logHandler.publishedRecords.toString(),
                0, logHandler.publishedRecords.size());
        
        boolean isContract = h.isContract(DummyContract.class);
        assertFalse("according to javadoc, even though DummyContract is a Contract it does not exist in habitat", isContract);
        assertEquals(logHandler.publishedRecords.toString(),
                0, logHandler.publishedRecords.size());

        locator = new ContractLocatorImpl<DummyContract>(h, DummyContract.class, isContract);
        locator.named("whatever");
        service = locator.get();
        assertEquals(logHandler.publishedRecords.toString(),
                1, logHandler.publishedRecords.size());
    }
    
}
