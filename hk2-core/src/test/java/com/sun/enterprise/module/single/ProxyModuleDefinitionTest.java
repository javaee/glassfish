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

package com.sun.enterprise.module.single;

import static org.junit.Assert.*;

import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.jar.Manifest;
import java.util.logging.Logger;

import org.junit.Test;

import com.sun.enterprise.module.ModuleDependency;
import com.sun.enterprise.module.ModuleMetadata;

/**
 * Tests for {@link ProxyModuleDefinition}
 * 
 * @author Jeff Trent
 */
public class ProxyModuleDefinitionTest {

  @Test
  public void testGetLocations() throws Exception {
    ClassLoader loader = ProxyModuleDefinitionTest.class.getClassLoader();
    ProxyModuleDefinition pmd = new ProxyModuleDefinition(loader);
    assertNotNull(pmd.getLocations());
    assertNotSame(pmd.getLocations(), pmd.getLocations());
    List<URI> coll = Arrays.asList(pmd.getLocations());
    System.out.println(coll);
  }
  
  @Test
  public void testGetLocationsIsNotHeapIntensive() throws Exception {
    ClassLoader loader = ProxyModuleDefinitionTest.class.getClassLoader();
    ArrayList<ProxyModuleDefinition> list = new ArrayList<ProxyModuleDefinition>();

//    long totalMemory = Runtime.getRuntime().totalMemory();

    System.gc();
    System.gc();
    Thread.sleep(100);
    
    long freeMemory0 = getMemoryUse();
    ProxyModuleDefinition pmd = new ProxyModuleDefinition(loader);
    long freeMemory1 = getMemoryUse();
    
    for (int i = 0; i < 100; i++) {
      pmd = new ProxyModuleDefinition(loader);
      list.add(pmd);
    }
    
    System.gc();
    System.gc();
    Thread.sleep(100);
    
    long freeMemory2 = getMemoryUse();
    
    Logger.getAnonymousLogger().fine("First Object: " + (freeMemory1 - freeMemory0));

    long avgHeapPerObject = (freeMemory2-freeMemory1)/100;
    Logger.getAnonymousLogger().fine("100 Objects: " + (freeMemory2 - freeMemory1)
        + ", or " + avgHeapPerObject + " per object");
    
    assertTrue("expect less heap consumed: " + avgHeapPerObject, avgHeapPerObject < 8192);
  }
  
  @Test
  public void testGetManifest() throws Exception {
    ClassLoader loader = ProxyModuleDefinitionTest.class.getClassLoader();
    ProxyModuleDefinition pmd = new ProxyModuleDefinition(loader);
    Manifest mf = pmd.getManifest();
    assertNotNull(mf);
    assertNotSame(mf, pmd.getManifest());
    assertNotNull(mf.getEntries());
//    assertFalse(mf.getEntries().isEmpty());
  }
  
  @Test
  public void testGetMetadata() throws Exception {
    ClassLoader loader = ProxyModuleDefinitionTest.class.getClassLoader();
    ProxyModuleDefinition pmd = new ProxyModuleDefinition(loader);
    ModuleMetadata md = pmd.getMetadata();
    assertNotNull(md);
    assertNotSame(md, pmd.getMetadata());
    assertNotNull(md.getEntries());
    assertFalse(md.getEntries().iterator().hasNext());
  }
  
  @Test
  public void testGetDependencies() throws Exception {
    ClassLoader loader = ProxyModuleDefinitionTest.class.getClassLoader();
    ProxyModuleDefinition pmd = new ProxyModuleDefinition(loader);
    ModuleDependency md[] = pmd.getDependencies();
    assertNotSame(md, pmd.getDependencies());
  }
  
  private static long getMemoryUse(){
    long totalMemory = Runtime.getRuntime().totalMemory();
    long freeMemory = Runtime.getRuntime().freeMemory();
    return (totalMemory - freeMemory);
  }


}
