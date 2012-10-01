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
package org.glassfish.hk2.inhabitants;

import org.glassfish.hk2.api.Descriptor;
import org.glassfish.hk2.api.DescriptorType;
import org.glassfish.hk2.api.DynamicConfiguration;
import org.glassfish.hk2.api.DynamicConfigurationService;
import org.glassfish.hk2.api.HK2Loader;
import org.glassfish.hk2.api.MultiException;
import org.glassfish.hk2.api.ServiceLocator;
import org.glassfish.hk2.utilities.BuilderHelper;
import org.glassfish.hk2.utilities.DescriptorImpl;
import org.jvnet.hk2.component.Habitat;
import org.jvnet.hk2.component.Inhabitant;
import org.jvnet.hk2.component.MultiMap;

import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.inject.Singleton;

/**
 * Parses <tt>/META-INF/inhabitants</tt> and populate {@link Habitat}.
 *
 * <p>
 * This class can be subclasses to customize the parsing behavior, which is useful
 * for ignoring some components.
 *
 * @author Kohsuke Kawaguchi
 * @author Jerome Dochez
 */
public class InhabitantsParser implements InhabitantStore {
  
    private final Logger logger = Logger.getLogger(InhabitantsParser.class.getName());
  
    public final ServiceLocator serviceLocator;

    /**
     * Fully qualified class names of components to be replaced, to {@link Class} that replaces them.
     * If the value is null, that means just drops it without a replacement.
     *
     * @see #drop(Class)
     * @see #replace(Class, Class)
     */
    // Whether this feature should belong to this base class is arguable --- perhaps a better
    // approach is to create a sub class that does it?
    private final Map<String,Class<?>> replacements = new HashMap<String,Class<?>>();

    public InhabitantsParser(ServiceLocator serviceLocator) {
        this.serviceLocator = serviceLocator;
    }

    /**
     * Tells {@link InhabitantsParser} that if it encounters the specified component
     * while parsing inhabitants file,
     * simply drop it and pretend that such an inhabitant had never existed.
     *
     * <p>
     * This is useful when the application that's hosting an HK2 environment
     * wants to tweak the inhabitant population at sub-module level.
     */
    public void drop(Class<?> component) {
        drop(component.getName());
    }

    public void drop(String fullyQualifiedClassName) {
        replace(fullyQualifiedClassName,null);
    }

    /**
     * Tells {@link InhabitantsParser} that if it encounters the specified component
     * while parsing inhabitants file,
     * ignore the one in the inhabitants file and instead insert the specified 'new' component.
     *
     * <p>
     * This is useful when the application that's hosting an HK2 environment
     * wants to tweak the inhabitant population at sub-module level.
     */
    public void replace(Class<?> oldComponent, Class<?> newComponent) {
        replace(oldComponent.getName(),newComponent);
    }

    public void replace(String oldComponentFullyQualifiedClassName, Class<?> newComponent) {
        replacements.put(oldComponentFullyQualifiedClassName,newComponent);
    }

    /**
     * Parses the inhabitants file (which is represented by {@link InhabitantsScanner}).
     *
     * <p>
     * All the earlier drop/replace commands will be honored during this process.
     */
    @SuppressWarnings("unchecked")
    public void parse(Iterable<InhabitantParser> scanner, HK2Loader classLoader) throws IOException {
        if (scanner==null)
            return;
        
        if (serviceLocator==null) {
        	throw new RuntimeException("ServiceLocator not available");
        }
        
		DynamicConfigurationService dcs = serviceLocator
				.getService(DynamicConfigurationService.class);
		
        for (InhabitantParser parser : scanner) {

    		DynamicConfiguration config = dcs.createDynamicConfiguration();

        	final String implName = parser.getImplName();

        	if (implName==null) 
        		continue;
        	
			final Iterable<String> indexes = parser.getIndexes();

			final Set<String> contracts = new HashSet<String>();

			Iterator<String> indexIterator = indexes.iterator();



			final MultiMap<String, String> metadata = parser.getMetaData();

		
			DescriptorImpl descriptor = new DescriptorImpl();
			
			descriptor.setImplementation(implName);
			descriptor.addAdvertisedContract(implName);
			descriptor.setScope(Singleton.class.getCanonicalName()); //TODO: scope support?
			descriptor.setLoader(classLoader);
			
			while (indexIterator.hasNext()) {
				String index = indexIterator.next();

				int idx = index.indexOf(':');
				if (-1 != idx) {
					// v=contract:name
					index = index.substring(0, idx);
				}

				descriptor.addAdvertisedContract(index);

			}
			
		
					Iterator<String> cit = descriptor.getAdvertisedContracts()
							.iterator();

					while (cit.hasNext()) {
						String contract = cit.next();
						List<String> name = metadata.get(contract);

						if (name.size() != 0) {
							descriptor.setName(name.get(0));
							break;
						}
					}
			config.bind(descriptor);
		    config.commit();      
        }
    }

    private void reportProblem(String typeName, Exception e) {
//      throw new ComponentException("unable to create inhabitant for: " + typeName, e);
      logger.log(Level.SEVERE, "Unable to create inhabitant for {0} - and therefore ignoring it; check classpath; re: {1}", 
          new Object[] {typeName, e.getMessage()});
      logger.log(Level.INFO, "root cause", e);
    }

    /**
     * Returns true if this inhabitant should be ignored.
     *  
     * @param inhabitantParser
     * @return
     */
    protected boolean isFilteredInhabitant(InhabitantParser inhabitantParser) {
        return false;
    }

    /**
     * Returns true if this inhabitant should be ignored.
     *  
     * @param typeName
     * @return
     */
    protected boolean isFilteredInhabitant(String typeName) {
        return false;
    }
    
    /**
     * Adds the given inhabitant to the habitat, with all its indices.
     */
    protected void add(Inhabitant<?> i, InhabitantParser parser) {
        add(i);

        for (String v : parser.getIndexes()) {
            // register inhabitant to the index
            int idx = v.indexOf(':');
            if(idx==-1) {
                // no name
                addIndex(i,v,null);
            } else {
                // v=contract:name
                String contract = v.substring(0, idx);
                String name = v.substring(idx + 1);
                addIndex(i, contract, name);
            }
        }
    }
    
    /**
     * Returns the contract name given a raw index entry.
     * 
     * @param v
     *    the raw index entry in the format of contract[:name]
     * @param name 
     *  a StringBuilder used to optionally store the service name
     *  
     * @return the contract name
     */
    public static String parseIndex(String v, StringBuilder name) {
      int idx = v.indexOf(':');
      if (-1 == idx) {
        return v;
      } else {
          // v=contract:name
          String contract = v.substring(0, idx);
          if (null != name) {
            name.append(v.substring(idx + 1));
          }
          return contract;
      }
    }

    /**
     * Adds the given inhabitant to the habitat
     * @param i
     */
    @Override
    public void add(Inhabitant<?> i) {
      logger.log(Level.FINE, "adding inhabitant: {0} to habitat {1}", 
          new Object[] {i, serviceLocator});
   //   serviceLocator.add(i);
    }

    /**
     * Adds the given inhabitant index to the habitat
     */
    @Override
    public void addIndex(Inhabitant<?> i, String typeName, String name) {
      logger.log(Level.FINE, "adding index for inhabitant: {0} with typeName {1} and name {2} to habitat {3}",
          new Object[] {i, typeName, name, serviceLocator});
 //     serviceLocator.addIndex(i, typeName, name);
    }

    @Override
    public boolean remove(Inhabitant<?> i) {
      logger.log(Level.FINE, "removing inhabitant: {0} from habitat {1}", 
          new Object[] {i, serviceLocator});
      return true;
    }

    @Override
    public boolean removeIndex(String index, String name) {
      logger.log(Level.FINE, "removing inhabitant index: {0},{1} from habitat {2}", 
          new Object[] {index, name, serviceLocator});
		DynamicConfigurationService dcs = serviceLocator
		.getService(DynamicConfigurationService.class);

		DynamicConfiguration config = dcs.createDynamicConfiguration();

		config.addUnbindFilter(BuilderHelper.createContractFilter(index));

		config.commit();
		return true;
    }

    @Override
    public boolean removeIndex(String index, Object serviceOrInhabitant) {
      logger.log(Level.FINE, "removing inhabitant index: {0},{1} from habitat {2}", 
          new Object[] {index, serviceOrInhabitant, serviceLocator});
      

		DynamicConfigurationService dcs = serviceLocator
				.getService(DynamicConfigurationService.class);

		DynamicConfiguration config = dcs.createDynamicConfiguration();
		
		config.addUnbindFilter(BuilderHelper.createContractFilter(index));
		
		config.commit();
        return true;
    }

}
