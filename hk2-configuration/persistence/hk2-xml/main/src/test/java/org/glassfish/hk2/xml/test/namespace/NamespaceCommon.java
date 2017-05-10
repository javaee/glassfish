/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2017 Oracle and/or its affiliates. All rights reserved.
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
package org.glassfish.hk2.xml.test.namespace;

import java.net.URI;
import java.util.Map;

import javax.xml.namespace.QName;

import org.glassfish.hk2.api.ServiceLocator;
import org.glassfish.hk2.xml.api.XmlRootHandle;
import org.glassfish.hk2.xml.api.XmlService;
import org.junit.Assert;

/**
 * 
 * @author jwells
 */
public class NamespaceCommon {
    public final static String XTRA_ATTRIBUTES_FILE = "xmlns/xtra-attributes.xml";
    
    private final static String ACME_NS_URI = "http://www.acme.org/jmxoverjms";
    private final static String BOX_NS_URI = "http://www.boxco.com/boxes";
    private final static String HLMS_NS_URI = "http://www.holmes.com/ac";
    
    private final static String ATTA_LOCAL = "attA";
    private final static String ATTA_PREFIX = "xos";
    private final static String ATTB_LOCAL = "attB";
    private final static String ATTB_PREFIX = "box";
    private final static String ATTC_LOCAL = "attC";
    private final static String ATTC_PREFIX = "sox";
    private final static String ATTD_LOCAL = "attD";
    
    private final static QName JOJ_ATTA_QNAME = new QName(ACME_NS_URI, ATTA_LOCAL, ATTA_PREFIX);
    private final static QName BOX_ATTB_QNAME = new QName(BOX_NS_URI, ATTB_LOCAL, ATTB_PREFIX);
    private final static QName DFL_ATTB_QNAME = new QName(ATTB_LOCAL);
    private final static QName SOX_ATTC_QNAME = new QName(HLMS_NS_URI, ATTC_LOCAL, ATTC_PREFIX);
    private final static QName DFL_ATTD_QNAME = new QName(ATTD_LOCAL);
    
    private final static String FOO = "foo";
    private final static String BAZ = "baz";
    private final static String GRAX = "grax";
    private final static String BAR = "bar";
    private final static String GMB = "gumby";
    private final static String FIG = "figaro";
    
    public static void testExtraAttributes(ServiceLocator locator, URI uri) {
        XmlService xmlService = locator.getService(XmlService.class);
        
        XmlRootHandle<XtraAttributesRootBean> rootHandle = xmlService.unmarshal(uri, XtraAttributesRootBean.class);
        XtraAttributesRootBean root = rootHandle.getRoot();
        FooBean fooBean = root.getFoo();
        
        Assert.assertEquals(FOO, fooBean.getAttA());
        
        Map<QName, String> others = fooBean.getOtherAttributes();
        Assert.assertEquals(5, others.size());
        
        boolean foundBaz = false;
        boolean foundGrax = false;
        boolean foundBar = false;
        boolean foundGumby = false;
        boolean foundFigaro = false;
        for (Map.Entry<QName, String> entry : others.entrySet()) {
            QName qEntry = entry.getKey();
            String value = entry.getValue();
            
            if (qEntry.equals(JOJ_ATTA_QNAME)) {
                Assert.assertEquals(BAZ, value);
                foundBaz = true;
            }
            else if (qEntry.equals(BOX_ATTB_QNAME)) {
                Assert.assertEquals(GRAX, value);
                foundGrax = true;
            }
            else if (qEntry.equals(DFL_ATTB_QNAME)) {
                Assert.assertEquals(BAR, value);
                foundBar = true;
            }
            else if (qEntry.equals(SOX_ATTC_QNAME)) {
                Assert.assertEquals(GMB, value);
                foundGumby = true;
            }
            else if (qEntry.equals(DFL_ATTD_QNAME)) {
                Assert.assertEquals(FIG, value);
                foundFigaro = true;
            }
            else {
                Assert.fail("Unknown QName=" + qEntry + " and value " + value);
            }
        }
        
        Assert.assertTrue(foundBaz);
        Assert.assertTrue(foundGrax);
        Assert.assertTrue(foundBar);
        Assert.assertTrue(foundGumby);
        Assert.assertTrue(foundFigaro);
    }

}
