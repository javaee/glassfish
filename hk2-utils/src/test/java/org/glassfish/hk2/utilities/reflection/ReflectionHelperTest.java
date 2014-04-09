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
package org.glassfish.hk2.utilities.reflection;

import java.util.HashMap;
import java.util.List;

import junit.framework.Assert;

import org.junit.Test;

/**
 * @author jwells
 *
 */
public class ReflectionHelperTest {
    private final static String KEY1 = "key1";
    private final static String KEY2 = "key2";
    private final static String VALUE1 = "VALUE1";
    private final static String VALUE2 = "VALUE2";
    private final static String MULTI_KEY = "multiKey";
    private final static String MULTI_VALUE = "\"A,B,C=,D\"";
    private final static String NAKED_MULTI_VALUE = "A,B,C=,D";
    
    private final static String GOOD_METADATA = KEY1 + "=" + VALUE1 + "," +
        MULTI_KEY + "=" + MULTI_VALUE + "," +
        KEY2 + "=" + VALUE2 + "," +
        KEY1 + "=" + VALUE2;
    
    private final static String GOOD_METADATA_2 = "key1=value1,key2=value2";
    
    private final static String BADLY_FORMED_METADATA = KEY1 + "=" + VALUE1 + "," +
            KEY2 + "=\"No trailing quote";
    
    @Test
    public void testBasicServiceMetadata() {
        HashMap<String, List<String>> metadata = new HashMap<String, List<String>>();
        
        ReflectionHelper.parseServiceMetadataString(GOOD_METADATA, metadata);
        
        Assert.assertEquals(metadata.get(KEY1).get(0), VALUE1);
        Assert.assertEquals(metadata.get(KEY1).get(1), VALUE2);
        
        Assert.assertEquals(metadata.get(KEY2).get(0), VALUE2);
        
        Assert.assertEquals(metadata.get(MULTI_KEY).get(0), NAKED_MULTI_VALUE);   
    }
    
    @Test
    public void testBasic2ServiceMetadata() {
        HashMap<String, List<String>> metadata = new HashMap<String, List<String>>();
        
        ReflectionHelper.parseServiceMetadataString(GOOD_METADATA_2, metadata);
        
        Assert.assertEquals(metadata.get("key1").get(0), "value1");
        Assert.assertEquals(metadata.get("key2").get(0), "value2");
    }
    
    @Test
    public void testEmptyStringMetadata() {
        HashMap<String, List<String>> metadata = new HashMap<String, List<String>>();
        
        ReflectionHelper.parseServiceMetadataString("", metadata);
        
        Assert.assertTrue(metadata.isEmpty());
    }
    
    @Test
    public void testBadlyFormedMetadata() {
        HashMap<String, List<String>> metadata = new HashMap<String, List<String>>();
        
        try {
            ReflectionHelper.parseServiceMetadataString(BADLY_FORMED_METADATA, metadata);
            Assert.fail("Should have failed due to parse");
        }
        catch (IllegalStateException ise) {
            Assert.assertTrue(ise.getMessage().contains("Badly formed metadata"));
        }
    }

}
