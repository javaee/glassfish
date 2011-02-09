/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2010-2011 Oracle and/or its affiliates. All rights reserved.
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
package org.jvnet.hk2.junit;

import com.sun.hk2.component.*;

import org.jvnet.hk2.component.ComponentException;
import org.jvnet.hk2.component.Enableable;
import org.jvnet.hk2.component.Habitat;
import org.jvnet.hk2.component.HabitatFactory;
import org.jvnet.hk2.component.Inhabitant;
import org.jvnet.hk2.component.InhabitantsParserFactory;
import org.jvnet.hk2.component.RunLevelService;
import org.jvnet.hk2.component.classmodel.ClassPath;
import org.jvnet.hk2.component.classmodel.InhabitantsFeed;
import org.jvnet.hk2.component.classmodel.InhabitantsParsingContextGenerator;

import java.io.*;
import java.util.*;
import java.util.concurrent.Callable;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Services available to junit tests running with the {@link Hk2Runner} runner.
 *
 * @author Jerome Dochez
 * @uathor Jeff Trent
 */
public class Hk2TestServices {

    private final Logger logger = Logger.getLogger(Hk2TestServices.class.getName());

    private static final boolean USE_CACHE = true;
    private static final SoftCache<ClassPath, InhabitantsParsingContextGenerator> ipcgCache = 
      (USE_CACHE) ? new SoftCache<ClassPath, InhabitantsParsingContextGenerator>() : null;
    
    private Habitat habitat;
    
    private final HabitatFactory habitatFactory;
    private final InhabitantsParserFactory ipFactory;
    private final boolean defaultRLSEnabled;
    
    public Hk2TestServices() {
        this(null, null, true, true);
    }

    protected Hk2TestServices(Class<? extends HabitatFactory> habitatFactoryClass,
        Class<? extends InhabitantsParserFactory> ipFactoryClass,
        boolean defaultRLSEnabled,
        boolean rlsConstraintsEnabled) {
      if (null == habitatFactoryClass || habitatFactoryClass.isInterface()) {
          this.habitatFactory = null;
      } else {
          try {
              this.habitatFactory = habitatFactoryClass.newInstance();
          } catch (Exception e) {
              throw new RuntimeException(e);
          }
      }
      
      if (null == ipFactoryClass || ipFactoryClass.isInterface()) {
          this.ipFactory = null;
      } else {
          try {
              this.ipFactory = ipFactoryClass.newInstance();
          } catch (Exception e) {
              throw new RuntimeException(e);
          }
      }
      
      this.defaultRLSEnabled = defaultRLSEnabled;
      RunLevelInhabitant.enable(rlsConstraintsEnabled);
      
      logger.log(Level.FINER, "Singleton created");

      habitat = createHabitat();
      InhabitantsParser ip = createInhabitantsParser(habitat);
      populateHabitat(habitat, ip);
      preInitialized();
      habitat.initialized();
    }

    protected void populateHabitat(final Habitat habitat, InhabitantsParser ip) {
      final ClassPath classpath = ClassPath.create(habitat, true);
      Callable<InhabitantsParsingContextGenerator> populator = new Callable<InhabitantsParsingContextGenerator>() {
          @Override
          public InhabitantsParsingContextGenerator call() throws Exception {
            InhabitantsParsingContextGenerator ipcgen = InhabitantsParsingContextGenerator.create(habitat);
            Set<String> cpSet = classpath.getEntries();
            for (String fileName : cpSet) {
                File f = new File(fileName);
                if (f.exists()) {
                  try {
                    ipcgen.parse(f);
                  } catch (IOException e) {
                    e.printStackTrace();
                  }
                }
            }
            return ipcgen;
          }
      };

      InhabitantsParsingContextGenerator ipcgen;
      try {
        InhabitantsFeed feed = InhabitantsFeed.create(habitat, ip);
        ipcgen = (USE_CACHE) ? ipcgCache.get(classpath, populator) : populator.call();
        feed.populate(ipcgen);
  
        if (logger.isLoggable(Level.FINER)) {
          Iterator<String> contracts = habitat.getAllContracts();
          while (contracts.hasNext()) {
              String contract = contracts.next();
              logger.log(Level.FINER, "Found contract: {0}", contract);
              for (Inhabitant<?> t : habitat.getInhabitantsByContract(contract)) {
                logger.log(Level.FINER, " --> {0} {1}", new Object[] {t.typeName(), t.metadata()});
              }
          }
        }
      } catch (Exception e) {
        throw new RuntimeException(e);
      }
    }

    protected void preInitialized() {
      RunLevelService<?> rls = habitat.getComponent(RunLevelService.class, "default");
      if (Enableable.class.isInstance(rls)) {
        ((Enableable)rls).enable(defaultRLSEnabled);
      }
    }
    
    public Habitat getHabitat() {
        return habitat;
    }

    public Habitat createHabitat() throws ComponentException {
        if (null != habitatFactory) {
          return habitatFactory.newHabitat();
        }
        
        return new Habitat(); 
    }

    // does not create / spawn RunLevelService
    public Habitat createPopulatedHabitat() throws ComponentException {
      Habitat habitat = createHabitat();
      InhabitantsParser ip = createInhabitantsParser(habitat);
      populateHabitat(habitat, ip);
      habitat.initialized();
      return habitat;
    }
    
    public InhabitantsParser createInhabitantsParser(Habitat h) throws ComponentException {
      if (null != ipFactory) {
        return ipFactory.createInhabitantsParser(h);
      }
      
      return new InhabitantsParser(h); 
  }
}
