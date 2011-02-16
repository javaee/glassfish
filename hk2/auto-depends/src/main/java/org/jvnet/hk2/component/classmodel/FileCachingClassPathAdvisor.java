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
package org.jvnet.hk2.component.classmodel;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.util.Properties;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Uses a properties file to map all known URIs to last modified date,size, and whether it contains
 * significant class-model artifacts that contributes to habitat production.
 * 
 * @author Jeff Trent
 */
public class FileCachingClassPathAdvisor implements ClassPathAdvisor {

  private final Logger logger = Logger.getLogger(FileCachingClassPathAdvisor.class.getName());
  
  public static final String TAG_SYS_PROP = "classpathadvisor.cache.file";

  public static final String HK2_CLASS_PATH_ADVISOR_CACHE_FILE = ".hk2ClassPathAdvisorCache.properties";
  
  private final String defaultCacheFileName = 
    new File(System.getProperty("java.io.tmpdir"), HK2_CLASS_PATH_ADVISOR_CACHE_FILE).toString();
  private final String cacheFileName;
  private final File cacheFile;

  private Properties cache;
  
  
  public FileCachingClassPathAdvisor() {
    this.cacheFileName = System.getProperty(TAG_SYS_PROP, defaultCacheFileName);
    this.cacheFile = new File(cacheFileName);
  }
  
  public FileCachingClassPathAdvisor(String cacheFileName) {
    this.cacheFileName = cacheFileName;
    this.cacheFile = new File(cacheFileName);
  }
  
  @Override
  public void starting(ClassPath inhabitantsClassPath) {
    logger.log(Level.INFO, "reading cache {0}", cacheFileName);
    
    cache = new Properties();

    if (cacheFile.exists()) {
      try {
        InputStream is = new BufferedInputStream(new FileInputStream(cacheFile));
        cache.load(is);
        is.close();
      } catch (IOException e) {
        throw new RuntimeException(e);
      }
    }
  }

  @Override
  public void finishing(Set<URI> significant, Set<URI> insignificant) {
    logger.log(Level.INFO, "Non-Contributing URIs are: {0}", insignificant);
    logger.log(Level.INFO, "Contributing URIs are: {0}", significant);
    
    boolean dirty = false;

    for (URI uri : insignificant) {
      // we don't attempt to cache directories or missing files
      File file = new File(uri);
      if (file.exists() && !file.isDirectory()) {
        String newVal = entryFor(uri, false);
        String key = uri.toString().toLowerCase();
        String oldVal = (String) cache.put(key, newVal);
        dirty |= !newVal.equals(oldVal);
        logger.log(Level.FINE, "putting: {0} with {1}; oldVal was {2}",
            new Object[] {key, newVal, oldVal});
      }
    }

    for (URI uri : significant) {
      // we don't attempt to cache directories or missing files
      File file = new File(uri);
      if (file.exists() && !file.isDirectory()) {
        String newVal = entryFor(uri, true);
        String key = uri.toString().toLowerCase();
        String oldVal = (String) cache.put(key, newVal);
        dirty |= !newVal.equals(oldVal);
        logger.log(Level.FINE, "putting: {0} with {1}; oldVal was {2}",
            new Object[] {key, newVal, oldVal});
      }
    }

    if (dirty) {
      logger.log(Level.INFO, "writing cache {0}", cacheFile);
      
      try {
        OutputStream os = new BufferedOutputStream(new FileOutputStream(cacheFile));
        cache.store(os, null);
        os.close();
      } catch (IOException e) {
        throw new RuntimeException(e);
      }
    }
  }

  private String entryFor(URI uri, boolean significant) {
    StringBuilder sb = new StringBuilder();
    sb.append(significant ? "1" : "0").append(",");
    File file = new File(uri);
    if (file.exists()) {
      sb.append(file.lastModified()).append(",").append(file.length());
    } else {
      sb.append("-1").append(",").append("-1");
    }
    return sb.toString();
  }

  @Override
  public boolean accept(File file) {
    assert(null != cache);
    
    URI uri = file.toURI();
    String cachedVal = cache.getProperty(uri.toString().toLowerCase());
    if (null != cachedVal) {
      // if cache does have it, we need to check file stats
      String compareSig = cachedVal.substring(0, 1);
      boolean significant = compareSig.equals("1");
      String compareVal = entryFor(uri, significant);
      if (compareVal.equals(cachedVal)) {
        // we can trust the cache entry
        logger.log(Level.FINE, "cache for {0} indicates significance val {1}", new Object[] {uri, compareSig});
        return significant;
      } else {
        logger.log(Level.FINE, "cache is dirty for {0}", uri);
        return true;
      }
    } else {
      // if cache doesn't have it we need to accept it because we are not sure if it's significant
      logger.log(Level.FINE, "cache does not contain {0}", uri);
      return true;
    }
  }

}
