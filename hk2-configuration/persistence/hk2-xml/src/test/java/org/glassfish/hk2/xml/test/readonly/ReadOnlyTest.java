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
package org.glassfish.hk2.xml.test.readonly;

import java.net.URL;

import org.glassfish.hk2.api.ServiceLocator;
import org.glassfish.hk2.xml.api.XmlRootHandle;
import org.glassfish.hk2.xml.api.XmlService;
import org.glassfish.hk2.xml.test.utilities.Utilities;
import org.junit.Assert;
import org.junit.Test;

/**
 * @author jwells
 *
 */
public class ReadOnlyTest {
    private final static String LIBRARY1_FILE = "library1.xml";
    
    private final static String LIBRARY_NAME = "Sesame Street Library";
    
    private final static String GEEK_SUBLIME_ISBN = "978-1-55597-685-9";
    private final static String GEEK_SUBLIME_NAME = "Geek Sublime";
    
    private final static String ESL_ISBN = "1-592-40087-6";
    private final static String ESL_NAME = "Eats, Shoots and Leaves";
    
    private final static String ALIEN_NAME = "Alien";
    private final static String HANNA_NAME = "Hannah and her Sisters";
    private final static String WOODS_NAME = "Into the Woods";
    
    private final static String ADDRESS_LINE_1 = "555 Sesame Street";
    private final static String ADDRESS_LINE_2 = "CO Grover";
    private final static String TOWN = "New York";
    private final static String STATE = "NY";
    private final static int ZIP = 10128;
    
    /**
     * Tests that we can add a read-only bean
     * 
     * @throws Exception
     */
    @Test // @org.junit.Ignore
    public void testReadOnlyBeans() throws Exception {
        ServiceLocator locator = Utilities.createLocator();
        XmlService xmlService = locator.getService(XmlService.class);
        
        URL url = getClass().getClassLoader().getResource(LIBRARY1_FILE);
        
        XmlRootHandle<LibraryBean> rootHandle = xmlService.unmarshall(url.toURI(), LibraryBean.class);
        LibraryBean library = rootHandle.getRoot();
        
        Assert.assertEquals(LIBRARY_NAME, library.getName());
        
        {
            BookBean geekSublimeBook = library.getBooks().get(0);
            Assert.assertEquals(GEEK_SUBLIME_ISBN, geekSublimeBook.getISBN());
            Assert.assertEquals(GEEK_SUBLIME_NAME, geekSublimeBook.getName());
        }
        
        {
            BookBean eslBook = library.getBooks().get(1);
            Assert.assertEquals(ESL_ISBN, eslBook.getISBN());
            Assert.assertEquals(ESL_NAME, eslBook.getName());
        }
        
        Assert.assertEquals(ALIEN_NAME, library.getMovies().get(0).getName());
        Assert.assertEquals(HANNA_NAME, library.getMovies().get(1).getName());
        Assert.assertEquals(WOODS_NAME, library.getMovies().get(2).getName());
        
        AddressBean address = library.getAddress();
        Assert.assertNotNull(address);
        
        Assert.assertEquals(ADDRESS_LINE_1, address.getStreetAddress1());
        Assert.assertEquals(ADDRESS_LINE_2, address.getStreetAddress2());
        Assert.assertEquals(TOWN, address.getTown());
        Assert.assertEquals(STATE, address.getState());
        Assert.assertEquals(ZIP, address.getZipCode());
    }

}
