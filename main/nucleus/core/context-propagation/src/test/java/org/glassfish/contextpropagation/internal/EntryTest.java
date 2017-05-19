/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2012-2017 Oracle and/or its affiliates. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License.  You can
 * obtain a copy of the License at
 * https://oss.oracle.com/licenses/CDDL+GPL-1.1
 * or LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at LICENSE.txt.
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

package org.glassfish.contextpropagation.internal;

import static org.junit.Assert.assertEquals;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.EnumSet;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

//import mockit.Deencapsulation;

import org.glassfish.contextpropagation.PropagationMode;
import org.glassfish.contextpropagation.internal.Entry.ContextType;
import org.junit.Test;

public class EntryTest {
  
  private static final boolean IS_ORIGIN = true;
  private static final boolean ALLOW_ALL_TO_READ = true;

  @Test
  public void validatel() {
    createContext("value", PropagationMode.defaultSet(), ContextType.ASCII_STRING, IS_ORIGIN, ALLOW_ALL_TO_READ).validate();
  }
  
  @Test(expected=IllegalStateException.class)
  public void validateNullValue() {
    createContext(null, PropagationMode.defaultSet(), ContextType.ASCII_STRING, IS_ORIGIN, ALLOW_ALL_TO_READ).validate();
  }

  @Test(expected=IllegalStateException.class)
  public void validateNullPropMode() {
    createContext("value", null, ContextType.ASCII_STRING, IS_ORIGIN, ALLOW_ALL_TO_READ).validate();
  }

  @Test(expected=IllegalStateException.class)
  public void validateNullContextType() {
    createContext("value", PropagationMode.defaultSet(), null, IS_ORIGIN, ALLOW_ALL_TO_READ).validate();
  }

  @Test(expected=IllegalStateException.class)
  public void validateNullISOriginator() {
    createContext("value", PropagationMode.defaultSet(), ContextType.ASCII_STRING, (Boolean) null, ALLOW_ALL_TO_READ).validate();
  }

  @Test(expected=IllegalStateException.class)
  public void validateNullAllowAllToRead() {
    createContext("value", PropagationMode.defaultSet(), ContextType.ASCII_STRING, IS_ORIGIN, null).validate();
  }


  private Entry createContext(String value,
      EnumSet<PropagationMode> propModes, ContextType type,
      Boolean isOrigin, Boolean allowAllToRead) {
    Entry entry = new Entry(value, propModes, type);
    return entry.init(isOrigin, allowAllToRead);
  }

  @Test
  public void testToContextTypeFromNumberClass() {
    assertEquals(ContextType.ATOMICINTEGER, ContextType.fromNumberClass(AtomicInteger.class));
    assertEquals(ContextType.ATOMICLONG, ContextType.fromNumberClass(AtomicLong.class));
    assertEquals(ContextType.BIGDECIMAL, ContextType.fromNumberClass(BigDecimal.class));
    assertEquals(ContextType.BIGINTEGER, ContextType.fromNumberClass(BigInteger.class));
    assertEquals(ContextType.BYTE, ContextType.fromNumberClass(Byte.class));
    assertEquals(ContextType.DOUBLE, ContextType.fromNumberClass(Double.class));
    assertEquals(ContextType.FLOAT, ContextType.fromNumberClass(Float.class));
    assertEquals(ContextType.INT, ContextType.fromNumberClass(Integer.class));
    assertEquals(ContextType.LONG, ContextType.fromNumberClass(Long.class));
    assertEquals(ContextType.SHORT, ContextType.fromNumberClass(Short.class));
  }
  
//  @Test
//  public void testToContextTypeOrdinal() {
//    ContextType[] byOrdinal = Deencapsulation.getField(ContextType.class, "byOrdinal");
//    for (int i = 0; i < byOrdinal.length; i++) {
//      assertEquals(i, ContextType.fromOrdinal(i).ordinal());
//    }
//    assertEquals(ContextType.values().length, byOrdinal.length);
//  }

}
