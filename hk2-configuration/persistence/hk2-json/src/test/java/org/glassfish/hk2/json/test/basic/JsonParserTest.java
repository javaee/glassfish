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
package org.glassfish.hk2.json.test.basic;

import java.net.URI;
import java.net.URL;
import java.util.List;

import org.glassfish.hk2.api.ServiceLocator;
import org.glassfish.hk2.json.api.JsonUtilities;
import org.glassfish.hk2.json.test.skillzbeans.JsonRootBean;
import org.glassfish.hk2.json.test.skillzbeans.SkillBean;
import org.glassfish.hk2.json.test.skillzbeans.SpecificSkillBean;
import org.glassfish.hk2.json.test.utilities.Utilities;
import org.glassfish.hk2.xml.api.XmlRootHandle;
import org.glassfish.hk2.xml.api.XmlService;
import org.junit.Assert;
import org.junit.Test;

/**
 * @author jwells
 *
 */
public class JsonParserTest {
    private final static String SKILLZ_FILE = "skillz.json";
    
    public final static String SKILLZ = "skillz";
    
    public final static String WEB = "web";
    public final static String DB = "database";
    
    private final static String HTML = "html";
    private final static String CSS = "css";
    
    private final static String SQL = "sql";
    
    /**
     * Tests a basic bean can be marshalled
     */
    @Test
    // @org.junit.Ignore
    public void testBasicMarshal() throws Exception {
        ServiceLocator locator = Utilities.enableLocator();
        
        XmlService jsonService = locator.getService(XmlService.class, JsonUtilities.JSON_SERVICE_NAME);
        Assert.assertNotNull(jsonService);
        
        URL url = getClass().getClassLoader().getResource(SKILLZ_FILE);
        URI uri = url.toURI();
        
        XmlRootHandle<JsonRootBean> skillBeanHandle = jsonService.unmarshal(uri, JsonRootBean.class);
        SkillBean skillzBean = skillBeanHandle.getRoot().getSkillz();
        
        List<SpecificSkillBean> specificsList = skillzBean.getWebBean();
        Assert.assertEquals(2, specificsList.size());
        
        for (int lcv = 0; lcv < specificsList.size(); lcv++) {
            SpecificSkillBean specific = specificsList.get(lcv);
            
            if (lcv == 0) {
                Assert.assertEquals(HTML, specific.getName());
                Assert.assertEquals(5, specific.getYears());
            }
            else if (lcv == 1) {
                Assert.assertEquals(CSS, specific.getName());
                Assert.assertEquals(3, specific.getYears());
            }
            else {
                Assert.fail("Unknown index " + lcv);
            }
        }
        
        SpecificSkillBean specifics[] = skillzBean.getDatabaseBean();
        Assert.assertEquals(1, specifics.length);
        
        for (int lcv = 0; lcv < specifics.length; lcv++) {
            SpecificSkillBean specific = specifics[lcv];
            
            Assert.assertEquals(SQL, specific.getName());
            Assert.assertEquals(7, specific.getYears());
        }
    }

}
