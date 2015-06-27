/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2013-2015 Oracle and/or its affiliates. All rights reserved.
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

package org.jvnet.hk2.internal;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.LinkedList;
import java.util.List;

import org.glassfish.hk2.api.ActiveDescriptor;
import org.glassfish.hk2.api.DescriptorFileFinder;
import org.glassfish.hk2.api.DescriptorFileFinderInformation;
import org.glassfish.hk2.api.DynamicConfiguration;
import org.glassfish.hk2.api.DynamicConfigurationService;
import org.glassfish.hk2.api.MultiException;
import org.glassfish.hk2.api.Populator;
import org.glassfish.hk2.api.PopulatorPostProcessor;
import org.glassfish.hk2.api.ServiceLocator;
import org.glassfish.hk2.utilities.ClasspathDescriptorFileFinder;
import org.glassfish.hk2.utilities.DescriptorImpl;

/**
 * Implementation of the Populator for DynamicConfigurationService
 * 
 * @author jwells
 */
public class PopulatorImpl implements Populator {
    private final ServiceLocator serviceLocator;
    private final DynamicConfigurationService dcs;
    
    /* package */ PopulatorImpl(ServiceLocator serviceLocator,
            DynamicConfigurationService dcs) {
        this.serviceLocator = serviceLocator;
        this.dcs = dcs;
    }

    @Override
    public List<ActiveDescriptor<?>> populate(DescriptorFileFinder fileFinder,
            PopulatorPostProcessor... postProcessors) throws IOException {
        List<ActiveDescriptor<?>> descriptors = new LinkedList<ActiveDescriptor<?>> ();

        if (fileFinder == null) {
            fileFinder = serviceLocator.getService(DescriptorFileFinder.class);
            if (fileFinder == null) return descriptors;
        }
        
        if (postProcessors == null) postProcessors = new PopulatorPostProcessor[0];
        
        List<InputStream> descriptorFileInputStreams;
        List<String> descriptorInformation = null;
        try {
            descriptorFileInputStreams = fileFinder.findDescriptorFiles();
            if (fileFinder instanceof DescriptorFileFinderInformation) {
                DescriptorFileFinderInformation dffi = (DescriptorFileFinderInformation) fileFinder;
                
                descriptorInformation = dffi.getDescriptorFileInformation();
                if (descriptorInformation != null && 
                        (descriptorInformation.size() != descriptorFileInputStreams.size())) {
                    throw new IOException("The DescriptorFileFinder implementation " +
                            fileFinder.getClass().getName() + " also implements DescriptorFileFinderInformation, " +
                            "however the cardinality of the list returned from getDescriptorFileInformation (" +
                            descriptorInformation.size() + ") does not equal the cardinality of the list " +
                            "returned from findDescriptorFiles (" + descriptorFileInputStreams.size() + ")");
                }
            }
        }
        catch (IOException ioe) {
            throw ioe;
        }
        catch (Throwable th) {
            throw new MultiException(th);
        }
        
        Collector collector = new Collector();

        DynamicConfiguration config = dcs.createDynamicConfiguration();

        int lcv = 0;
        for (InputStream is : descriptorFileInputStreams) {
            String identifier = (descriptorInformation == null) ? null : descriptorInformation.get(lcv) ;
            lcv++;

            BufferedReader br = new BufferedReader(new InputStreamReader(is));

            try {
                boolean readOne = false;

                do {
                    DescriptorImpl descriptorImpl = new DescriptorImpl();

                    try {
                        readOne = descriptorImpl.readObject(br);
                    }
                    catch (IOException ioe) {
                        if (identifier != null) {
                            collector.addThrowable(new IOException("InputStream with identifier \"" + identifier + "\" failed", ioe));
                        }
                        else {
                            collector.addThrowable(ioe);
                        }
                    }

                    if (readOne) {
                            
                        for (PopulatorPostProcessor pp : postProcessors) {
                            try {
                                descriptorImpl = pp.process(serviceLocator, descriptorImpl);
                            }
                            catch (Throwable th) {
                                if (identifier != null) {
                                    collector.addThrowable(new IOException("InputStream with identifier \"" + identifier + "\" failed", th));
                                }
                                else {
                                    collector.addThrowable(th);
                                }
                                descriptorImpl = null;
                            }

                            if (descriptorImpl == null) {
                                break;
                            }
                        }
                            
                        if (descriptorImpl != null) {
                            descriptors.add(config.bind(descriptorImpl, false));
                        }

                    }
                } while (readOne);

            } finally {
                br.close();
            }
        }
        
        // Prior to commit!
        collector.throwIfErrors();

        config.commit();

        return descriptors;
    }

    /* (non-Javadoc)
     * @see org.glassfish.hk2.api.Populator#populate()
     */
    @Override
    public List<ActiveDescriptor<?>> populate() throws IOException {
        return populate(new ClasspathDescriptorFileFinder());
    }

}
