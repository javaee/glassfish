/*
 * 
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2007-2010 Sun Microsystems, Inc. All rights reserved.
 * 
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License. You can obtain
 * a copy of the License at https://glassfish.dev.java.net/public/CDDL+GPL.html
 * or glassfish/bootstrap/legal/LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 * 
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at glassfish/bootstrap/legal/LICENSE.txt.
 * Sun designates this particular file as subject to the "Classpath" exception
 * as provided by Sun in the GPL Version 2 section of the License file that
 * accompanied this code.  If applicable, add the following below the License
 * Header, with the fields enclosed by brackets [] replaced by your own
 * identifying information: "Portions Copyrighted [year]
 * [name of copyright owner]"
 * 
 * Contributor(s):
 * 
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

import java.io.IOException;
import java.util.Collection;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.glassfish.hk2.classmodel.reflect.ParsingContext;
import org.jvnet.hk2.component.Habitat;

import com.sun.hk2.component.Holder;
import com.sun.hk2.component.InhabitantIntrospectionScanner;
import com.sun.hk2.component.InhabitantsParser;
import com.sun.hk2.component.InhabitantsScanner;
import com.sun.hk2.component.IntrospectionScanner;

/**
 * Responsible for feeding inhabitants into a habitat.
 *  
 * @author Jerome Dochez
 * @author Jeff Trent
 *
 * @since 3.1
 */
public abstract class InhabitantsHabitatFeed {

  private final Logger logger = Logger.getLogger(InhabitantsHabitatFeed.class.getName());
  
  private final InhabitantsParser ip; 
  
  private Holder<ClassLoader> classLoaderHolder;
  
  /**
   * Creates an InhabitantsHabitatFeed.
   * 
   * @param h reserved for future use
   * @param ip the inhabitants parser sync
   * 
   * @return the InhabitantsHabitatFeed
   */
  public static InhabitantsHabitatFeed create(Habitat h, InhabitantsParser ip) {
    return new InhabitantsHabitatFeed(ip) {};
  }
  
  protected InhabitantsHabitatFeed(InhabitantsParser ip) {
    this.ip = ip;
    setClassLoaderContext(getClass().getClassLoader());
  }
  
  public void setClassLoaderContext(ClassLoader cl) {
    classLoaderHolder = new Holder.Impl<ClassLoader>(cl);
  }

  public void populateHabitat(InhabitantsParsingContextGenerator ipcgen,
      Collection<IntrospectionScanner> supplementalScanners) {
    logger.log(Level.FINER, "Starting to introspect");
    ParsingContext context = ipcgen.getContext();
    InhabitantIntrospectionScanner is = new InhabitantIntrospectionScanner(
        context);
    try {
      ip.parse(is, classLoaderHolder);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
    logger.log(Level.FINER, "finished introspecting");

    logger.log(Level.FINER, "Starting to introspect");
    Collection<InhabitantsScanner> metaInfScanners = ipcgen.getInhabitantsScanners();
    for (InhabitantsScanner scanner : metaInfScanners) {
      try {
        ip.parse(scanner, classLoaderHolder);
        scanner.close();
      } catch (IOException e) {
        throw new RuntimeException(e);
      }
    }
    logger.log(Level.FINER, "finished introspecting");

    if (null != supplementalScanners) {
      for (IntrospectionScanner s : supplementalScanners) {
        logger.log(Level.FINE, "parsing with supplemental scanner {0}", s);
        s.parse(context, classLoaderHolder);
      }
    }
  }
}
