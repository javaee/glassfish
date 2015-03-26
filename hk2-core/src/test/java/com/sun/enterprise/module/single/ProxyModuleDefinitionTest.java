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
