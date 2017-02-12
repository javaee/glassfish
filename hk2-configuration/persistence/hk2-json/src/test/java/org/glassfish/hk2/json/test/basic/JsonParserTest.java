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
import org.glassfish.hk2.json.test.skillzbeans.SkillBean;
import org.glassfish.hk2.json.test.skillzbeans.SkillCategoryBean;
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
    
    private final static String SKILLZ = "skillz";
    
    private final static String WEB = "web";
    private final static String DB = "database";
    
    private final static String HTML = "html";
    private final static String CSS = "css";
    
    private final static String SQL = "sql";
    
    /**
     * Tests a basic bean can be marshalled
     */
    @Test
    @org.junit.Ignore
    public void testBasicMarshal() throws Exception {
        ServiceLocator locator = Utilities.enableLocator();
        
        XmlService jsonService = locator.getService(XmlService.class, JsonUtilities.JSON_SERVICE_NAME);
        Assert.assertNotNull(jsonService);
        
        URL url = getClass().getClassLoader().getResource(SKILLZ_FILE);
        URI uri = url.toURI();
        
        XmlRootHandle<SkillBean> skillBeanHandle = jsonService.unmarshal(uri, SkillBean.class);
        SkillBean skillBean = skillBeanHandle.getRoot();
        
        Assert.assertEquals(SKILLZ, skillBean.getName());
        
        List<SkillCategoryBean> categories = skillBean.getSkillCategories();
        Assert.assertEquals(2, categories.size());
        
        for (int lcv = 0; lcv < categories.size(); lcv++) {
            SkillCategoryBean category = categories.get(lcv);
            
            if (lcv == 0) {
                Assert.assertEquals(WEB, category.getName());
                
                SpecificSkillBean specifics[] = category.getSpecificSkills();
                Assert.assertEquals(2, specifics.length);
                
                for (int inner = 0; inner < specifics.length; inner++) {
                    SpecificSkillBean specific = specifics[inner];
                    
                    if (inner == 0) {
                        Assert.assertEquals(HTML, specific.getName());
                        Assert.assertEquals(5, specific.getYears());
                    }
                    else if (inner == 1) {
                        Assert.assertEquals(CSS, specific.getName());
                        Assert.assertEquals(3, specific.getYears());
                    }
                    else {
                        Assert.fail("Unknown index " + inner);
                    }
                }
            }
            else if (lcv == 1) {
                Assert.assertEquals(DB, category.getName());
                
                SpecificSkillBean specifics[] = category.getSpecificSkills();
                Assert.assertEquals(1, specifics.length);
                
                for (int inner = 0; inner < specifics.length; inner++) {
                    SpecificSkillBean specific = specifics[inner];
                    
                    Assert.assertEquals(SQL, specific.getName());
                    Assert.assertEquals(7, specific.getYears());
                }
                
            }
            else {
                Assert.fail("Unknown lcv " + lcv);
            }
        }
    }

}
