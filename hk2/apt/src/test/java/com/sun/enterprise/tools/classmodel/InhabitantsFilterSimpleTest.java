/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2007-2011 Oracle and/or its affiliates. All rights reserved.
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
package com.sun.enterprise.tools.classmodel;

import static org.junit.Assert.*;

import java.util.Arrays;
import java.util.Collections;

import org.junit.Test;

import com.sun.enterprise.tools.InhabitantsDescriptor;

/**
 * Tests for InhabitantsFilter.
 * 
 * @author Jeff Trent
 *
 */
public class InhabitantsFilterSimpleTest {

  /**
   * Test basic processing logic.
   */
  @Test
  public void process() {
    InhabitantsDescriptor inDescriptor = testDescriptor();
    inDescriptor.enableDateOutput(false);
    InhabitantsDescriptor outDescriptor = new InhabitantsDescriptor();
    outDescriptor.enableDateOutput(false);
    Filter filter = new Filter();
    InhabitantsFilter.process(inDescriptor, outDescriptor, filter);
    String expected = expected();
    String output = clean(outDescriptor.toString());
    
    assertEquals("output equality expected", expected, output);
    
    assertNotNull(inDescriptor.keySet().toString(), inDescriptor.remove("notincluded_service2"));
    assertEquals("object equality expected (keys)", inDescriptor.keySet(), outDescriptor.keySet());
    assertEquals("object equality expected", inDescriptor, outDescriptor);
  }
  
  private InhabitantsDescriptor testDescriptor() {
    InhabitantsDescriptor descriptor = new InhabitantsDescriptor();
    descriptor.setComment("test descriptor line 1");
    descriptor.appendComment("test descriptor line 2");
    descriptor.putAll("service1", 
        Collections.singleton("contract1"), Collections.singleton("annotation1"), "name1", null);
    descriptor.putAll("notincluded_service2", 
        Collections.singleton("contract1"), Collections.singleton("annotation1"), "name1", null);
    descriptor.putAll("service3", 
        Arrays.asList(new String[] {"contract1", "contract2"}), Collections.singleton("annotation2"), null, null);
    descriptor.putAll("service4", 
        null, null, null, Collections.singletonMap("a", "1"));
    return descriptor;
  }
  
  private String expected() {
    StringBuilder sb = new StringBuilder();
    sb.append("class=service1,index=contract1:name1,index=annotation1\n");
    sb.append("class=service3,index=contract1,index=contract2,index=annotation2\n");
    sb.append("class=service4,a=1\n");
    return sb.toString();
  }
  
  private String clean(String str) {
    return str.replace("\r", "");
  }

  private static class Filter extends CodeSourceFilter {
    public Filter() {
      super(null);
    }
    
    @Override
    public boolean matches(String str) {
      return !str.startsWith("notincluded_");
    }
  }
}
