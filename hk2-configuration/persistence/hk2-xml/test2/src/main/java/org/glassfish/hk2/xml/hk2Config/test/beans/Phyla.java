/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2016 Oracle and/or its affiliates. All rights reserved.
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
package org.glassfish.hk2.xml.hk2Config.test.beans;

import java.beans.PropertyVetoException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import javax.xml.bind.annotation.XmlElement;

import org.glassfish.hk2.api.Customizer;
import org.glassfish.hk2.xml.api.annotations.Hk2XmlPreGenerate;
import org.glassfish.hk2.xml.hk2Config.test.customizers.PhylaCustomizer;
import org.jvnet.hk2.annotations.Contract;
import org.jvnet.hk2.config.ConfigBeanProxy;
import org.jvnet.hk2.config.ConfigSupport;
import org.jvnet.hk2.config.Configured;
import org.jvnet.hk2.config.DuckTyped;
import org.jvnet.hk2.config.Element;
import org.jvnet.hk2.config.SingleConfigCode;
import org.jvnet.hk2.config.TransactionFailure;

/**
 * @author jwells
 *
 */
@Configured
@Contract
@Hk2XmlPreGenerate
@Customizer(PhylaCustomizer.class)
public interface Phyla extends ConfigBeanProxy {
    @XmlElement
    @Element("*")
    List<Phylum> getPhylum();
    void setPhylum(List<Phylum> runtimes);
    void addPhylum(Phylum addMe);

    @DuckTyped
    <T extends Phylum> List<T> getPhylumByType(Class<T> type);

    @DuckTyped
    <T extends Phylum> T getPhylumByName(String name);

    @DuckTyped
    <T extends Phylum> T createPhylum(Map<String, PropertyValue> properties);

    @DuckTyped
    <T extends Phylum> T deletePhylum(T runtime);
    
    class Duck  {
        public static <T extends Runtime> List<T> getRuntimeByType(Phyla runtimes, Class<T> type) {
            if (type == null || runtimes.getPhylum() == null) {
                return null;
            }
            List<T> result = new ArrayList<T>();
            for (Phylum runtime: runtimes.getPhylum()) {
                if (runtime.getClass().equals(type)) {
                    result.add(type.cast(runtime));
                }
            }
            return result;
        }
      
        @SuppressWarnings("unchecked")
        public static <T extends Phylum> T getRuntimeByName(Phyla runtimes, String name) {
            if (name == null || runtimes.getPhylum() == null) {
                return null;
            }
            for (Phylum runtime: runtimes.getPhylum()) {
                if (runtime.getName().equals(name)) {   
                    return (T) runtime;
                }
            }
            return null;
        }

        @SuppressWarnings("unchecked")
        public static <T extends Phylum> T createRuntime(
                final Phyla runtimes,
                final Map<String, PropertyValue> properties)
                throws TransactionFailure {
            T runtime = (T) ConfigSupport.apply(new SingleConfigCode<Phyla>() {

                @Override
                public Object run(Phyla writeableRuntime)
                        throws TransactionFailure,
                        PropertyVetoException {

                    Objects.requireNonNull(properties);
                    
                    return null;
                }
            }, runtimes);

            // read-only view
            return runtimes.getPhylumByName(runtime.getName());
        }
        
        @SuppressWarnings("unchecked")
        public static <T extends Runtime> T deleteRuntime(final Phyla runtimes,
                final T runtime) throws TransactionFailure {
            return (T) ConfigSupport.apply(new SingleConfigCode<Phyla>() {

                @Override
                public Object run(Phyla writeableRuntimes)
                        throws TransactionFailure {
                    writeableRuntimes.getPhylum().remove(runtime);
                    return runtime; 

                }
            
            }, runtimes);
        
        }
    }


}
